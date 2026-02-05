package com.jgabriel.meeting_scheduler.scheduling;

import com.jgabriel.meeting_scheduler.scheduling.dto.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private static final String MSG_BLOCK_NOT_FOUND = "Time block not found with ID: ";
    private static final String MSG_BLOCK_UNAVAILABLE = "This time block is already reserved";
    private static final String MSG_VERSION_CONFLICT = "Optimistic locking failure: Client provided version %d, but current version is %d.";

    private final TimeBlockRepository blockRepository;

    public Page<TimeBlock> getAvailableBlocks(Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return blockRepository.findWithFilters(userId, from, to, pageable);
    }

    @Transactional
    public TimeBlock addAvailability(LocalDateTime start, LocalDateTime end, Long ownerId) {
        if (end.isBefore(start)) {
            throw new IllegalStateException("End time cannot be before start time");
        }

        var block = TimeBlock.builder()
                .startTime(start)
                .endTime(end)
                .ownerId(ownerId)
                .status(BlockStatus.AVAILABLE)
                .build();

        return blockRepository.save(block);
    }

    @Transactional
    public TimeBlock reserveBlock(Long blockId, ReservationRequest request) {
        TimeBlock block = blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found with id: " + blockId));

        if (!block.getVersion().equals(request.version())) {
            throw new OptimisticLockingFailureException("Stale data: version mismatch");
        }

        if (block.getStatus() != BlockStatus.AVAILABLE) {
            throw new OptimisticLockingFailureException("Slot is already reserved");
        }

        block.setStatus(BlockStatus.RESERVED);
        block.setReservedBy(request.userId());
        block.setTitle(request.title());
        block.setDescription(request.description());
        block.setParticipants(request.participants());

        return blockRepository.save(block);
    }

    @Transactional
    public TimeBlock cancelReservation(Long blockId, Long userId) {
        TimeBlock block = blockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_BLOCK_NOT_FOUND + blockId));

        if (!userId.equals(block.getReservedBy())) {
            throw new IllegalStateException("Only the user who reserved the slot can cancel it.");
        }

        block.setStatus(BlockStatus.AVAILABLE);
        block.setReservedBy(null);
        block.setTitle(null);
        block.setDescription(null);
        block.getParticipants().clear();

        return blockRepository.save(block);
    }

    public void deleteBlock(Long blockId) {
        if (!blockRepository.existsById(blockId)) {
            throw new IllegalArgumentException("Block not found");
        }
        blockRepository.deleteById(blockId);
    }
}