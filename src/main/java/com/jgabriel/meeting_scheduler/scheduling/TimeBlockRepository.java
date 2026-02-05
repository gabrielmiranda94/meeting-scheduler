package com.jgabriel.meeting_scheduler.scheduling;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    @Query("SELECT t FROM TimeBlock t WHERE " +
            "(cast(:userId as Long) IS NULL OR t.ownerId = :userId) AND " +
            "(cast(:from as java.time.LocalDateTime) IS NULL OR t.startTime >= :from) AND " +
            "(cast(:to as java.time.LocalDateTime) IS NULL OR t.endTime <= :to)")
    Page<TimeBlock> findWithFilters(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}