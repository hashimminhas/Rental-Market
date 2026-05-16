package com.uuriturg.user.repository;

import com.uuriturg.user.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserIdAndActiveTrue(UUID userId);

    boolean existsByEmail(String email);
}
