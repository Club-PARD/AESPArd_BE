package com.pard.pree_be.practice.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.report.service.ReportService;
import com.pard.pree_be.practice.dto.PracticeDto;
import com.pard.pree_be.practice.dto.PracticeResponseDto;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.practice.repo.PracticeRepo;
import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.presentation.repo.PresentationRepo;
import com.pard.pree_be.utils.AudioAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepo practiceRepo;
    private final PresentationRepo presentationRepo;
    private final AnalysisRepo analysisRepo;
    private final ReportRepo reportRepo;
    private final ReportService reportService;

    /**
     * Add a new practice to an existing presentation.
     */
    public PracticeResponseDto addPracticeToExistingPresentation(UUID presentationId, String videoKey, int eyePercentage, MultipartFile audioFile) throws IOException {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found"));

        long practiceCount = practiceRepo.countByPresentation_PresentationId(presentationId) + 1;
        String practiceName = practiceCount + "번째 연습";
        String audioFilePath = saveAudioFile(audioFile);

        Practice practice = Practice.builder()
                .practiceName(practiceName)
                .presentation(presentation)
                .practiceCreatedAt(LocalDateTime.now())
                .videoKey(videoKey)
                .eyePercentage(eyePercentage)
                .audioFile(audioFile.getBytes())
                .audioFilePath(audioFilePath)
                .build();

        practiceRepo.save(practice);

        performAnalysis(practice, eyePercentage);

        return PracticeResponseDto.builder()
                .id(practice.getId())
                .practiceName(practice.getPracticeName())
                .practiceCreatedAt(LocalDateTime.now())
                .totalScore(practice.getTotalScore())
                .videoKey(practice.getVideoKey())
                .build();
    }

    // 가장 최근 밢표에 새로운 연습 추가하기 👍
    public PracticeResponseDto addPracticeToMostRecentlyUpdatedPresentation(
            UUID userId, String videoKey, int eyePercentage, MultipartFile audioFile) throws IOException {

        // Fetch the most recently updated presentations for the user
        List<Presentation> presentations = presentationRepo.findMostRecentlyUpdatedByUser(userId);

        if (presentations.isEmpty()) {
            throw new IllegalArgumentException("No recent presentation found for the user.");
        }

        // Select the first (most recent) presentation
        Presentation mostRecent = presentations.get(0);

        // Calculate the practice count for naming
        long practiceCount = practiceRepo.countByPresentation_PresentationId(mostRecent.getPresentationId()) + 1;
        String practiceName = practiceCount + " 번째 연습";

        // Save the audio file and get its path
        String audioFilePath = saveAudioFile(audioFile);

        // Build the new Practice entity
        Practice practice = Practice.builder()
                .practiceName(practiceName)
                .presentation(mostRecent)
                .practiceCreatedAt(LocalDateTime.now())
                .videoKey(videoKey)
                .eyePercentage(eyePercentage)
                .audioFilePath(audioFilePath)
                .build();

        // Save the practice entity
        practiceRepo.save(practice);

        // Perform analysis on the practice
        performAnalysis(practice, eyePercentage);

        // Return the response DTO
        return PracticeResponseDto.builder()
                .id(practice.getId())
                .practiceName(practice.getPracticeName())
                .practiceCreatedAt(practice.getPracticeCreatedAt())
                .totalScore(practice.getTotalScore())
                .videoKey(practice.getVideoKey())
                .build();
    }


    public List<PracticeDto> getPracticesByPresentationId(UUID presentationId) {
        return practiceRepo.findByPresentation_PresentationIdOrderByPracticeCreatedAtDesc(presentationId).stream()
                .map(practice -> PracticeDto.builder()
                        .id(practice.getId())
                        .practiceName(practice.getPracticeName())
                        .practiceCreatedAt(practice.getPracticeCreatedAt())
                        .totalScore(practice.getTotalScore())
                        .videoKey(practice.getVideoKey())
                        .analysisId(practice.getAnalyses() != null && !practice.getAnalyses().isEmpty()
                                ? practice.getAnalyses().get(0).getId()
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

    public List<PracticeDto> getRecentPractice(UUID presentationId) {
        return practiceRepo.findTop1ByPresentation_PresentationIdOrderByPracticeCreatedAtDesc(presentationId).stream()
                .map(practice -> PracticeDto.builder()
                        .id(practice.getId())
                        .practiceName(practice.getPracticeName())
                        .practiceCreatedAt(practice.getPracticeCreatedAt())
                        .totalScore(practice.getTotalScore())
                        .videoKey(practice.getVideoKey())
                        .analysisId(practice.getAnalyses() != null && !practice.getAnalyses().isEmpty()
                                ? practice.getAnalyses().get(0).getId()
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Retrieve recent practice scores for a presentation.
     */
    public List<Integer> getRecentPracticeScores(UUID presentationId) {
        return practiceRepo.findTop5ByPresentation_PresentationIdOrderByPracticeCreatedAtDesc(presentationId).stream()
                .map(Practice::getTotalScore)
                .collect(Collectors.toList());
    }


    /**
     * Perform analysis on the practice.
     */
    private void performAnalysis(Practice practice, int eyePercentage) {
        try {
            double decibel = AudioAnalyzer.calculateAverageDecibel(practice.getAudioFilePath());
            double duration = getAudioDuration(practice.getAudioFilePath());

            int fillerCount = 3;
            int blankCount = 2;
            double speechSpeed = 143.0;

            Analysis analysis = Analysis.builder()
                    .practice(practice)
                    .decibel(decibel)
                    .duration(duration)
                    .eyePercentage(eyePercentage)
                    .fillerCount(fillerCount)
                    .blankCount(blankCount)
                    .speechSpeed(speechSpeed)
                    .build();

            analysisRepo.save(analysis);

            generateReportsForAnalysis(analysis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze practice: " + e.getMessage(), e);
        }
    }


    /**
     * Generate reports for the analysis.
     */
    private void generateReportsForAnalysis(Analysis analysis) {
        List<Report> reports = List.of(
                reportService.generateDurationReport(analysis.getDuration()),
                reportService.generateSpeechSpeedReport(analysis.getSpeechSpeed()),
                reportService.generateDecibelReport(analysis.getDecibel()),
                reportService.generateFillerReport(analysis.getFillerCount()),
                reportService.generateBlankReport(analysis.getBlankCount()),
                reportService.generateEyeTrackingReport(analysis.getEyePercentage())
        );

        reports.forEach(report -> {
            report.setAnalysis(analysis);
            reportRepo.save(report);
        });
    }

    /**
     * Calculate audio duration.
     */
    private double getAudioDuration(String audioFilePath) throws Exception {
        File audioFile = new File(audioFilePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        return (frames + 0.0) / format.getFrameRate();
    }

    /**
     * Save audio file to disk.
     */
    private String saveAudioFile(MultipartFile audioFile) throws IOException {
        Path audioPath = Paths.get("uploads");

        if (!Files.exists(audioPath)) {
            Files.createDirectories(audioPath);
        }
        String audioFileName = UUID.randomUUID() + ".wav";
        Path filePath = audioPath.resolve(audioFileName);
        Files.write(filePath, audioFile.getBytes());
        return filePath.toString();
    }

}
