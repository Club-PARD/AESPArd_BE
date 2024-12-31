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

    public List<PresentationCellDto> getPresentationsSortedByFavorite(UUID userId) {
        return presentationRepo.findAllByUser_UserIdOrderByToggleFavoriteDescUpdatedAtDesc(userId)
                .stream()
                .map(this::mapToPresentationCellDto)
                .collect(Collectors.toList());
    }

    public boolean toggleFavorite(UUID presentationId) {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found"));

        boolean newState = !presentation.isToggleFavorite();
        presentation.setToggleFavorite(newState);

        presentationRepo.save(presentation);
        presentationRepo.flush();

        return newState;
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
                .idealMinTime(presentation.getIdealMinTime()) // Map min time
                .idealMaxTime(presentation.getIdealMaxTime())
                .build();
    }

    private String formatUpdatedAt(LocalDateTime updatedAt) {
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = ChronoUnit.DAYS.between(updatedAt, now);

        if (daysDiff == 0)
            return "오늘"; // "Today"
        return daysDiff + "일전"; // e.g., "1 day ago"
    }

    public void deletePresentation(UUID presentationId) {
        Presentation presentation = presentationRepo.findById(presentationId)
                .orElseThrow(() -> new IllegalArgumentException("Presentation not found"));

        presentationRepo.delete(presentation);
    }

    @Transactional
    public void deleteAllPresentationsForUser(UUID userId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
        presentationRepo.deleteAllByUserId(userId);
    }

    // TODO: if nothing matchs 아무것도 보내지 말게 해야함 ㅋㅋㅋ
    @Transactional
    public void deleteSelectedPresentations(List<UUID> presentationIds) {
        List<Presentation> presentations = presentationRepo.findAllById(presentationIds);

        if (presentations.isEmpty()) {
            throw new IllegalArgumentException("No presentations found for the given IDs.");
        }

        presentations.forEach(presentationRepo::delete); // DELETE cascade
    }

    @Transactional
    public List<PresentationCellDto> searchPresentationsByName(UUID userId, String searchTerm) {
        List<Presentation> presentations;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            presentations = presentationRepo.findAllByUser_UserIdOrderByUpdatedAtDesc(userId);
        } else {
            presentations = presentationRepo
                    .findByUser_UserIdAndPresentationNameContainingIgnoreCaseOrderByUpdatedAtDesc(userId, searchTerm);
        }
        return presentations.stream()
                .map(this::mapToPresentationCellDto)
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


}
