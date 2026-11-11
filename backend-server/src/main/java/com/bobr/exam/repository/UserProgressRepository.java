package com.bobr.exam.repository;

import com.bobr.exam.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    Optional<UserProgress> findByUserIdAndTaskId(UUID userId, UUID taskId);
}
