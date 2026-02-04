package com.jgabriel.meeting_scheduler.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private static final String MSG_BLOCK_NOT_FOUND = "Time block not found with ID: ";
    private static final String MSG_BLOCK_UNAVAILABLE = "This time block is already reserved";

    private final TimeBlockRepository blockRepository;

    public List<TimeBlock> getAvailableBlocks() {
        return blockRepository.findAll();
    }

    @Transactional
    public TimeBlock addAvailability(LocalDateTime start, LocalDateTime end) {
        TimeBlock block = TimeBlock.builder()
                .startTime(start)
                .endTime(end)
                .status(BlockStatus.AVAILABLE)
                .build();
        return blockRepository.save(block);
    }

    @Transactional
    public TimeBlock reserveBlock(Long blockId, Long userId) {
        TimeBlock block = blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_BLOCK_NOT_FOUND + blockId));

        if (block.getStatus() != BlockStatus.AVAILABLE) {
            throw new IllegalStateException(MSG_BLOCK_UNAVAILABLE);
        }

        block.setStatus(BlockStatus.RESERVED);
        block.setReservedBy(userId);

        return blockRepository.save(block);
    }
}