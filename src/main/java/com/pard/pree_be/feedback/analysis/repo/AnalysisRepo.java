package com.pard.pree_be.feedback.analysis.repo;

import com.pard.pree_be.feedback.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnalysisRepo extends JpaRepository<Analysis, UUID> {

    @Query("SELECT a FROM Analysis a WHERE a.practice.id = :practiceId")
    Analysis findByPracticeId(@Param("practiceId") UUID practiceId);

    @Query("SELECT a FROM Analysis a WHERE a.practice.presentation.presentationId = :presentationId ORDER BY a.practice.practiceCreatedAt DESC")
    List<Analysis> findTop5ByPresentationIdOrderByPracticeCreatedAtDesc(@Param("presentationId") UUID presentationId);

}