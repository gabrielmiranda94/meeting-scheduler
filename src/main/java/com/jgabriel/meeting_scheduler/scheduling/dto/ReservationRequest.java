package com.jgabriel.meeting_scheduler.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationRequest(
        @NotNull(message = "User ID is required for reservation")
        @Positive
        Long userId
) {}