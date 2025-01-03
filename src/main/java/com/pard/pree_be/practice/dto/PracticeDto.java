package com.pard.pree_be.practice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PracticeDto {
    private Long id;
    private String practiceName;

    @JsonProperty("createdAt")
    private LocalDateTime practiceCreatedAt;
    private int totalScore;
    private UUID analysisId;
    private String videoKey;

}
