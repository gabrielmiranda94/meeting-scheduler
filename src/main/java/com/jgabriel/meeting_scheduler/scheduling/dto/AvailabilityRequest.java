package com.jgabriel.meeting_scheduler.scheduling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Payload to define a new available time slot.")
public record AvailabilityRequest(

        @NotNull
        @Future
        @Schema(description = "Start time (ISO 8601). Must be in the future.", example = "2026-12-01T10:00:00")
        LocalDateTime startTime,

        @NotNull
        @Future
        @Schema(description = "End time (ISO 8601). Must be after start time.", example = "2026-12-01T11:00:00")
        LocalDateTime endTime,

        @NotNull
        @Schema(description = "ID of the calendar owner.", example = "1")
        Long ownerId
) {}