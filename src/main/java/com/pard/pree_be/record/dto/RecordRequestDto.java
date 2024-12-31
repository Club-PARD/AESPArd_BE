package com.pard.pree_be.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordRequestDto {

    @NotBlank(message = "Recent scores cannot be blank.")
    @Pattern(regexp = "^(\\d+,){5}\\d+$", message = "Recent scores must be a comma-separated list of 6 integers.")
    private String recentScores; // Format: "60,30,20,50,50,50"
}