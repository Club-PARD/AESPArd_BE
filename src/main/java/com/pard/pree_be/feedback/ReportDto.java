package com.pard.pree_be.feedback.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportDto {
    private String name;
    private double counter;
    private int score;
    private String feedbackMessage;
}
