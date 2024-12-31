package com.pard.pree_be.feedback.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportResponseDto {
    private String name;
    private int counter;
    private int score;
    private String feedbackMessage;
}
