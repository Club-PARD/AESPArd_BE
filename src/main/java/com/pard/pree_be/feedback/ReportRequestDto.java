package com.pard.pree_be.feedback.report.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReportRequestDto {
    private UUID analysisId;
    private String name;
    private double counter;
}
