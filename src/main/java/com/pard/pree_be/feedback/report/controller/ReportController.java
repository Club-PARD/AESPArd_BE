package com.pard.pree_be.feedback.report.controller;

import com.pard.pree_be.feedback.report.dto.ReportResponseDto;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepo reportRepo;

    @GetMapping("/{analysisId}")
    public ResponseEntity<List<ReportResponseDto>> getReportsByAnalysisId(@PathVariable UUID analysisId) {
        List<Report> reports = reportRepo.findByAnalysisId(analysisId);

        List<ReportResponseDto> response = reports.stream()
                .map(report -> ReportResponseDto.builder()
                        .name(report.getName())
                        .counter(report.getCounter())
                        .score(report.getScore())
                        .feedbackMessage(report.getFeedbackMessage())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
