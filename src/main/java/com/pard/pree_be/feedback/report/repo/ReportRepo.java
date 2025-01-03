package com.pard.pree_be.feedback.report.repo;

import com.pard.pree_be.feedback.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepo extends JpaRepository<Report, UUID> {
    List<Report> findByAnalysisId(Long analysisId);
}
