package com.jgabriel.meeting_scheduler.scheduling;

import com.jgabriel.meeting_scheduler.scheduling.dto.AvailabilityRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.ReservationRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.TimeBlockResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final SchedulingService schedulingService;

    @GetMapping
    public ResponseEntity<Page<TimeBlockResponse>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        Page<TimeBlockResponse> response = schedulingService.getAvailableBlocks(userId, from, to, pageable)
                .map(TimeBlockResponse::from);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TimeBlockResponse> defineAvailability(@RequestBody @Valid AvailabilityRequest request) {
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

    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> delete(@PathVariable Long blockId) {
        schedulingService.deleteBlock(blockId);
        return ResponseEntity.noContent().build();
    }
}