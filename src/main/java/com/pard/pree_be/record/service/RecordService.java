package com.pard.pree_be.record.service;

import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.practice.repo.PracticeRepo;
import com.pard.pree_be.record.dto.RecordRequestDto;
import com.pard.pree_be.record.dto.RecordResponseDto;
import com.pard.pree_be.record.entity.Record;
import com.pard.pree_be.record.repo.RecordRepo;
import com.pard.pree_be.user.entity.User;
import com.pard.pree_be.user.repo.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final PracticeRepo practiceRepo;

    public List<Integer> getScoresByPresentation(UUID presentationId) {
        // Retrieve scores for practices linked to the given presentationId
        return practiceRepo.findScoresByPresentation_PresentationId(presentationId);
    }

}