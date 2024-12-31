package com.pard.pree_be.feedback.analysis.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.report.service.ReportService;
import com.pard.pree_be.practice.entity.Practice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepo analysisRepo;
    private final ReportRepo reportRepo;
    private final ReportService reportService;

    public Analysis analyzePractice(Practice practice) {
        // Simulated/hardcoded values for now
        double duration = 120.0; // Example duration
        double speechSpeed = 143.0; // Example speech speed (words per minute)
        double decibel = 68.0; // Example decibel average
        int fillerCount = 2; // Example filler count
        int blankCount = 1; // Example blank count
        int eyePercentage = 85; // Example eye-tracking percentage

        Analysis analysis = Analysis.builder()
                .practice(practice)
                .duration(duration)
                .speechSpeed(speechSpeed)
                .decibel(decibel)
                .fillerCount(fillerCount)
                .blankCount(blankCount)
                .eyePercentage(eyePercentage)
                .build();
        Analysis savedAnalysis = analysisRepo.save(analysis);

        generateReports(savedAnalysis);
        return savedAnalysis;
    }

    public void generateReports(Analysis analysis) {
        List<Report> reports = List.of(
                reportService.generateDurationReport(analysis.getDuration()),
                reportService.generateSpeechSpeedReport(analysis.getSpeechSpeed()),
                reportService.generateDecibelReport(analysis.getDecibel()),
                reportService.generateFillerReport(analysis.getFillerCount()),
                reportService.generateBlankReport(analysis.getBlankCount()),
                reportService.generateEyeTrackingReport(analysis.getEyePercentage())
        );

        reports.forEach(report -> report.setAnalysis(analysis));
        reportRepo.saveAll(reports);
    }
}
