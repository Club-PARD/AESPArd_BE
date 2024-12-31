package com.pard.pree_be.feedback.report.service;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import com.pard.pree_be.feedback.analysis.repo.AnalysisRepo;
import com.pard.pree_be.feedback.analysis.service.AnalysisService;
import com.pard.pree_be.feedback.report.repo.ReportRepo;
import com.pard.pree_be.feedback.report.dto.ReportDto;
import com.pard.pree_be.feedback.report.entity.Report;
import com.pard.pree_be.feedback.scoring.MetricStandard;
import com.pard.pree_be.feedback.scoring.ScoringStandards;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.practice.repo.PracticeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {


}
