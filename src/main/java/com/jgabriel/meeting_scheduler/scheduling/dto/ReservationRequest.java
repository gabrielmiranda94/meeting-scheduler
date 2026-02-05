package com.jgabriel.meeting_scheduler.scheduling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record ReservationRequest(
        @NotNull Long userId,
        @NotNull Long version,
        @NotBlank(message = "Meeting title is mandatory") String title,
        String description,
        Set<String> participants
) {}