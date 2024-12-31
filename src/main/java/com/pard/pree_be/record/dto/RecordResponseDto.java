package com.pard.pree_be.record.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RecordResponseDto {
    private UUID recordId;
    private UUID userId;
    private List<Integer> recentScores;
    private LocalDateTime createdAt;
}
