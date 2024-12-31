package com.pard.pree_be.feedback.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MetricStandard {
    private double idealMinValue;
    private double idealMaxValue;
    private double penaltyStep;
    private double penaltyAmount; // 까이는 총 점

    // feedback messages
    private String messagePerfect;
    private String messageNearIdeal;
    private String messageModerateIssue;
    private String messageSevereIssue;
}
