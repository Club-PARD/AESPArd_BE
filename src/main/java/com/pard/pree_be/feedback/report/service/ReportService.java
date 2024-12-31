package com.pard.pree_be.feedback.report.service;

import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.scoring.MetricStandard;
import com.pard.pree_be.feedback.scoring.ScoringStandards;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    /**
     * Generate report for duration metric.
     */
    public Report generateDurationReport(double duration) {
        MetricStandard standard = ScoringStandards.getStandard("duration");
        String feedbackMessage;
        int score;

        if (duration < standard.getIdealMinValue()) {
            double diff = standard.getIdealMinValue() - duration;
            feedbackMessage = String.format("%.1f초가 부족해요, 핵심 내용을 더 자세하게 말해볼까요?", diff);
            score = 100 - (int) (diff / standard.getPenaltyStep()) * (int) standard.getPenaltyAmount();
        } else if (duration > standard.getIdealMaxValue()) {
            double diff = duration - standard.getIdealMaxValue();
            feedbackMessage = String.format("%.1f초가 초과되었어요, 내용을 더 간략하게 줄여볼까요?", diff);
            score = 100 - (int) (diff / standard.getPenaltyStep()) * (int) standard.getPenaltyAmount();
        } else {
            feedbackMessage = "완벽한 시간 관리입니다! 잘하셨습니다.";
            score = 100;
        }

        return buildReport("duration", duration, score, feedbackMessage);
    }

    /**
     * Generate report for speech speed metric.
     */
    public Report generateSpeechSpeedReport(double speechSpeed) {
        MetricStandard standard = ScoringStandards.getStandard("speechSpeed");
        int score = 100;
        String feedbackMessage;

        if (speechSpeed < standard.getIdealMinValue()) {
            int penaltySteps = (int) Math.ceil((standard.getIdealMinValue() - speechSpeed) / standard.getPenaltyStep());
            score -= penaltySteps * (int) standard.getPenaltyAmount();
            feedbackMessage = penaltySteps > 4 ? "꽤 느린 편이에요. 조금만 빠르게 말해볼까요?" : "조금 느린 편이에요. 조금만 빠르게 말해볼까요?";
        } else if (speechSpeed > standard.getIdealMaxValue()) {
            int penaltySteps = (int) Math.ceil((speechSpeed - standard.getIdealMaxValue()) / standard.getPenaltyStep());
            score -= penaltySteps * (int) standard.getPenaltyAmount();
            feedbackMessage = penaltySteps > 4 ? "꽤 빠른 편이에요. 조금만 천천히 말해볼까요?" : "조금 빠른 편이에요. 조금만 천천히 말해볼까요?";
        } else {
            feedbackMessage = "완벽한 속도에요! 이 속도를 유지하세요.";
        }

        score = Math.max(score, 0);
        return buildReport("speechSpeed", speechSpeed, score, feedbackMessage);
    }

    /**
     * Generate report for decibel metric.
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

        score = Math.max(score, 0);
        return buildReport("decibel", decibel, score, feedbackMessage);
    }

    /**
     * Generate report for filler count metric.
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

        score = Math.max(score, 0);
        return buildReport("fillers", fillerCount, score, feedbackMessage);
    }

    /**
     * Generate report for blank count metric.
     */
    public Report generateBlankReport(int blankCount) {
        MetricStandard standard = ScoringStandards.getStandard("blanks");
        String feedbackMessage;
        int score = 100;

        if (blankCount == 0) {
            feedbackMessage = "한 번도 없었어요!";
        } else if (blankCount <= 3) {
            feedbackMessage = "조금만 줄이면 발표 흐름이 더 매끄러워질 거예요.";
            score -= blankCount * 2;
        } else {
            feedbackMessage = "너무 많아요. 발표 내용을 더 숙지해보세요.";
            score -= blankCount * 5;
        }

        score = Math.max(score, 0);
        return buildReport("blanks", blankCount, score, feedbackMessage);
    }

    /**
     * Generate report for eye tracking percentage metric.
     */
    public Report generateEyeTrackingReport(int eyePercentage) {
        String feedbackMessage;
        int score;

        if (eyePercentage >= 80) {
            feedbackMessage = "훌륭합니다! 실전에서도 관객과의 소통이 중요해요.";
            score = 100;
        } else if (eyePercentage >= 70) {
            feedbackMessage = "관객을 바라보는 습관을 조금만 더 길러보세요!";
            score = 100 - (80 - eyePercentage) * 5;
        } else {
            feedbackMessage = "관객을 바라보는 습관이 부족해요! 더 많은 연습이 필요해요.";
            score = 100 - (80 - eyePercentage) * 5;
        }

        score = Math.max(score, 0);
        return buildReport("eyeTracking", eyePercentage, score, feedbackMessage);
    }

    /**
     * Helper method to build a report.
     */
    private Report buildReport(String name, double counter, int score, String feedbackMessage) {
        return Report.builder()
                .name(name)
                .counter(counter)
                .score(score)
                .feedbackMessage(feedbackMessage)
                .build();
    }
}
