package com.pard.pree_be.feedback.recentscores.repo;
import com.pard.pree_be.feedback.recentscores.entity.RecentScores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecentScoresRepo extends JpaRepository<RecentScores, UUID> {
    Optional<RecentScores> findByMetricName(String metricName);
}
