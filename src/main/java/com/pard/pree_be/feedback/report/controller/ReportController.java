package com.pard.pree_be.feedback.report.controller;

import com.pard.pree_be.feedback.report.dto.ReportDto;
import com.pard.pree_be.feedback.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> createTestReport(@RequestParam UUID practiceId) {
        reportService.generateTestReport(practiceId);
        return ResponseEntity.status(201).body("Test report and related analysis generated successfully!");
    }

    @GetMapping
    public ResponseEntity<List<ReportDto>> getReports(@RequestParam UUID analysisId) {
        return ResponseEntity.ok(reportService.getReportsForAnalysis(analysisId));
    }

}