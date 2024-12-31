package com.pard.pree_be.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PresentationCellDto {

    private UUID presentationId;
    private String presentationName;
    private boolean toggleFavorite;
    private int totalScore;
    private int totalPractices;
    private String updatedAtText; // "1일전"
    private boolean showMeOnScreen;
    private boolean showTimeOnScreen;

    private LocalDateTime updatedAt;
    private double idealMinTime;
    private double idealMaxTime;
}