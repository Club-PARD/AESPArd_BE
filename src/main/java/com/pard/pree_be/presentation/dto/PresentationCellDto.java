package com.pard.pree_be.presentation.dto;

import com.pard.pree_be.presentation.entity.Presentation;
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

    public static PresentationResponseDto fromEntity(Presentation presentation) {
        PresentationResponseDto dto = new PresentationResponseDto();
        dto.setPresentationId(presentation.getPresentationId());
        dto.setPresentationName(presentation.getPresentationName());
        dto.setCreatedAt(presentation.getCreatedAt());
        dto.setTotalScore(presentation.getTotalScore());
        return dto;
    }
}