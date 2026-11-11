package com.bobr.exam.controller;

import com.bobr.exam.dto.TaskDTO;
import com.bobr.exam.dto.TaskPublic;
import com.bobr.exam.model.Task;
import com.bobr.exam.model.User;
import com.bobr.exam.model.UserProgress;
import com.bobr.exam.repository.TaskRepository;
import com.bobr.exam.repository.UserProgressRepository;
import com.bobr.exam.repository.UserRepository;
import com.bobr.exam.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskRepository tasks;
    private final UserRepository users;
    private final UserProgressRepository progress;
    private final JwtService jwt;

    public TaskController(TaskRepository tasks, UserRepository users,
                          UserProgressRepository progress, JwtService jwt) {
        this.tasks = tasks;
        this.users = users;
        this.progress = progress;
        this.jwt = jwt;
    }

    @GetMapping("/courses/{examType}/{subject}/path")
    public ResponseEntity<List<TaskPublic>> path(@PathVariable String examType,
                                                 @PathVariable String subject) {
        List<Task> found = tasks
                .findByExamTypeAndSubjectOrderByDifficultyAsc(examType, subject);
        List<TaskPublic> out = new ArrayList<>();
        for (Task t : found) {
            out.add(TaskPublic.of(t));
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> one(@PathVariable UUID id) {
        Task t = tasks.findById(id).orElse(null);
        if (t == null) {
            return ResponseEntity.status(404).body("Task not found");
        }
        return ResponseEntity.ok(TaskPublic.of(t));
    }

    private String norm(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase().replace(',', '.').replace(" ", "");
    }

    private boolean same(Task t, String user) {
        String c = norm(t.getCorrectAnswer());
        String u = norm(user);
        if (u.isEmpty()) {
            return false;
        }
        if (u.equals(c)) {
            return true;
        }
        if ("text_input".equals(t.getType())) {
            try {
                double a = Double.parseDouble(u);
                double b = Double.parseDouble(c);
                return Math.abs(a - b) < 1e-9;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    @PostMapping("/progress/submit")
    public ResponseEntity<?> submit(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody TaskDTO.SubmitRequest req) {

        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing token");
        }
        String token = auth.substring(7);
        if (!jwt.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }
        User user = users.findByEmail(jwt.extractEmail(token)).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        Task task = tasks.findById(req.getTaskId()).orElse(null);
        if (task == null) {
            return ResponseEntity.status(404).body("Task not found");
        }

        boolean correct = same(task, req.getUserAnswer());

        UserProgress p = progress
                .findByUserIdAndTaskId(user.getId(), task.getId()).orElse(null);
        if (p == null) {
            p = new UserProgress();
            p.setUserId(user.getId());
            p.setTaskId(task.getId());
            p.setAttempts(1);
        } else {
            p.setAttempts(p.getAttempts() + 1);
        }
        p.setStatus(correct ? "completed" : "failed");
        p.setCompletedAt(Instant.now());
        progress.save(p);

        if (correct) {
            user.setXp(user.getXp() + 10);
        } else if (user.getHearts() > 0) {
            user.setHearts(user.getHearts() - 1);
        }
        users.save(user);

        TaskDTO.SubmitResponse r = new TaskDTO.SubmitResponse();
        r.setCorrect(correct);
        r.setCorrectAnswer(correct ? task.getCorrectAnswer() : null);
        r.setExplanation(task.getExplanation());
        r.setXp(user.getXp());
        r.setHearts(user.getHearts());
        return ResponseEntity.ok(r);
    }
}
