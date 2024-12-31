package com.pard.pree_be.feedback.scoring;

import java.util.Map;

public class ScoringStandards {

        public static final Map<String, MetricStandard> METRICS = Map.of(
                        "speechSpeed", new MetricStandard(130.0, 150.0, 5.0, 7.0,
                                        "완벽한 속도에요! 이 속도를 유지하세요.",
                                        "조금 느린 편이에요. 조금만 빠르게 말해볼까요?",
                                        "꽤 느린 편이에요. 조금만 빠르게 말해볼까요?",
                                        "꽤 빠른 편이에요. 조금만 천천히 말해볼까요?"),
                        "decibel", new MetricStandard(65.0, 75.0, 1.0, 5.0,
                                        "발표에 딱 맞는 목소리 크기였습니다!",
                                        "너무 작은 편이에요. 목소리를 더 크게 내보세요!",
                                        null,
                                        "너무 큰 편이에요. 목소리를 더 작게 내보세요!"),
                        "fillers", new MetricStandard(0.0, 0.0, 1.0, 5.0,
                                        "한 번도 없었어요!",
                                        "조금만 줄이면 더 깔끔한 발표가 될 수 있어요.",
                                        null,
                                        "의식적으로 발화 지연 표현을 고치려고 노력해보세요!"),
                        "blanks", new MetricStandard(0.0, 0.0, 1.0, 2.0,
                                        "한 번도 없었어요!",
                                        "조금만 줄이면 발표 흐름이 더 매끄러워질 거예요.",
                                        null,
                                        "너무 많아요. 발표 내용을 더 숙지해보세요."),
                        "eyeTracking", new MetricStandard(80.0, 100.0, 1.0, 5.0,
                                        "훌륭합니다! 실전에서도 관객과의 소통이 중요해요.",
                                        "관객을 바라보는 습관을 조금만 더 길러보세요!",
                                        null,
                                        "관객을 바라보는 습관이 부족해요! 더 많은 연습이 필요해요."));

        public static MetricStandard getStandard(String metricName) {
                return METRICS.get(metricName);
        }
}
