package com.mrtob.srs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String front;

    @Column(nullable = false)
    private String back;

    // SM-2
    @Builder.Default
    private double easinessFactor = 2.5;

    private int intervalDays;
    private int repetitions;

    // FSRS
    private double stability;
    private double difficulty;

    // Learning step index (0 = first step, -1 = graduated to review phase)
    @Builder.Default
    private int learningStep = 0;

    @Column(nullable = false)
    private Instant nextReview;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
