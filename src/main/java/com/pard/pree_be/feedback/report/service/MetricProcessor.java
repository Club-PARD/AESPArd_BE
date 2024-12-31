package com.pard.pree_be.feedback.report.service;

import com.pard.pree_be.feedback.report.entity.Report;

import com.pard.pree_be.feedback.scoring.MetricStandard;

public class MetricProcessor {

    public static Report processMetric(String name, double counter, MetricStandard standard) {
        int score = 100;
        String feedbackMessage = "";

        // Process based on metric thresholds
        if (name.equals("duration")) {
            feedbackMessage = processDuration(counter, standard);
            score = calculateScore(counter, standard);
        } else if (name.equals("speechSpeed")) {
            feedbackMessage = processSpeechSpeed(counter, standard);
            score = calculateScore(counter, standard);
        } else if (name.equals("decibel")) {
            feedbackMessage = processDecibel(counter, standard);
            score = calculateScore(counter, standard);
        }
        // Add other metrics similarly...

        return Report.builder()
                .name(name)
                .counter((int) counter)
                .score(Math.max(score, 0)) // Ensure score doesn't drop below 0
                .feedbackMessage(feedbackMessage)
                .build();
    }

    private static String processDuration(double duration, MetricStandard standard) {
        if (duration < standard.getIdealMinValue()) {
            double diff = standard.getIdealMinValue() - duration;
            return String.format("%.1f초가 부족해요, 핵심 내용을 더 자세하게 말해볼까요?", diff);
        } else if (duration > standard.getIdealMaxValue()) {
            double diff = duration - standard.getIdealMaxValue();
            return String.format("%.1f초가 초과되었어요, 내용을 더 간략하게 줄여볼까요?", diff);
        } else {
            return "완벽한 시간 관리입니다! 잘하셨습니다.";
        }
    }

    private static String processSpeechSpeed(double speechSpeed, MetricStandard standard) {
        if (speechSpeed < standard.getIdealMinValue()) {
            return "조금 느린 편이에요. 조금만 빠르게 말해볼까요?";
        } else if (speechSpeed > standard.getIdealMaxValue()) {
            return "조금 빠른 편이에요. 조금만 천천히 말해볼까요?";
        } else {
            return "완벽한 속도에요! 이 속도를 유지하세요.";
        }
    }

    private static String processDecibel(double decibel, MetricStandard standard) {
        if (decibel < standard.getIdealMinValue()) {
            return "너무 작은 편이에요. 목소리를 더 크게 내보세요!";
        } else if (decibel > standard.getIdealMaxValue()) {
            return "너무 큰 편이에요. 목소리를 더 작게 내보세요!";
        } else {
            return "발표에 딱 맞는 목소리 크기였습니다!";
        }
    }

    private static int calculateScore(double counter, MetricStandard standard) {
        int penalty = 0;

        if (counter < standard.getIdealMinValue()) {
            penalty = (int) ((standard.getIdealMinValue() - counter) / standard.getPenaltyStep()) * (int) standard.getPenaltyAmount();
        } else if (counter > standard.getIdealMaxValue()) {
            penalty = (int) ((counter - standard.getIdealMaxValue()) / standard.getPenaltyStep()) * (int) standard.getPenaltyAmount();
        }

        return 100 - penalty;
    }
}
