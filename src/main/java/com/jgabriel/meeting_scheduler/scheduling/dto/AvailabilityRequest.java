package com.jgabriel.meeting_scheduler.scheduling.dto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AvailabilityRequest(
        @NotNull(message = "Start time is mandatory")
        @Future(message = "Start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time is mandatory")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime
) {
    public boolean isValid() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}