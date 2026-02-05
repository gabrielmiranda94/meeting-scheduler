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

    public Page<TimeBlock> getAvailableBlocks(Pageable pageable) {
        return blockRepository.findByStatus(BlockStatus.AVAILABLE, pageable);
    }

    @Transactional
    public TimeBlock addAvailability(LocalDateTime start, LocalDateTime end, Long ownerId) {
        TimeBlock block = TimeBlock.builder()
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
                .orElseThrow(() -> new IllegalArgumentException(MSG_BLOCK_NOT_FOUND + blockId));

        if (!block.getVersion().equals(request.version())) {
            throw new OptimisticLockingFailureException(
                    String.format(MSG_VERSION_CONFLICT, request.version(), block.getVersion())
            );
        }

        if (block.getStatus() != BlockStatus.AVAILABLE) {
            throw new IllegalStateException(MSG_BLOCK_UNAVAILABLE);
        }

        block.setStatus(BlockStatus.RESERVED);
        block.setReservedBy(request.userId());
        block.setTitle(request.title());
        block.setDescription(request.description());

        if (request.participants() != null) {
            block.setParticipants(request.participants());
        }

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
}