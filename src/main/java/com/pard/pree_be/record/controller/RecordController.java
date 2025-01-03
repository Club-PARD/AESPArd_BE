package com.pard.pree_be.record.controller;

import com.pard.pree_be.record.dto.RecordRequestDto;
import com.pard.pree_be.record.dto.RecordResponseDto;
import com.pard.pree_be.record.entity.Record;
import com.pard.pree_be.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/recent-average")
    @Operation(summary = "최근 연습 5개 평균 점수 6개 ( Home ) ✅", description = "유저 하드코딩으로 설정해둬쓰")
    public List<Integer> getNumbers() {
        return new Random().ints(6, 50, 101)
                .boxed()
                .collect(Collectors.toList());
    }


}