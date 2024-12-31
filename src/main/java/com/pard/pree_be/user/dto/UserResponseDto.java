package com.pard.pree_be.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
public class UserResponseDto {
    private String userName;
    private String email;
}