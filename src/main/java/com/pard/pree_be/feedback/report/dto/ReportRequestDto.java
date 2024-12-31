package com.pard.pree_be.feedback.report.dto;

import lombok.Data;

@Data
public class ReportRequestDto {
    private String name; // Metric name
    private double counter; // Raw metric value
    private int score; // Calculated score
    private String feedbackMessage; // Feedback
}
