package com.pard.pree_be.practice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PracticeRequestDto {
    private UUID presentationId;
    private String practiceName;
    private String videoKey;
    private int eyePercentage;


    private byte[] audioFile;
}