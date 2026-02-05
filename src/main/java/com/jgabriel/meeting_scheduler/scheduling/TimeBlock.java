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
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockStatus status;

    private String title;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_participants", joinColumns = @JoinColumn(name = "time_block_id"))
    @Column(name = "participant_email")
    @Builder.Default
    private Set<String> participants = new HashSet<>();

    private Long reservedBy;

    @Version
    private Long version;
}