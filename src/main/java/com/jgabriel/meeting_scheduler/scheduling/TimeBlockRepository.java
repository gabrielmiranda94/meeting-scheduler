package com.jgabriel.meeting_scheduler.scheduling;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
    List<TimeBlock> findByStatus(BlockStatus status);
}