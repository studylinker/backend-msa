package com.study.user.repository;

import com.study.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // ✅ 오늘 가입한 사용자 수.
    @Query(value = "SELECT COUNT(*) FROM Users WHERE DATE(created_at) = CURDATE()", nativeQuery = true)
    long countNewUsersToday();
}
