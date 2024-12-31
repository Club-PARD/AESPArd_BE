package com.pard.pree_be.record.entity;

import com.pard.pree_be.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID recordId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private int totalPresentations;

    @ElementCollection
    private List<Integer> recentScores;

    @Column(nullable = false)
    private double recentAverageScore;
}