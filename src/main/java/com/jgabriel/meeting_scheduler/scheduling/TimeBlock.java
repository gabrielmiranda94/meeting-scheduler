package com.jgabriel.meeting_scheduler.scheduling;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "time_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockStatus status = BlockStatus.AVAILABLE;

    @Column(name = "reserved_by_user_id")
    private Long reservedBy;

    private String title;
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "time_block_id")
    )
    @Column(name = "participant")
    @Builder.Default
    private Set<String> participants = new HashSet<>();

    @Version
    private Long version;
}