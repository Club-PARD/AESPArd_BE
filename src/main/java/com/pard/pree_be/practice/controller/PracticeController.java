package com.pard.pree_be.practice.controller;

import com.pard.pree_be.practice.dto.PracticeDto;
import com.pard.pree_be.practice.dto.PracticeRequestDto;
import com.pard.pree_be.practice.dto.PracticeResponseDto;
import com.pard.pree_be.practice.service.PracticeService;
import com.pard.pree_be.presentation.dto.PresentationRequestDto;
import com.pard.pree_be.presentation.dto.PresentationResponseDto;
import com.pard.pree_be.presentation.entity.Presentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/practices")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    @PostMapping("/{presentationId}/add-practice")
    @Operation(summary = "선택한 발표에 연습 추가하기 ( AddPractice )", description = "기존 발표에 연습을 추가")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "발표 연습이 성공적으로 추가되었습니다."),
            @ApiResponse(responseCode = "400", description = "요청 데이터가 잘못되었습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인해 연습을 추가할 수 없습니다.")
    })
    public ResponseEntity<PracticeResponseDto> addPractice(
            @PathVariable UUID presentationId,
            @RequestParam(required = false) String videoKey,
            @RequestParam(required = false) Integer eyePercentage,
            @RequestParam(required = false) MultipartFile audioFile) throws IOException {

        if (videoKey == null || videoKey.isEmpty()) {
            throw new IllegalArgumentException("videoKey is missing.");
        }
        if (eyePercentage == null) {
            throw new IllegalArgumentException("eyePercentage is missing.");
        }
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("audioFile is missing.");
        }


        PracticeResponseDto response = practiceService.addPracticeToExistingPresentation(presentationId, videoKey, eyePercentage, audioFile);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{userId}/recent-presentation/add-practice")
    @Operation(summary = " 가장 최근 발표에 연습 새로 추가 ( AddPresentation/ AddPractice )", description = " 잔머리 굴려서 만듬ㅋㅋㅋㅋ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Practice successfully added to the most recent presentation."),
            @ApiResponse(responseCode = "404", description = "No recent presentation found for the user."),
            @ApiResponse(responseCode = "400", description = "Invalid request data.")
    })
    public ResponseEntity<PresentationResponseDto> addPracticeToRecentPresentation(
            @PathVariable UUID userId,
            @RequestParam String videoKey,
            @RequestParam int eyePercentage,
            @RequestPart MultipartFile audioFile) throws IOException {

        Presentation updatedPresentation = practiceService.addPracticeToRecentPresentation(userId, videoKey, eyePercentage, audioFile);
        PresentationResponseDto responseDto = PresentationResponseDto.fromEntity(updatedPresentation);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    @GetMapping
    @Operation(summary = "선택한 발표 연습 리스트 불러오기 ( List )", description = "엽습이름 , 날짜, 점수! send ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practices successfully retrieved."),
            @ApiResponse(responseCode = "404", description = "No practices found.")
    })
    public ResponseEntity<List<PracticeDto>> getPracticesByPresentation(@RequestParam UUID presentationId) {
        List<PracticeDto> practices = practiceService.getPracticesByPresentationId(presentationId);
        return ResponseEntity.ok(practices);
    }

    @GetMapping("/recent-scores")
    @Operation(summary = "최근 점수 그래프에 들어가는거 ..!!! ( List )", description = "선택한 발표에 해당되는 최근 연습 score 불러와줌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scores successfully retrieved."),
            @ApiResponse(responseCode = "404", description = "No practices found for the given presentation ID.")
    })
    public ResponseEntity<List<Integer>> getRecentPracticeScores(@RequestParam UUID presentationId) {
        List<Integer> scores = practiceService.getRecentPracticeScores(presentationId);
        return ResponseEntity.ok(scores);
    }
}
