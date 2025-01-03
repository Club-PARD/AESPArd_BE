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
import com.pard.pree_be.utils.S3Service;
import com.pard.pree_be.utils.TranscriptionProcessor;
import com.pard.pree_be.utils.TranscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private final S3Service s3Service;
    private final TranscriptionService transcriptionService;
    private final TranscriptionProcessor transcriptionProcessor;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;


    /**
     * Add a new practice to an existing presentation.
     */
    public PracticeResponseDto addPracticeToExistingPresentation(UUID presentationId, String videoKey, int eyePercentage, MultipartFile audioFile) throws IOException {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found"));

        int idealMin = (int) presentation.getIdealMinTime();
        int idealMax = (int) presentation.getIdealMaxTime();

        long practiceCount = practiceRepo.countByPresentation_PresentationId(presentationId) + 1;
        String practiceName = practiceCount + "번째 연습";

        Practice practice = Practice.builder()
                .practiceName(practiceName)
                .presentation(presentation)
                .practiceCreatedAt(LocalDateTime.now())
                .videoKey(videoKey)
                .eyePercentage(eyePercentage)
                .build();

        practiceRepo.save(practice);

        String audioFilePath = saveAudioFile(audioFile, practice.getId());
        practice.setAudioFilePath(audioFilePath);
        practiceRepo.save(practice);

        performAnalysis(practice, eyePercentage, idealMin, idealMax);

        return PracticeResponseDto.builder()
                .id(practice.getId())
                .practiceName(practice.getPracticeName())
                .practiceCreatedAt(practice.getPracticeCreatedAt())
                .totalScore(practice.getTotalScore())
                .presentationTotalScore(presentation.getTotalScore())
                .videoKey(practice.getVideoKey())
                .build();
    }


    // 가장 최근 밢표에 새로운 연습 추가하기 👍
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

        // Retrieve idealMin and idealMax from the presentation
        int idealMin = (int) mostRecent.getIdealMinTime(); // Get the ideal minimum time
        int idealMax = (int) mostRecent.getIdealMaxTime(); // Get the ideal maximum time

        // Calculate the practice count for naming
        long practiceCount = practiceRepo.countByPresentation_PresentationId(mostRecent.getPresentationId()) + 1;
        String practiceName = practiceCount + " 번째 연습";

        // Create and save the practice first
        Practice practice = Practice.builder()
                .practiceName(practiceName)
                .presentation(mostRecent)
                .practiceCreatedAt(LocalDateTime.now())
                .videoKey(videoKey)
                .eyePercentage(eyePercentage)
                .build();

        practiceRepo.save(practice); // Save to generate the practice ID

        // Save the audio file to S3 using the generated practiceId
        String audioFilePath = saveAudioFile(audioFile, practice.getId());
        practice.setAudioFilePath(audioFilePath);
        practiceRepo.save(practice); // Update with the audio file path

        // Perform analysis on the uploaded audio
        performAnalysis(practice, eyePercentage, idealMin, idealMax);

        // Return the response DTO
        return PracticeResponseDto.builder()
                .id(practice.getId())
                .practiceName(practice.getPracticeName())
                .practiceCreatedAt(practice.getPracticeCreatedAt())
                .totalScore(practice.getTotalScore())
                .videoKey(practice.getVideoKey())
                .build();
    }


    public Analysis processAudio(MultipartFile audioFile, Long practiceId, int eyePercentage, int idealMin, int idealMax) throws Exception {
        Practice practice = practiceRepo.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("Practice not found"));

        String audioFilePath = s3Service.upload(audioFile, "audio-files", practiceId);
        double decibel = AudioAnalyzer.calculateAverageDecibel(audioFile.getInputStream()) + 95;
        double duration = calculateDuration(audioFile.getInputStream());
        String transcriptionJson = transcriptionService.transcribeAudio(bucketName, audioFilePath);

        double speechSpeed = transcriptionProcessor.calculateSpeechSpeed(transcriptionJson, duration);
        int fillerCount = transcriptionProcessor.countFillers(transcriptionJson);
        int blankCount = transcriptionProcessor.countBlanks(transcriptionJson, 3.0);

        return analyzeAndSaveMetrics(practice, decibel, duration, speechSpeed, fillerCount, blankCount, eyePercentage, idealMin, idealMax);
    }



    private Analysis analyzeAndSaveMetrics(Practice practice, double decibel, double duration, double speechSpeed, int fillerCount, int blankCount, int eyePercentage, int idealMin, int idealMax) {
        int roundedDecibel = (int) Math.round(decibel);
        int roundedDuration = (int) Math.round(duration);
        int roundedSpeechSpeed = (int) Math.round(speechSpeed);

        Analysis analysis = Analysis.builder()
                .practice(practice)
                .decibel(roundedDecibel)
                .duration(roundedDuration)
                .speechSpeed(roundedSpeechSpeed)
                .fillerCount(fillerCount)
                .blankCount(blankCount)
                .eyePercentage(eyePercentage)
                .build();

        analysisRepo.save(analysis);
        generateReportsForAnalysis(analysis, idealMin, idealMax);

        return analysis;
    }




    /**
     * Calculate the duration of an audio file using its InputStream.
     */
    private double calculateDuration(InputStream audioInputStream) throws Exception {
        // Wrap the InputStream for buffering
        InputStream bufferedStream = new BufferedInputStream(audioInputStream);

        // Convert to AudioInputStream for duration analysis
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedStream);

        // Get format details
        AudioFormat format = audioStream.getFormat();
        long frames = audioStream.getFrameLength();

        // Duration in seconds = Total frames / Frame rate
        return (double) frames / format.getFrameRate();
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
        return practiceRepo.findTop5ByPresentation_PresentationIdOrderByPracticeCreatedAtAsc(presentationId).stream()
                .map(Practice::getTotalScore)
                .collect(Collectors.toList());
    }


    /**
     * Perform analysis on the practice.
     */
    private void performAnalysis(Practice practice, int eyePercentage, int idealMin, int idealMax) {
        try {
            URL s3Url = new URL(practice.getAudioFilePath());
            HttpURLConnection connection = (HttpURLConnection) s3Url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            try (InputStream s3InputStream = connection.getInputStream()) {
                double decibel = AudioAnalyzer.calculateAverageDecibel(s3InputStream) + 100;

                String transcriptionJson = transcriptionService.transcribeAudio(bucketName, practice.getAudioFilePath());

                double duration = transcriptionProcessor.calculateDurationFromJson(transcriptionJson);
                double speechSpeed = transcriptionProcessor.calculateSpeechSpeedFromJson(transcriptionJson, duration);
                int fillerCount = transcriptionProcessor.countFillersFromJson(transcriptionJson);
                int blankCount = transcriptionProcessor.countBlanksFromJson(transcriptionJson, 3.0);

                analyzeAndSaveMetrics(practice, decibel, duration, speechSpeed, fillerCount, blankCount, eyePercentage, idealMin, idealMax);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze practice: " + e.getMessage(), e);
        }
    }

    public void processTranscriptionAndScore(String transcriptionJson, double duration) {
        // Use TranscriptionProcessor to calculate SPM
        String transcriptionText = transcriptionProcessor.extractTextFromJson(transcriptionJson); // Parse JSON for text
        double speechSPM = transcriptionProcessor.calculateSpeechSPM(transcriptionText, duration);

        // Pass both speechSPM and duration to generateSpeechSpeedReport
        Report speechSpeedReport = reportService.generateSpeechSpeedReport(speechSPM, duration);
        reportRepo.save(speechSpeedReport);
    }







    /**
     * Generate reports for the analysis.
     */
    private void generateReportsForAnalysis(Analysis analysis, int idealMin, int idealMax) {
        List<Report> reports = List.of(
                reportService.generateDurationReport(analysis.getDuration(), idealMin, idealMax),
                reportService.generateSpeechSpeedReport(analysis.getSpeechSpeed(), analysis.getDuration()), // Pass both arguments
                reportService.generateDecibelReport(analysis.getDecibel()),
                reportService.generateFillerReport(analysis.getFillerCount()),
                reportService.generateBlankReport(analysis.getBlankCount()),
                reportService.generateEyeTrackingReport(analysis.getEyePercentage())
        );

        reports.forEach(report -> {
            report.setAnalysis(analysis);
            reportRepo.save(report);
        });

        int totalScore = reports.stream()
                .mapToInt(Report::getTotalScore)
                .sum();

        Practice practice = analysis.getPractice();
        practice.setTotalScore(totalScore);
        practiceRepo.save(practice);
    }


    /**
     * Save audio file to disk.
     */
    private String saveAudioFile(MultipartFile audioFile, Long practiceId) throws IOException {
        return s3Service.upload(audioFile, "audio-files", practiceId);
    }



    public void deletePractice(Long practiceId) {
        Practice practice = practiceRepo.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("Practice not found"));
        practiceRepo.delete(practice);
    }

    public void batchDeletePractices(List<Long> practiceIds) {
        List<Practice> practices = practiceRepo.findAllById(practiceIds);
        if (practices.isEmpty()) {
            throw new IllegalArgumentException("No practices found for the given IDs");
        }
        practiceRepo.deleteAll(practices);
    }

    public void updatePracticeName(Long practiceId, String newName) {
        Practice practice = practiceRepo.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("Practice not found"));

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Practice name cannot be null or empty");
        }

        practice.setPracticeName(newName);
        practiceRepo.save(practice);
    }



}
