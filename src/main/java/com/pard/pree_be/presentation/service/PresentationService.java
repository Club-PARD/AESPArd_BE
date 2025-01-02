package com.pard.pree_be.presentation.service;

import com.pard.pree_be.practice.dto.PracticeRequestDto;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.practice.repo.PracticeRepo;
import com.pard.pree_be.presentation.dto.PresentationCellDto;
import com.pard.pree_be.presentation.dto.PresentationRequestDto;
import com.pard.pree_be.presentation.dto.PresentationCellDto;
import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.presentation.repo.PresentationRepo;
import com.pard.pree_be.record.service.RecordService;
import com.pard.pree_be.user.entity.User;
import com.pard.pree_be.user.repo.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresentationService {

    private final PresentationRepo presentationRepo;
    private final UserRepo userRepo;
    private final PracticeRepo practiceRepo;
    private final RecordService recordService;

    @Transactional
    public Presentation createPresentation(PresentationRequestDto requestDto) {
        User user = userRepo.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + requestDto.getUserId()));

        Presentation presentation = Presentation.builder()
                .user(user)
                .presentationName(requestDto.getPresentationName())
                .idealMinTime(requestDto.getIdealMinTime())
                .idealMaxTime(requestDto.getIdealMaxTime())
                .showMeOnScreen(requestDto.isShowMeOnScreen())
                .showTimeOnScreen(requestDto.isShowTimeOnScreen())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPractices(0)
                .build();

        return presentationRepo.save(presentation);
    }

    public List<PresentationCellDto> getPresentationList(UUID userId) {
        return presentationRepo.findAllByUser_UserId(userId)
                .stream()
                .map(this::mapToPresentationCellDto)
                .collect(Collectors.toList());
    }

    public List<PresentationCellDto> getAllPresentations(UUID userId) {
        // Fetch presentations for the user
        List<Presentation> presentations = presentationRepo.findByUser_UserId(userId);

        // Map to DTOs
        return presentations.stream()
                .map(presentation -> PresentationCellDto.builder()
                        .presentationId(presentation.getPresentationId())
                        .presentationName(presentation.getPresentationName())

                        .toggleFavorite(presentation.isToggleFavorite())
                        .totalPractices(presentation.getTotalPractices())
                        .totalScore(presentation.getTotalScore())
                        .build())
                .collect(Collectors.toList());
    }


    @Transactional
    public boolean toggleFavorite(UUID presentationId) {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new EntityNotFoundException("Presentation not found for ID: " + presentationId));

        presentation.setToggleFavorite(!presentation.isToggleFavorite());
        presentation.setUpdatedAt(LocalDateTime.now()); // Ensure updatedAt is refreshed
        return presentationRepo.save(presentation).isToggleFavorite();
    }

    public List<PresentationCellDto> getFavoritesByCreatedAt(UUID userId) {
        return presentationRepo.findFavoritesOrderByCreatedAt(userId)
                .stream()
                .map(this::mapToPresentationCellDto)
                .collect(Collectors.toList());
    }


    public List<PresentationCellDto> getPresentationsSortedByUpdatedAt(UUID userId) {
        return presentationRepo.findAllByUser_UserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::mapToPresentationCellDto) // Map to DTO with formatted "updatedAt"
                .collect(Collectors.toList());
    }

    private PresentationCellDto mapToPresentationCellDto(Presentation presentation) {
        return PresentationCellDto.builder()
                .presentationId(presentation.getPresentationId())
                .presentationName(presentation.getPresentationName())
                .toggleFavorite(presentation.isToggleFavorite())
                .totalScore(presentation.getTotalScore())
                .totalPractices(presentation.getTotalPractices())
                .updatedAtText(formatUpdatedAt(presentation.getUpdatedAt()))
                .idealMinTime(presentation.getIdealMinTime())
                .idealMaxTime(presentation.getIdealMaxTime())
                .showMeOnScreen(presentation.isShowMeOnScreen())
                .showTimeOnScreen(presentation.isShowTimeOnScreen())
                .build();
    }


    private String formatUpdatedAt(LocalDateTime updatedAt) {
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = ChronoUnit.DAYS.between(updatedAt, now);

        if (daysDiff == 0)
            return "오늘";
        return daysDiff + "일전";
    }

    @Transactional
    public void deletePresentation(UUID presentationId) {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found for ID: " + presentationId));
        presentationRepo.delete(presentation);
    }


    @Transactional
    public void deleteAllPresentationsForUser(UUID userId) {
        List<Presentation> presentations = presentationRepo.findAllByUser_UserId(userId);
        presentationRepo.deleteAll(presentations);
    }

    @Transactional
    public void deleteSelectedPresentations(List<UUID> presentationIds) {
        List<Presentation> presentations = presentationRepo.findAllById(presentationIds);
        if (presentations.isEmpty()) {
            throw new IllegalArgumentException("No presentations found for the given IDs.");
        }
        presentationRepo.deleteAll(presentations);
    }


    @Transactional
    public List<PresentationCellDto> searchPresentationsByName(UUID userId, String searchTerm) {
        List<Presentation> presentations;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Fetch all presentations for the user
            presentations = presentationRepo.findByUser_UserId(userId);
        } else {
            // Fetch presentations matching the search term
            presentations = presentationRepo.findByUser_UserId(userId).stream()
                    .filter(presentation -> presentation.getPresentationName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Map to DTOs with proper updatedAtText
        return presentations.stream()
                .map(this::mapToPresentationCellDto) // Use the common mapping method
                .collect(Collectors.toList());
    }



    @Transactional
    public void addPracticeToExistingPresentation(UUID presentationId, PracticeRequestDto practiceRequestDto) {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found"));

        Practice practice = Practice.builder()
                .practiceName(practiceRequestDto.getPracticeName())
                .audioFile(practiceRequestDto.getAudioFile())
                .eyePercentage(practiceRequestDto.getEyePercentage())
                .presentation(presentation)
                .build();

        practiceRepo.save(practice);

        // Update presentation practice count
        presentation.setTotalPractices(presentation.getTotalPractices() + 1);
        presentationRepo.save(presentation);
        presentationRepo.flush();
    }

    @Transactional
    public void createPresentationWithPractice(PresentationRequestDto presentationRequestDto,
                                               PracticeRequestDto practiceRequestDto) {
        User user = userRepo.findById(presentationRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + presentationRequestDto.getUserId()));

        // Create the new presentation
        Presentation presentation = Presentation.builder()
                .user(user)
                .presentationName(presentationRequestDto.getPresentationName())
                .idealMinTime(presentationRequestDto.getIdealMinTime())
                .idealMaxTime(presentationRequestDto.getIdealMaxTime())
                .showMeOnScreen(presentationRequestDto.isShowMeOnScreen())
                .showTimeOnScreen(presentationRequestDto.isShowTimeOnScreen())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalPractices(1) // Starts with the first practice
                .build();

        presentation = presentationRepo.save(presentation);

        // Create the first practice
        Practice practice = Practice.builder()
                .practiceName(practiceRequestDto.getPracticeName())
                .audioFile(practiceRequestDto.getAudioFile())
                .eyePercentage(practiceRequestDto.getEyePercentage())
                .presentation(presentation)
                .build();

        practiceRepo.save(practice);
    }

    @Transactional
    public void updatePresentationName(UUID presentationId, String newName) {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found for ID: " + presentationId));
        presentation.setPresentationName(newName);
        presentation.setUpdatedAt(LocalDateTime.now()); // Update timestamp
        presentationRepo.save(presentation);
    }

}
