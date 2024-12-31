package com.pard.pree_be.feedback.report.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.analysis.service.AnalysisService;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.report.dto.ReportDto;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.scoring.MetricStandard;
import com.pard.pree_be.feedback.scoring.ScoringStandards;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.practice.repo.PracticeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepo reportRepo;
    private final PracticeRepo practiceRepo;
    private final AnalysisRepo analysisRepo;
    private final AnalysisService analysisService;

    public void generateTestReport(UUID practiceId) {

        Practice practice = practiceRepo.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("Practice not found for ID: " + practiceId));

        // Generate analysis
        Analysis analysis = analysisService.analyzePractice(practice);

        // Generate reports
        analysisService.generateReports(analysis);
    }

    public void generateReports(Analysis analysis) {
        List<Report> reports = List.of(
                createReport("duration", analysis.getDuration()),
                createReport("speechSpeed", analysis.getSpeechSpeed()),
                createReport("decibel", analysis.getDecibel()),
                createReport("fillers", analysis.getFillerCount()),
                createReport("blanks", analysis.getBlankCount()),
                createReport("eyeTracking", analysis.getEyePercentage()));
        reportRepo.saveAll(reports);
    }

    private Report createReport(String metricName, double counter) {
        MetricStandard standard = ScoringStandards.getStandard(metricName);
        int score = calculateScore(standard, counter);
        String feedback = generateFeedback(standard, counter);

        return Report.builder()
                .name(metricName)
                .counter(counter)
                .score(score)
                .feedbackMessage(feedback)
                .build();
    }

    private int calculateScore(MetricStandard standard, double counter) {
        // Calculate the score based on the metric standard ✨✨✨✨
        if (counter >= standard.getIdealMinValue() && counter <= standard.getIdealMaxValue()) {
            return 100; // Perfect score
        }

        double difference = Math.abs(counter - (counter < standard.getIdealMinValue()
                ? standard.getIdealMinValue()
                : standard.getIdealMaxValue()));

        return Math.max(0, 100 - (int) (difference / standard.getPenaltyStep()) * (int) standard.getPenaltyAmount());
    }

    public List<ReportDto> getReportsForAnalysis(UUID analysisId) {
        return reportRepo.findByAnalysisId(analysisId).stream()
                .map(report -> ReportDto.builder()
                        .name(report.getName())
                        .counter(report.getCounter())
                        .score(report.getScore())
                        .feedbackMessage(report.getFeedbackMessage())
                        .build())
                .collect(Collectors.toList());
    }

    private String generateFeedback(MetricStandard standard, double counter) {
        // Generate feedback based on the metric value ✨✨✨✨
        if (counter >= standard.getIdealMinValue() && counter <= standard.getIdealMaxValue()) {
            return standard.getMessagePerfect();
        }

        if (counter < standard.getIdealMinValue()) {
            return standard.getMessageNearIdeal() != null ? standard.getMessageNearIdeal()
                    : standard.getMessageSevereIssue();
        }

        return standard.getMessageSevereIssue();
    }
}
