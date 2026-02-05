package com.jgabriel.meeting_scheduler.scheduling;

import com.jgabriel.meeting_scheduler.scheduling.dto.AvailabilityRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.ReservationRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.TimeBlockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Availability Management", description = "Endpoints for managing time slots, aggregated calendar views, and reservation lifecycles.")
public class AvailabilityController {

    private final SchedulingService schedulingService;

    @Operation(summary = "List available slots (Aggregated View)",
            description = "Fetch slots with pagination. Supports filtering by specific User ID or Date Ranges (simulating a calendar view).")
    @ApiResponse(responseCode = "200", description = "List of slots retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<TimeBlockResponse>> list(
            @Parameter(description = "Filter by Slot Owner ID")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "Start of date range (ISO 8601 format: YYYY-MM-DDThh:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "End of date range (ISO 8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(hidden = true) // O Springdoc documenta paginação automaticamente, ocultamos o parâmetro bruto
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        Page<TimeBlockResponse> response = schedulingService.getAvailableBlocks(userId, from, to, pageable)
                .map(TimeBlockResponse::from);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create availability slot", description = "Owners define new time slots available for booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Slot created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., End time before Start time)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<TimeBlockResponse> defineAvailability(@RequestBody @Valid AvailabilityRequest request) {
        TimeBlock block = schedulingService.addAvailability(
                request.startTime(),
                request.endTime(),
                request.ownerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeBlockResponse.from(block));
    }

    @Operation(summary = "Reserve a slot",
            description = "Clients book an available slot. **Enforces Optimistic Locking**: You must provide the correct `version`. Returns 409 if data is stale.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation successful"),
            @ApiResponse(responseCode = "404", description = "Slot ID not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Concurrency Conflict (Stale Version) or Slot already booked",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{blockId}/reserve")
    public ResponseEntity<TimeBlockResponse> reserve(
            @Parameter(description = "ID of the time slot to reserve") @PathVariable Long blockId,
            @RequestBody @Valid ReservationRequest request) {

        TimeBlock block = schedulingService.reserveBlock(blockId, request);
        return ResponseEntity.ok(TimeBlockResponse.from(block));
    }

    @Operation(summary = "Cancel reservation", description = "Frees up a reserved slot, making it available again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancellation successful"),
            @ApiResponse(responseCode = "404", description = "Slot ID not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Slot is not currently reserved",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{blockId}/cancel")
    public ResponseEntity<TimeBlockResponse> cancel(
            @PathVariable Long blockId,
            @Parameter(description = "User ID requesting cancellation") @RequestParam Long userId) {

        TimeBlock block = schedulingService.cancelReservation(blockId, userId);
        return ResponseEntity.ok(TimeBlockResponse.from(block));
    }

    @Operation(summary = "Delete slot", description = "Permanently removes a time slot from the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Slot deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Slot ID not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> delete(@PathVariable Long blockId) {
        schedulingService.deleteBlock(blockId);
        return ResponseEntity.noContent().build();
    }
}