package com.jgabriel.meeting_scheduler.scheduling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "Payload to reserve an existing slot.")
public record ReservationRequest(

        @NotNull
        @Schema(description = "ID of the user making the reservation.", example = "500")
        Long userId,

        @NotNull
        @Schema(description = "Optimistic Locking Version. Must match the current DB version.", example = "0")
        Long version,

        @NotBlank
        @Schema(description = "Meeting title.", example = "Project Kickoff")
        String title,

        @Schema(description = "Detailed description.", example = "Discussion about Q1 goals")
        String description,

        @Schema(description = "List of attendee emails.", example = "[\"alice@corp.com\", \"bob@corp.com\"]")
        Set<String> participants
) {}