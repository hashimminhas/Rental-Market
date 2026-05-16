package com.uuriturg.user.service;

import com.uuriturg.user.dto.*;

import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID userId);

    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    void deleteUser(UUID userId);

    ValidateUserResponse validateUser(UUID userId);
}
