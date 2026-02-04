package com.jgabriel.meeting_scheduler.scheduling;
import com.jgabriel.meeting_scheduler.scheduling.dto.AvailabilityRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.ReservationRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.TimeBlockResponse; // Import novo
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final SchedulingService schedulingService;

    @GetMapping
    public ResponseEntity<List<TimeBlockResponse>> list() {
        List<TimeBlockResponse> response = schedulingService.getAvailableBlocks()
                .stream()
                .map(TimeBlockResponse::from) // Converte cada item
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TimeBlockResponse> defineAvailability(@RequestBody @Valid AvailabilityRequest request) {
        if (!request.isValid()) {
            return ResponseEntity.badRequest().build();
        }
        TimeBlock block = schedulingService.addAvailability(request.startTime(), request.endTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(TimeBlockResponse.from(block));
    }

    @PostMapping("/{blockId}/reserve")
    public ResponseEntity<TimeBlockResponse> reserve(@PathVariable Long blockId, @RequestBody @Valid ReservationRequest request) {
        TimeBlock block = schedulingService.reserveBlock(blockId, request.userId());
        return ResponseEntity.ok(TimeBlockResponse.from(block));
    }
}