package com.uuriturg.user.service;

import com.uuriturg.user.domain.User;
import com.uuriturg.user.domain.UserRole;
import com.uuriturg.user.dto.*;
import com.uuriturg.user.exception.DuplicateEmailException;
import com.uuriturg.user.exception.UserNotFoundException;
import com.uuriturg.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final IUserRepository userRepository;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        UserRole role = UserRole.TENANT;
        if (request.getRole() != null) {
            try { role = UserRole.valueOf(request.getRole().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(role)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getRole() != null) {
            try { user.setRole(UserRole.valueOf(request.getRole().toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public ValidateUserResponse validateUser(UUID userId) {
        User user = userRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ValidateUserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.getActive())
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
