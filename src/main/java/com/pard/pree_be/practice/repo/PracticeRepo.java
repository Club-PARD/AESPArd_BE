package com.pard.pree_be.practice.repo;

import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.presentation.entity.Presentation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PracticeRepo extends JpaRepository<Practice, Long> {


    List<Practice> findByPresentation_PresentationIdOrderByPracticeCreatedAtDesc(UUID presentationId);


    @Query("SELECT p.totalScore FROM Practice p WHERE p.presentation.presentationId = :presentationId")
    List<Integer> findScoresByPresentation_PresentationId(@Param("presentationId") UUID presentationId);
    long countByPresentation_PresentationId(UUID presentationId);

    List<Practice> findTop5ByPresentation_PresentationIdOrderByPracticeCreatedAtAsc(UUID presentationId);
    List<Practice> findTop1ByPresentation_PresentationIdOrderByPracticeCreatedAtDesc(UUID presentationId);


    @Query("SELECT p FROM Practice p WHERE p.presentation.presentationId = :presentationId ORDER BY p.practiceCreatedAt DESC")
    Optional<Practice> findTopByPresentationIdOrderByPracticeCreatedAtDesc(@Param("presentationId") UUID presentationId);


}