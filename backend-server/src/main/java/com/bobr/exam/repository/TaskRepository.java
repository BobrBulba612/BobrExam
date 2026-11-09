package com.bobr.exam.repository;

import com.bobr.exam.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByExamTypeAndSubjectOrderByDifficultyAsc(String examType, String subject);
}
