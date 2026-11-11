package com.bobr.exam.repository;

import com.bobr.exam.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    Optional<UserProgress> findByUserIdAndTaskId(UUID userId, UUID taskId);
    @Transactional
    void deleteByUserId(UUID userId);
}