package com.jgabriel.meeting_scheduler.scheduling;

import com.jgabriel.meeting_scheduler.scheduling.dto.AvailabilityRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.ReservationRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.TimeBlockResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final SchedulingService schedulingService;

    @GetMapping
    public ResponseEntity<Page<TimeBlockResponse>> list(
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        Page<TimeBlockResponse> response = schedulingService.getAvailableBlocks(pageable)
                .map(TimeBlockResponse::from);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TimeBlockResponse> defineAvailability(@RequestBody @Valid AvailabilityRequest request) {
        if (!request.isValid()) {
            return ResponseEntity.badRequest().build();
        }
        TimeBlock block = schedulingService.addAvailability(
                request.startTime(),
                request.endTime(),
                request.ownerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeBlockResponse.from(block));
    }

    @PostMapping("/{blockId}/reserve")
    public ResponseEntity<TimeBlockResponse> reserve(
            @PathVariable Long blockId,
            @RequestBody @Valid ReservationRequest request) {

        TimeBlock block = schedulingService.reserveBlock(blockId, request);
        return ResponseEntity.ok(TimeBlockResponse.from(block));
    }

    @PostMapping("/{blockId}/cancel")
    public ResponseEntity<TimeBlockResponse> cancel(
            @PathVariable Long blockId,
            @RequestParam Long userId) {

        TimeBlock block = schedulingService.cancelReservation(blockId, userId);
        return ResponseEntity.ok(TimeBlockResponse.from(block));
    }
}