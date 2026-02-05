package com.jgabriel.meeting_scheduler.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationRequest(
        @NotNull Long userId,
        @NotNull Long version
) {}