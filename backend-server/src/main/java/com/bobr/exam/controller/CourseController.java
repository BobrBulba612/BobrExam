package com.bobr.exam.controller;

import com.bobr.exam.dto.TaskPublic;
import com.bobr.exam.model.Task;
import com.bobr.exam.model.User;
import com.bobr.exam.repository.TaskRepository;
import com.bobr.exam.repository.UserRepository;
import com.bobr.exam.service.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
@CrossOrigin(origins = "*")
public class CourseController {

    private final TaskRepository tasks;
    private final UserRepository users;
    private final JwtService jwt;
    private final ObjectMapper om = new ObjectMapper();

    public CourseController(TaskRepository tasks, UserRepository users, JwtService jwt) {
        this.tasks = tasks;
        this.users = users;
        this.jwt = jwt;
    }

    private int gradeOf(Task t) {
        return t.getGrade() == null ? 9 : t.getGrade();
    }

    @GetMapping("/my")
    public ResponseEntity<?> my(
            @RequestHeader(value = "Authorization", required = false) String auth)
            throws Exception {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing token");
        }
        String token = auth.substring(7);
        if (!jwt.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }
        User u = users.findByEmail(jwt.extractEmail(token)).orElse(null);
        if (u == null) return ResponseEntity.status(401).body("User not found");
        if (u.getGrade() == null || u.getSubjects() == null) {
            return ResponseEntity.status(400).body("Setup required");
        }

        List<String> subs = om.readValue(u.getSubjects(),
                new TypeReference<List<String>>() { });
        int g = u.getGrade();
        List<Map<String, Object>> out = new ArrayList<>();

        for (String s : subs) {
            List<Task> all = tasks.findBySubjectOrderByGradeAscDifficultyAsc(s);
            List<Task> pick = new ArrayList<>();
            for (Task t : all) {
                if (Math.abs(gradeOf(t) - g) <= 1) pick.add(t);
            }
            pick.sort(Comparator
                    .comparingInt((Task t) -> Math.abs(gradeOf(t) - g))
                    .thenComparingInt(t -> t.getDifficulty() == null ? 1 : t.getDifficulty()));
            if (pick.size() > 8) pick = new ArrayList<>(pick.subList(0, 8));

            List<TaskPublic> pubs = new ArrayList<>();
            for (Task t : pick) pubs.add(TaskPublic.of(t));

            Map<String, Object> unit = new HashMap<>();
            unit.put("subject", s);
            unit.put("grade", g);
            unit.put("tasks", pubs);
            out.add(unit);
        }
        return ResponseEntity.ok(out);
    }
}
