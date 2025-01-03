package com.pard.pree_be.feedback.analysis.controller;

import com.pard.pree_be.feedback.analysis.service.AnalysisService;
import com.pard.pree_be.feedback.recentscores.dto.RecentScoresDto;
import com.pard.pree_be.feedback.recentscores.entity.RecentScores;
import com.pard.pree_be.feedback.recentscores.repo.RecentScoresRepo;
import com.pard.pree_be.utils.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final RecentScoresRepo recentScoresRepo;

    /**
     * Endpoint to retrieve the most recent scores for all metrics.
     *
     * @return A list of RecentScoresDto containing the metric name, recent scores, and average.
     */
    @GetMapping("/recent-scores")
    public ResponseEntity<List<RecentScoresDto>> getRecentScores() {
        List<RecentScores> recentScores = recentScoresRepo.findAll();
        List<RecentScoresDto> response = recentScores.stream()
                .map(score -> new RecentScoresDto(score.getMetricName(), score.getScores(), score.getAverage()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }


    @RestController
    @RequestMapping("/transcribe")
    public class TranscriptionController {

        private final TranscriptionService transcriptionService;

        public TranscriptionController(TranscriptionService transcriptionService) {
            this.transcriptionService = transcriptionService;
        }

        @PostMapping("/audio")
        public ResponseEntity<String> transcribe(@RequestParam String bucketName, @RequestParam String audioFileKey) {
            String result = transcriptionService.transcribeAudio(bucketName, audioFileKey);
            return ResponseEntity.ok(result);  // Send the transcript URL or other result
        }
    }


}
