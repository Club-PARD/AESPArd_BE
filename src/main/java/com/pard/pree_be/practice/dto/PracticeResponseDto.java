package com.pard.pree_be.practice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PracticeResponseDto {
    private Long id;
    private String practiceName;
    @JsonProperty("createdAt")
    private LocalDateTime practiceCreatedAt;
    private int totalScore;
    private String videoKey;
    private int presentationTotalScore;
}