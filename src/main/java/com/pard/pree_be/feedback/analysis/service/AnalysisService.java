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
import com.pard.pree_be.utils.TranscriptionProcessor;
import com.pard.pree_be.utils.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepo analysisRepo;
    private final ReportRepo reportRepo;
    private final ReportService reportService;
    private final TranscriptionService transcriptionService;
    private final TranscriptionProcessor transcriptionProcessor;
    private final RecentScoresRepo recentScoresRepo;

    private final PresentationRepo presentationRepo;


    /**
     * Generates an analysis for the given practice and creates reports for each feature.
     */
    public Analysis analyzePractice(Practice practice, String audioFileKey, String bucketName) {
        // Get the transcription text from AWS Transcribe
        String transcriptionText = transcriptionService.transcribeAudio(bucketName, audioFileKey);
        System.out.println("Transcription Text: " + transcriptionText);

        // Process the transcription for filler count, blank count, and speech speed
        int fillerCount = transcriptionProcessor.countFillers(transcriptionText);
        int blankCount = transcriptionProcessor.countBlanks(transcriptionText, 3.0); // Example: blank threshold of 3 seconds
        double speechSpeed = transcriptionProcessor.calculateSpeechSpeed(transcriptionText, practice.getAnalyses().get(0).getDuration());

        // Retrieve the decibel and duration values from the first Analysis in the list
        double duration = practice.getAnalyses().get(0).getDuration();  // Get the duration from the analysis
        double decibel = practice.getAnalyses().get(0).getDecibel();    // Get the decibel from the analysis

        // Create and save Analysis entity
        Analysis analysis = Analysis.builder()
                .practice(practice)
                .duration(duration)
                .speechSpeed(speechSpeed)
                .decibel(decibel)
                .fillerCount(fillerCount)
                .blankCount(blankCount)
                .eyePercentage(practice.getEyePercentage()) // You can modify this or retrieve from a different source
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
        // Retrieve the associated presentation to get idealMin and idealMax
        Presentation presentation = analysis.getPractice().getPresentation();
        int idealMin = (int) presentation.getIdealMinTime(); // Assuming idealMinTime is stored in seconds
        int idealMax = (int) presentation.getIdealMaxTime(); // Assuming idealMaxTime is stored in seconds

        // Generate individual reports for each metric
        List<Report> reports = List.of(
                reportService.generateDurationReport(analysis.getDuration(), idealMin, idealMax),
                reportService.generateSpeechSpeedReport(analysis.getSpeechSpeed(), analysis.getDuration()), // Pass both arguments
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
