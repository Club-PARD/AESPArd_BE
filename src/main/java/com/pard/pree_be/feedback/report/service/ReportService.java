package com.pard.pree_be.feedback.report.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.scoring.MetricStandard;
import com.pard.pree_be.feedback.scoring.ScoringStandards;
import com.pard.pree_be.presentation.entity.Presentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepo reportRepo;
    private final AnalysisRepo analysisRepo;

    public ReportService(ReportRepo reportRepo, AnalysisRepo analysisRepo) {
        this.reportRepo = reportRepo;
        this.analysisRepo = analysisRepo;
    }

    /**
     * Generate reports for all metrics in an analysis and save them.
     */
    public void generateReports(Analysis analysis) {
        // Retrieve the associated presentation
        Presentation presentation = analysis.getPractice().getPresentation();
        int idealMin = (int) presentation.getIdealMinTime(); // Fetch ideal min time
        int idealMax = (int) presentation.getIdealMaxTime(); // Fetch ideal max time

        // Generate individual reports for each metric
        List<Report> reports = List.of(
                generateDurationReport(analysis.getDuration(), idealMin, idealMax),
                generateSpeechSpeedReport(analysis.getSpeechSpeed()),
                generateDecibelReport(analysis.getDecibel()),
                generateFillerReport(analysis.getFillerCount()),
                generateBlankReport(analysis.getBlankCount()),
                generateEyeTrackingReport(analysis.getEyePercentage())
        );

        // Save reports and link them to the analysis
        reports.forEach(report -> {
            report.setAnalysis(analysis);
            reportRepo.save(report);
        });

        // Calculate total weighted score
        int totalScore = reports.stream()
                .mapToInt(Report::getTotalScore)
                .sum();

        // Update the analysis with the total score
        analysis.setTotalScore(totalScore);
        analysisRepo.save(analysis);
    }


    public Report generateDurationReport(double duration, int idealMin, int idealMax) {
        int score = 100;
        String feedbackMessage;

        if (duration < idealMin) {
            int diff = (int) (idealMin - duration);
            feedbackMessage = diff + "초가 부족해요. 내용을 조금 더 추가해보세요!";
            score -= (diff / 10) * 5;
        } else if (duration > idealMax) {
            int diff = (int) (duration - idealMax);
            feedbackMessage = diff + "초가 초과되었어요. 내용을 간략하게 줄여볼까요!";
            score -= (diff / 10) * 5;
        } else {
            feedbackMessage = "완벽한 시간 관리입니다! 잘하셨습니다.";
        }

        score = Math.max(score, 0); // Prevent negative scores

        return buildReport("duration", (int) duration, score, feedbackMessage);
    }





    /**
     * Generate a report for the speech speed metric.
     */
    public Report generateSpeechSpeedReport(double speechSpeedSPM) {
        // Define the ideal range for Korean speech speed in SPM
        int idealMin = 180;
        int idealMax = 250;
        int score = 100;
        String feedbackMessage;

        if (speechSpeedSPM < idealMin) {
            int diff = (int) (idealMin - speechSpeedSPM); // Difference in SPM
            feedbackMessage = diff + "꽤 느린 편이에요. 조금만 빠르게 말해볼까요?";
            score -= (diff / 10) * 5; // Deduct 5 points per 10 SPM below idealMin
        } else if (speechSpeedSPM > idealMax) {
            int diff = (int) (speechSpeedSPM - idealMax); // Difference in SPM
            feedbackMessage = diff + "꽤 빠른 편이에요. 조금만 천천히 말해볼까요?";
            score -= (diff / 10) * 5; // Deduct 5 points per 10 SPM above idealMax
        } else {
            feedbackMessage = "완벽한 속도에요! 이 속도를 유지하세요.";
        }

        // Ensure score is not negative
        score = Math.max(score, 0);

        return buildReport("speechSpeed", (int) speechSpeedSPM, score, feedbackMessage);
    }


    /**
     * Generate a report for the decibel metric.
     */
    public Report generateDecibelReport(double decibel) {
        MetricStandard standard = ScoringStandards.getStandard("decibel");
        String feedbackMessage;
        int score = 100;

        if (decibel < standard.getIdealMinValue()) {
            feedbackMessage = "너무 작은 편이에요. 목소리를 더 크게 내보세요!";
            score -= (int) ((standard.getIdealMinValue() - decibel) * standard.getPenaltyAmount());
        } else if (decibel > standard.getIdealMaxValue()) {
            feedbackMessage = "너무 큰 편이에요. 목소리를 더 작게 내보세요!";
            score -= (int) ((decibel - standard.getIdealMaxValue()) * standard.getPenaltyAmount());
        } else {
            feedbackMessage = "발표에 딱 맞는 목소리 크기였습니다!";
        }

        return buildReport("decibel", decibel, Math.max(score, 0), feedbackMessage);
    }

    /**
     * Generate a report for the filler count metric.
     */
    public Report generateFillerReport(int fillerCount) {
        MetricStandard standard = ScoringStandards.getStandard("fillers");
        String feedbackMessage;
        int score = 100 - fillerCount * (int) standard.getPenaltyAmount();

        if (fillerCount == 0) {
            feedbackMessage = "한 번도 없었어요!";
        } else if (fillerCount <= 4) {
            feedbackMessage = "조금만 줄이면 더 깔끔한 발표가 될 수 있어요.";
        } else {
            feedbackMessage = "의식적으로 발화 지연 표현을 고치려고 노력해보세요!";
        }

        return buildReport("fillers", fillerCount, Math.max(score, 0), feedbackMessage);
    }

    /**
     * Generate a report for the blank count metric.
     */
    public Report generateBlankReport(int blankCount) {
        MetricStandard standard = ScoringStandards.getStandard("blanks");
        String feedbackMessage;
        int score = 100;

        if (blankCount == 0) {
            feedbackMessage = "한 번도 없었어요!";
        } else if (blankCount <= 3) {
            feedbackMessage = "조금만 줄이면 발표 흐름이 더 매끄러워질 거예요.";
            score -= blankCount * 2; // Example: Deduct 2 points per blank within the range 3-5 seconds
        } else {
            feedbackMessage = "너무 많아요. 발표 내용을 더 숙지해보세요.";
            score -= blankCount * 5; // Example: Deduct 5 points per blank above 5 seconds
        }

        return buildReport("blanks", blankCount, Math.max(score, 0), feedbackMessage);
    }

    /**
     * Generate a report for the eye tracking percentage metric.
     */
    public Report generateEyeTrackingReport(int eyePercentage) {
        int score = eyePercentage >= 85 ? 100 : eyePercentage;
        String feedbackMessage = score == 100
                ? "훌륭합니다! 실전에서도 관객과의 소통이 중요해요."
                : "관객과의 시선을 조금 더 유지해보세요!";

        return buildReport("eyeTracking", eyePercentage, score, feedbackMessage);
    }


    private static final Map<String, Double> WEIGHTAGE_MAP = Map.of(
            "eyeTracking", 0.20,
            "blanks", 0.10,
            "decibel", 0.20,
            "fillers", 0.10,
            "speechSpeed", 0.20,
            "duration", 0.20
    );

    /**
     * Helper method to build a report.
     */
    private Report buildReport(String name, double counter, int score, String feedbackMessage) {
        double weightage = WEIGHTAGE_MAP.getOrDefault(name, 0.0); // Retrieve the weightage for the metric
        int weightedScore = (int) (score * weightage); // Calculate the weighted score

        return Report.builder()
                .name(name)
                .counter((int) counter)
                .score(score)
                .totalScore(weightedScore)
                .feedbackMessage(feedbackMessage)
                .build();
    }


}
