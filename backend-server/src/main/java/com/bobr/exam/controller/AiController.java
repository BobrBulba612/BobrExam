package com.bobr.exam.controller;

import com.bobr.exam.dto.AiDTO;
import com.bobr.exam.model.Task;
import com.bobr.exam.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final TaskRepository tasks;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${qwen.api.key:}")
    private String apiKey;

    @Value("${qwen.api.url}")
    private String apiUrl;

    @Value("${qwen.model.name}")
    private String model;

    @Value("${proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${proxy.host:}")
    private String proxyHost;

    @Value("${proxy.port:1080}")
    private int proxyPort;

    @Value("${ai.mock.enabled:true}")
    private boolean mockEnabled;

    public AiController(TaskRepository tasks) {
        this.tasks = tasks;
    }

    private HttpClient client() {
        HttpClient.Builder b = HttpClient.newBuilder();
        b.connectTimeout(Duration.ofSeconds(20));
        if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty()) {
            InetSocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
            b.proxy(ProxySelector.of(addr));
        }
        return b.build();
    }

    private Map<String, Object> answer(String text, boolean mock) {
        Map<String, Object> r = new HashMap<>();
        r.put("explanation", text);
        r.put("mascot_state", "professor_glasses");
        r.put("mock", mock);
        return r;
    }

    private String fallback(AiDTO.ExplainRequest req, String hint) {
        StringBuilder sb = new StringBuilder();
        sb.append("Привет! Я Бобр-Профессор. ");
        sb.append("Тема: ").append(req.getSubject()).append(". ");
        if (hint != null && !hint.isEmpty()) {
            sb.append(hint).append(" ");
        }
        sb.append("Подумай, какое правило здесь применяется. ");
        sb.append("Какой первый шаг ты бы сделал?");
        return sb.toString();
    }

    private String prompt(AiDTO.ExplainRequest req, String correct, String hint) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ты дружелюбный ИИ-репетитор Бобр-Профессор. ");
        sb.append("Готовишь школьника к ").append(req.getExamType()).append(". ");
        sb.append("Предмет: ").append(req.getSubject()).append(". ");
        sb.append("Задание: ").append(req.getTaskText()).append(". ");
        sb.append("Ответ ученика: ").append(req.getUserAnswer()).append(". ");
        if (correct != null && !correct.isEmpty()) {
            sb.append("Верный ответ: ").append(correct).append(". ");
        }
        if (hint != null && !hint.isEmpty()) {
            sb.append("Методическая подсказка: ").append(hint).append(". ");
        }
        sb.append("Объясни теорию просто и пошагово. ");
        sb.append("Не давай готовый ответ сразу. ");
        sb.append("Заверши 1-2 наводящими вопросами. ");
        sb.append("Не более 150 слов. Короткие абзацы.");
        return sb.toString();
    }

    @PostMapping("/explain")
    public ResponseEntity<?> explain(@RequestBody AiDTO.ExplainRequest req) {
        String correct = req.getCorrectAnswer();
        String hint = null;

        if (req.getTaskId() != null && !req.getTaskId().isEmpty()) {
            try {
                UUID id = UUID.fromString(req.getTaskId());
                Task t = tasks.findById(id).orElse(null);
                if (t != null) {
                    correct = t.getCorrectAnswer();
                    hint = t.getExplanation();
                }
            } catch (Exception ignored) { }
        }

        if (mockEnabled || apiKey == null || apiKey.isEmpty()) {
            return ResponseEntity.ok(answer(fallback(req, hint), true));
        }

        try {
            ObjectNode payload = om.createObjectNode();
            payload.put("model", model);
            ArrayNode msgs = payload.putArray("messages");
            ObjectNode m = msgs.addObject();
            m.put("role", "system");
            m.put("content", prompt(req, correct, hint));
            payload.put("max_tokens", 300);
            payload.put("temperature", 0.7);

            String body = om.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("X-Title", "BobrExam")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = client().send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                return ResponseEntity.ok(answer(fallback(req, hint), true));
            }

            JsonNode root = om.readTree(resp.body());
            String text = root.path("choices").path(0)
                    .path("message").path("content").asText("");

            if (text.isEmpty()) {
                return ResponseEntity.ok(answer(fallback(req, hint), true));
            }
            return ResponseEntity.ok(answer(text, false));
        } catch (Exception e) {
            return ResponseEntity.ok(answer(fallback(req, hint), true));
        }
    }
}
