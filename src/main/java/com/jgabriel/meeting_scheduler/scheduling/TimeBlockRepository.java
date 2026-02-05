package com.jgabriel.meeting_scheduler.scheduling;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
    Page<TimeBlock> findByStatus(BlockStatus status, Pageable pageable);
}