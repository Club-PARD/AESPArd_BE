package com.pard.pree_be.presentation.repo;

import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresentationRepo extends JpaRepository<Presentation, UUID> {

    boolean existsByUser_UserIdAndPresentationName(UUID userId, String presentationName);

    @EntityGraph(attributePaths = "practices")
    List<Presentation> findAllByUser_UserIdOrderByUpdatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "practices")
    List<Presentation> findAllByUser_UserIdOrderByToggleFavoriteDescUpdatedAtDesc(UUID userId);

    List<Presentation> findAllByUser_UserId(UUID userId);

    @EntityGraph(attributePaths = { "practices.analysis", "practices.audioFile" })
    Optional<Presentation> findById(UUID presentationId);

    @Modifying
    @Query("DELETE FROM Presentation p WHERE p.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM Presentation p WHERE p.presentationId IN :ids")
    void deleteAllByIdIn(@Param("ids") List<UUID> ids);

    List<Presentation> findByUser_UserIdAndPresentationNameContainingIgnoreCaseOrderByUpdatedAtDesc(UUID userId,
            String presentationName);

    Optional<Presentation> findTopByUser_UserIdOrderByCreatedAtDesc(UUID userId);
}
