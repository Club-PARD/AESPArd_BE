package com.pard.pree_be.feedback.report.entity;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name; // Metric name (e.g., "duration", "speechSpeed")
    private int counter; // Raw metric value
    private int score; // Calculated score (0-100)
    private String feedbackMessage; // Generated feedback for the user

    @ManyToOne
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Column(nullable = false)
    private int totalScore;
}
