package com.jgabriel.meeting_scheduler.scheduling.dto;

import com.jgabriel.meeting_scheduler.scheduling.TimeBlock;
import java.time.LocalDateTime;

public record TimeBlockResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Long reservedBy,
        Long version
) {
    public static TimeBlockResponse from(TimeBlock block) {
        return new TimeBlockResponse(
                block.getId(),
                block.getStartTime(),
                block.getEndTime(),
                block.getStatus().name(),
                block.getReservedBy(),
                block.getVersion()
        );
    }
}