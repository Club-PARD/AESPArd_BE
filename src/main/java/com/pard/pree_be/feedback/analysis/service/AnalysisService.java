package com.pard.pree_be.feedback.analysis.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.recentscores.entity.RecentScores;
import com.pard.pree_be.feedback.recentscores.repo.RecentScoresRepo;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.report.service.ReportService;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.presentation.repo.PresentationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepo analysisRepo;
    private final ReportRepo reportRepo;
    private final ReportService reportService;
    private final RecentScoresRepo recentScoresRepo;
    private final PresentationRepo presentationRepo;

    /**
     * Generates an analysis for the given practice and creates reports for each feature.
     */
    public Analysis analyzePractice(Practice practice) {
        // Simulate or retrieve metric values
        double duration = 120.0; // Example: Duration in seconds
        double speechSpeed = 143.0; // Example: Words per minute
        double decibel = 68.0; // Example: Average decibel
        int fillerCount = 3; // Example: Filler count
        int blankCount = 2; // Example: Blank count
        int eyePercentage = 85; // Example: Eye-tracking percentage

        // Create and save Analysis entity
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

        // Generate reports for all metrics
        generateReports(savedAnalysis);
        return savedAnalysis;
    }

    /**
     * Generates reports for all features and links them to the analysis.
     */
    public void generateReports(Analysis analysis) {
        // Generate individual reports for each metric
        List<Report> reports = List.of(
                reportService.generateDurationReport(analysis.getDuration()),
                reportService.generateSpeechSpeedReport(analysis.getSpeechSpeed()),
                reportService.generateDecibelReport(analysis.getDecibel()),
                reportService.generateFillerReport(analysis.getFillerCount()),
                reportService.generateBlankReport(analysis.getBlankCount()),
                reportService.generateEyeTrackingReport(analysis.getEyePercentage())
        );

        // Save reports and update recent scores for each metric
        reports.forEach(report -> {
            report.setAnalysis(analysis); // Link report to analysis
            updateRecentScores(report.getName(), report.getScore()); // Update recent scores
        });

        reportRepo.saveAll(reports); // Save all reports in bulk

        // Calculate the overall total score as the sum of weighted scores
        int overallScore = reports.stream()
                .mapToInt(Report::getTotalScore) // Weighted scores
                .sum();

        // Update the analysis with the calculated total score
        analysis.setTotalScore(overallScore);
        analysisRepo.save(analysis); // Save updated analysis

    }

    private void updateRecentScores(String metricName, int score) {
        RecentScores recentScores = recentScoresRepo.findByMetricName(metricName)
                .orElseGet(() -> RecentScores.builder().metricName(metricName).build());
        recentScores.addScore(score); // Update scores
        recentScoresRepo.save(recentScores); // Save updated scores


    }





}
