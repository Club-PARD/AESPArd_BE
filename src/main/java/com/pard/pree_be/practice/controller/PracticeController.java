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

        PracticeResponseDto response = practiceService.addPracticeToExistingPresentation(presentationId, videoKey, eyePercentage, audioFile);
        return ResponseEntity.status(201).body(response);
    }



    @PostMapping("/recent-presentation/add-practice")
    @Operation(summary = "가장 최근 발표에 연습 추가하기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Practice successfully added to the most recently updated presentation."),
            @ApiResponse(responseCode = "404", description = "No recent presentation found."),
            @ApiResponse(responseCode = "400", description = "Invalid data.")
    })
    public ResponseEntity<PracticeResponseDto> addPracticeToMostRecentlyUpdatedPresentation(
            @RequestParam UUID userId,
            @RequestParam String videoKey,
            @RequestParam int eyePercentage,
            @RequestPart MultipartFile audioFile) throws IOException {

        PracticeResponseDto response = practiceService.addPracticeToMostRecentlyUpdatedPresentation(userId, videoKey, eyePercentage, audioFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "선택한 발표 연습 리스트 불러오기 ", description = "엽습이름 , 날짜, 점수! send ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practices successfully retrieved."),
            @ApiResponse(responseCode = "404", description = "No practices found.")
    })
    public ResponseEntity<List<PracticeDto>> getPracticesByPresentation(@RequestParam UUID presentationId) {
        List<PracticeDto> practices = practiceService.getPracticesByPresentationId(presentationId);
        return ResponseEntity.ok(practices);
    }

    @GetMapping("/recent")
    @Operation(summary = " 선책한 발표 가장 최근 연습 불러오기", description = "엽습이름 , 날짜, 점수! send ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practices successfully retrieved."),
            @ApiResponse(responseCode = "404", description = "No practices found.")
    })
    public ResponseEntity<List<PracticeDto>> getRecentPractice(@RequestParam UUID presentationId) {
        List<PracticeDto> practices = practiceService.getRecentPractice(presentationId);
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

    @DeleteMapping("/{practiceId}/one-delete")
    @Operation(summary = " 연습 하나 삭제하는거!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practice deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Practice not found.")
    })
    public ResponseEntity<Void> deletePractice(@PathVariable Long practiceId) {
        practiceService.deletePractice(practiceId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/batch-delete")
    @Operation(summary = " 연습 여러 선택해서 삭제!!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practices deleted successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid request.")
    })
    public ResponseEntity<Void> batchDeletePractices(@RequestBody List<Long> practiceIds) {
        practiceService.batchDeletePractices(practiceIds);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{practiceId}/update-name")
    @Operation(summary = "연습이름 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practice name updated successfully."),
            @ApiResponse(responseCode = "404", description = "Practice not found.")
    })
    public ResponseEntity<Void> updatePracticeName(@PathVariable Long practiceId, @RequestBody String newName) {
        // Sanitize the name: remove quotes and trim whitespace
        String sanitizedNewName = newName.replaceAll("^\"|\"$", "").trim();
        practiceService.updatePracticeName(practiceId, sanitizedNewName);
        return ResponseEntity.ok().build();
    }



}
