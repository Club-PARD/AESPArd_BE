package com.pard.pree_be.feedback.analysis.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequestDto {
    private UUID practiceId;
    private double totalDuration;
    private double speechSpeed;
    private double audioAvgDecibel;
    private int audioBlankCounter;
    private double eyePercentage;
    private int fillerCounter;
}
