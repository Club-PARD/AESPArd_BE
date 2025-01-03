package com.pard.pree_be.user.controller;

import com.pard.pree_be.user.dto.UserRequestDto;
import com.pard.pree_be.user.dto.UserResponseDto;
import com.pard.pree_be.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "새로운 사용자 생성 ✅", description = "user 의 userName 이랑 email 받아와서 새로운 유저 생성해줌.")
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody UserRequestDto.CreateUser req) {
        userService.save(req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "ID로 사용자 이름 조회 ( Home ) ✅", description = "사용자의 고유 ID를 입력하면 해당 사용자의 이름만 보내줌")
    @GetMapping("/{id}/name")
    public ResponseEntity<String> getUserNameById(@PathVariable UUID id) {
        String userName = userService.getUserNameById(id);
        return ResponseEntity.ok(userName);
    }

    @Operation(summary = "ID로 사용자 이름 및 이메일 조회( My ) ✅", description = "사용자의 고유 ID를 입력받아 해당 사용자의 이름과 이메일을 보내줌.")
    @GetMapping("/{id}/details")
    public ResponseEntity<UserResponseDto> getUserNameAndEmailById(@PathVariable UUID id) {
        UserResponseDto userDetails = userService.getUserNameAndEmailById(id);
        return ResponseEntity.ok(userDetails);
    }

}
