package com.pard.pree_be.feedback.report.dto;

import lombok.Data;

@Data
public class ReportResponseDto {
    private String name;
    private double counter;
    private int score;
    private String feedbackMessage;
}
