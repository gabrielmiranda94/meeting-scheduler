package com.jgabriel.meeting_scheduler.scheduling.dto;

import com.jgabriel.meeting_scheduler.scheduling.BlockStatus;
import com.jgabriel.meeting_scheduler.scheduling.TimeBlock;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Representation of a time slot (Available or Reserved).")
public record TimeBlockResponse(
        @Schema(description = "Unique Slot ID", example = "10")
        Long id,

        @Schema(description = "Start time", example = "2026-12-01T10:00:00")
        LocalDateTime startTime,

        @Schema(description = "End time", example = "2026-12-01T11:00:00")
        LocalDateTime endTime,

        @Schema(description = "Status of the slot", example = "AVAILABLE")
        BlockStatus status,

        @Schema(description = "Current version for Optimistic Locking", example = "1")
        Long version,

        @Schema(description = "Title (if reserved)", example = "Daily Meeting")
        String title,

        @Schema(description = "List of participants", example = "[\"dev@test.com\"]")
        Set<String> participants,

        @Schema(description = "ID of the user who reserved (if any)", example = "500")
        Long reservedBy
) {
    public static TimeBlockResponse from(TimeBlock block) {
        return new TimeBlockResponse(
                block.getId(),
                block.getStartTime(),
                block.getEndTime(),
                block.getStatus(),
                block.getVersion(),
                block.getTitle(),
                block.getParticipants(),
                block.getReservedBy()
        );
    }
}