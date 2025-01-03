package com.pard.pree_be.presentation.dto;

import com.pard.pree_be.presentation.entity.Presentation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PresentationResponseDto {

    private UUID presentationId;
    private String presentationName;
    private LocalDateTime createdAt;
    private int totalScore;
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

