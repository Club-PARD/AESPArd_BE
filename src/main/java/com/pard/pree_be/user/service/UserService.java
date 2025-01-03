package com.pard.pree_be.user.service;

import com.pard.pree_be.user.dto.UserResponseDto;
import org.springframework.stereotype.Service;
import com.pard.pree_be.user.dto.UserRequestDto;
import com.pard.pree_be.user.entity.User;
import com.pard.pree_be.user.repo.UserRepo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public void save(UserRequestDto.CreateUser req) {
        User user = User.builder()
                .userName(req.getUserName())
                .email(req.getEmail())
                .build();
        userRepo.save(user);
    }

    public User getUserById(UUID userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
    }

    public String getUserNameById(UUID userId) {
        return userRepo.findById(userId)
                .map(User::getUserName)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
    }

    public UserResponseDto getUserNameAndEmailById(UUID userId) {
        return userRepo.findById(userId)
                .map(user -> new UserResponseDto(user.getUserName(), user.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
    }

    public List<UUID> getAllUserIds() {
        return userRepo.findAll().stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }


}
