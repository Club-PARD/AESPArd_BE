package com.pard.pree_be.feedback.analysis.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.report.service.ReportService;
import com.pard.pree_be.practice.entity.Practice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepo analysisRepo;
    private final ReportRepo reportRepo;

    public Analysis analyzePractice(Practice practice) {
        // TODO: remove mock data
        Analysis analysis = Analysis.builder()
                .practice(practice)
                .duration(120.0)
                .speechSpeed(140.0)
                .decibel(68.0)
                .fillerCount(2)
                .blankCount(1)
                .eyePercentage(85)
                .build();
        Analysis savedAnalysis = analysisRepo.save(analysis);

        generateReports(savedAnalysis);

        return savedAnalysis;
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
        return Report.builder()
                .name(metricName)
                .counter(counter)
                .score(100)
                .feedbackMessage("Feedback for " + metricName)
                .build();
    }
}
