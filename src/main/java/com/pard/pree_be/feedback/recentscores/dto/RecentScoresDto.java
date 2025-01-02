package com.pard.pree_be.feedback.recentscores.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RecentScoresDto {
    private String metricName;
    private List<Integer> recentScores;
    private double average;
}
