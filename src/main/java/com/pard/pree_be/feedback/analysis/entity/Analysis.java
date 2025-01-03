package com.pard.pree_be.feedback.analysis.entity;

import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.feedback.report.entity.Report;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private double duration;
    private double speechSpeed;
    private double decibel;
    private int fillerCount;
    private int blankCount;
    private int eyePercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_id", nullable = false)
    private Practice practice;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();


    @Column(nullable = false)
    private int totalScore;
}
