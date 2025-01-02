package com.pard.pree_be.feedback.analysis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentScores {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String metricName;

    @ElementCollection
    @CollectionTable(name = "recent_scores_values", joinColumns = @JoinColumn(name = "recent_scores_id"))
    @Column(name = "value")
    private List<Integer> scores = new LinkedList<>();

    @Column
    private double average;

    @PrePersist
    @PreUpdate
    public void updateAverage() {
        this.average = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public void addScore(int score) {
        if (scores.size() >= 5) {
            scores.remove(0); // Remove the oldest score
        }
        scores.add(score);
        updateAverage();
    }
}
