package com.pard.pree_be.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationRequestDto {

    @NotNull
    private UUID userId;

    @NotBlank
    private String presentationName;

    @Positive
    private double idealMinTime;

    @Positive
    private double idealMaxTime;

    private String updatedAtText; // "1일전"

    private boolean showMeOnScreen;
    private boolean showTimeOnScreen;

}