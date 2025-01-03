package com.pard.pree_be.feedback.scoring;

import java.util.HashMap;
import java.util.Map;

public class ScoringStandards {

        private static final Map<String, MetricStandard> standards = new HashMap<>();

        static {
                standards.put("duration", new MetricStandard(
                        60, 120, 10, 5,
                        "완벽한 시간 관리입니다! 잘하셨습니다.",
                        "%.1f초가 초과되었어요, 내용을 더 간략하게 줄여볼까요?",
                        "%.1f초가 부족해요, 핵심 내용을 더 자세하게 말해볼까요?",
                        "시간이 너무 부족하거나 초과했습니다. 적절한 시간 배분을 해보세요."
                ));
                standards.put("speechSpeed", new MetricStandard(
                        130, 150, 5, 7,
                        "완벽한 속도에요! 이 속도를 유지하세요.",
                        "조금 빠른 편이에요. 조금만 천천히 말해볼까요?",
                        "조금 느린 편이에요. 조금만 빠르게 말해볼까요?",
                        "속도가 너무 빠르거나 느립니다. 발표 리듬을 개선해보세요."
                ));
                standards.put("decibel", new MetricStandard(
                        65, 75, 1, 5,
                        "발표에 딱 맞는 목소리 크기였습니다!",
                        "너무 큰 편이에요. 목소리를 더 작게 내보세요!",
                        "너무 작은 편이에요. 목소리를 더 크게 내보세요!",
                        "목소리 크기가 적절하지 않습니다. 균형을 맞춰보세요."
                ));
                standards.put("fillers", new MetricStandard(
                        0, 0, 1, 5,
                        "한 번도 없었어요!",
                        "조금만 줄이면 더 깔끔한 발표가 될 수 있어요.",
                        "의식적으로 발화 지연 표현을 고치려고 노력해보세요!",
                        "발화 지연 표현을 줄이고 명확하게 말해보세요."
                ));
                standards.put("blanks", new MetricStandard(
                        0, 0, 3, 2,
                        "한 번도 없었어요!",
                        "조금만 줄이면 발표 흐름이 더 매끄러워질 거예요.",
                        "너무 많아요. 발표 내용을 더 숙지해보세요.",
                        "발표 흐름이 끊기지 않도록 연습해보세요."
                ));
                standards.put("eyeTracking", new MetricStandard(
                        80, 100, 1, 5,
                        "훌륭합니다! 실전에서도 관객과의 소통이 중요해요.",
                        "관객을 바라보는 습관을 조금만 더 길러보세요!",
                        "관객을 바라보는 습관이 부족해요! 더 많은 연습이 필요해요.",
                        "관객과의 시선 교환을 더 연습해보세요."
                ));
        }

        public static MetricStandard getStandard(String metricName) {
                if (metricName == null) {
                        throw new IllegalArgumentException("Metric name cannot be null");
                }
                return standards.getOrDefault(metricName, null);
        }
}
