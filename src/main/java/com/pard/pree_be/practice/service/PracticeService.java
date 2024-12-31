package com.pard.pree_be.practice.service;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.analysis.service.AnalysisService;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.practice.dto.PracticeDto;
import com.pard.pree_be.practice.dto.PracticeRequestDto;
import com.pard.pree_be.practice.dto.PracticeResponseDto;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.practice.repo.PracticeRepo;
import com.pard.pree_be.presentation.dto.PresentationRequestDto;
import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.presentation.repo.PresentationRepo;
import com.pard.pree_be.user.entity.User;
import com.pard.pree_be.user.repo.UserRepo;
import com.pard.pree_be.utils.AudioAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sound.sampled.*;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepo practiceRepo;
    private final PresentationRepo presentationRepo;
    private final UserRepo userRepo;
    private final AnalysisRepo analysisRepo;
    private final ReportRepo reportRepo;

    public PracticeResponseDto addPracticeToExistingPresentation(UUID presentationId, String videoKey, int eyePercentage, MultipartFile audioFile) throws IOException {
        // Find the presentation
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found"));

        // Generate practice name
        long practiceCount = practiceRepo.countByPresentation_PresentationId(presentationId) + 1;
        String practiceName = practiceCount + "번째 연습";

        // Save audio file
        String audioFilePath = saveAudioFile(audioFile);


        // Create practice
        Practice practice = Practice.builder()
                .practiceName(practiceName)
                .presentation(presentation)
                .createdAt(LocalDateTime.now())
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
                .createdAt(practice.getCreatedAt())
                .totalScore(practice.getTotalScore())
                .videoKey(practice.getVideoKey())
                .build();
    }

    public Presentation addPracticeToRecentPresentation(UUID userId, String videoKey, int eyePercentage, MultipartFile audioFile) throws IOException {
        // Fetch the most recent presentation
        Presentation recentPresentation = presentationRepo.findTopByUser_UserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("No recent presentation found for the user."));

        // Create a new practice
        long practiceCount = practiceRepo.countByPresentation_PresentationId(recentPresentation.getPresentationId()) + 1;
        String practiceName = practiceCount + " 번째 연습";
        String audioFilePath = saveAudioFile(audioFile);

        Practice practice = Practice.builder()
                .practiceName(practiceName)
                .presentation(recentPresentation)
                .createdAt(LocalDateTime.now())
                .videoKey(videoKey)
                .eyePercentage(eyePercentage)
                .audioFilePath(audioFilePath)
                .build();

        practiceRepo.save(practice);

        // Perform analysis
        performAnalysis(practice, eyePercentage);

        // Add the practice to the presentation's practice list
        recentPresentation.getPractices().add(practice);

        // Increment the totalPractices field in the Presentation entity
        recentPresentation.incrementTotalPractices();

        // Save and return the updated presentation
        return presentationRepo.save(recentPresentation);
    }


    private double getAudioDuration(String audioFilePath) throws Exception {
        File audioFile = new File(audioFilePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        return (frames + 0.0) / format.getFrameRate();
    }


    private void performAnalysis(Practice practice, int eyePercentage) {
        try {
            // Analyze audio
            double decibel = AudioAnalyzer.calculateAverageDecibel(practice.getAudioFilePath());
            double duration = getAudioDuration(practice.getAudioFilePath()); // Calculate duration

            // Save analysis data
            Analysis analysis = Analysis.builder()
                    .practice(practice)
                    .decibel(decibel)
                    .duration(duration)
                    .eyePercentage(eyePercentage)
                    .build();

            analysisRepo.save(analysis);

            // Generate report based on analysis
            generateReportForAnalysis(analysis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze practice: " + e.getMessage());
        }
    }

    private void generateReportForAnalysis(Analysis analysis) {
        int score = calculateScore(analysis); // Implement scoring logic

        Report report = Report.builder()
                .analysis(analysis)
                .score(score)
                .feedbackMessage("Great job!")
                .build();

        reportRepo.save(report);
    }

    private int calculateScore(Analysis analysis) {
        // Example scoring logic
        int score = 100;
        if (analysis.getDecibel() < -30) {
            score -= 20;
        }
        if (analysis.getDuration() < 60) {
            score -= 10;
        }
        return score;
    }


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

    public List<PracticeDto> getPracticesByPresentationId(UUID presentationId) {
        return practiceRepo.findByPresentation_PresentationId(presentationId).stream()
                .map(practice -> PracticeDto.builder()
                        .id(practice.getId())
                        .practiceName(practice.getPracticeName())
                        .createdAt(practice.getCreatedAt())
                        .totalScore(practice.getTotalScore())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Integer> getRecentPracticeScores(UUID presentationId) {
        return practiceRepo.findTop5ByPresentation_PresentationIdOrderByCreatedAtDesc(presentationId).stream()
                .map(Practice::getTotalScore)
                .collect(Collectors.toList());
    }

    public double analyzeAudioDecibel(String audioFilePath) throws Exception {
        return AudioAnalyzer.calculateAverageDecibel(audioFilePath);
    }

    public void processPracticeWithDecibel(MultipartFile audioFile, UUID presentationId) throws Exception {
        // Save the audio file locally
        String savedPath = saveAudioFile(audioFile);

        // Analyze decibel
        double averageDecibel = analyzeAudioDecibel(savedPath);

        System.out.println("Average Decibel: " + averageDecibel);

        // Process the practice as needed (e.g., save in DB, associate with Presentation)
    }
}
