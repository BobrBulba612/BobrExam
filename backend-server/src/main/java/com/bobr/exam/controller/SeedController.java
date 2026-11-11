package com.bobr.exam.controller;

import com.bobr.exam.model.Task;
import com.bobr.exam.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seed")
@CrossOrigin(origins = "*")
public class SeedController {

    private final TaskRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public SeedController(TaskRepository repo) {
        this.repo = repo;
    }

    private void load(String file, List<Task> out) throws Exception {
        ClassPathResource res = new ClassPathResource(file);
        if (!res.exists()) return;
        try (InputStream in = res.getInputStream()) {
            JsonNode arr = om.readTree(in);
            for (JsonNode n : arr) {
                Task t = new Task();
                t.setSubject(n.path("subject").asText());
                t.setExamType(n.path("examType").asText());
                t.setGrade(n.path("grade").asInt());
                t.setExamNumber(n.path("examNumber").asInt());
                t.setYear(n.path("year").asInt());
                t.setTopic(n.path("topic").asText());
                t.setDifficulty(n.path("difficulty").asInt());
                t.setType(n.path("type").asText());
                t.setQuestionText(n.path("questionText").asText());
                t.setCorrectAnswer(n.path("correctAnswer").asText());
                t.setExplanation(n.path("explanation").asText());
                t.setSource(n.path("source").asText());
                JsonNode opts = n.path("options");
                if (opts.isArray()) t.setOptionsJson(om.writeValueAsString(opts));
                out.add(t);
            }
        }
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> seed(
            @RequestParam(defaultValue = "false") boolean force) throws Exception {
        Map<String, Object> r = new HashMap<>();
        if (force) repo.deleteAll();
        if (repo.count() > 0) {
            r.put("message", "already seeded");
            r.put("count", repo.count());
            return ResponseEntity.ok(r);
        }
        List<Task> batch = new ArrayList<>();
        load("tasks_school.json", batch);
        load("tasks_oge.json", batch);
        load("tasks_ege.json", batch);
        repo.saveAll(batch);
        r.put("message", "seeded");
        r.put("count", repo.count());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/tasks/count")
    public ResponseEntity<?> count() {
        Map<String, Object> r = new HashMap<>();
        r.put("count", repo.count());
        return ResponseEntity.ok(r);
    }
}
