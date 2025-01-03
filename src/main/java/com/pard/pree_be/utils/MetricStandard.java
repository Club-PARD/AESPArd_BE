package com.pard.pree_be.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetricStandard {
    private double idealMinValue;       // Minimum ideal value
    private double idealMaxValue;       // Maximum ideal value
    private double penaltyStep;         // Step for penalty (e.g., +5/-5 WPM)
    private double penaltyAmount;       // Penalty amount per step (e.g., -7 points)

    private String messagePerfect;      // Feedback for perfect performance
    private String messageNearIdeal;    // Feedback for near-ideal performance
    private String messageSevereIssue;  // Feedback for significant issues
    private String messageFast;         // Feedback for too fast (if applicable)
    private String messageSlow;         // Feedback for too slow (if applicable)
}

