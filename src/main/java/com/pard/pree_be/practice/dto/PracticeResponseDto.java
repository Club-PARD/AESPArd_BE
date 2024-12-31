package com.pard.pree_be.practice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PracticeResponseDto {
    private UUID id;
    private String practiceName;
    private LocalDateTime createdAt;
    private int totalScore;
    private String videoKey;
}