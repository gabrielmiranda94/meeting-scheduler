package com.jgabriel.meeting_scheduler.scheduling.dto;

import com.jgabriel.meeting_scheduler.scheduling.TimeBlock;
import java.time.LocalDateTime;
import java.util.Set;

public record TimeBlockResponse(
        Long id,
        Long ownerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Long reservedBy,
        String title,
        String description,
        Set<String> participants,
        Long version
) {
    public static TimeBlockResponse from(TimeBlock block) {
        return new TimeBlockResponse(
                block.getId(),
                block.getOwnerId(),
                block.getStartTime(),
                block.getEndTime(),
                block.getStatus().name(),
                block.getReservedBy(),
                block.getTitle(),
                block.getDescription(),
                block.getParticipants(),
                block.getVersion()
        );
    }
}