package com.pard.pree_be.practice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricInput {
    private String metricName;
    private double value; // Actual val for metric

}
