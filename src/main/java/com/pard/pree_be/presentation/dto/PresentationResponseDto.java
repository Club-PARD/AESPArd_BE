package com.pard.pree_be.presentation.dto;

import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.practice.repo.PracticeRepo;
import com.pard.pree_be.practice.entity.Practice;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;
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

    public static PresentationResponseDto fromEntity(Presentation presentation, PracticeRepo practiceRepo) {
        PresentationResponseDto dto = new PresentationResponseDto();
        dto.setPresentationId(presentation.getPresentationId());
        dto.setPresentationName(presentation.getPresentationName());
        dto.setCreatedAt(presentation.getCreatedAt());
        dto.setIdealMinTime(presentation.getIdealMinTime());
        dto.setIdealMaxTime(presentation.getIdealMaxTime());

        Optional<Integer> mostRecentPracticeScore = practiceRepo
                .findMostRecentPracticeByPresentationId(presentation.getPresentationId())
                .map(Practice::getTotalScore);

        dto.setTotalScore(mostRecentPracticeScore.orElse(0));

        return dto;
    }
}

