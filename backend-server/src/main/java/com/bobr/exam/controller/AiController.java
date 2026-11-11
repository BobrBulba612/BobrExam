package com.bobr.exam.controller;

import com.bobr.exam.dto.AiDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    @Value("${qwen.api.key:}") private String apiKey;
    @Value("${qwen.api.url}") private String apiUrl;
    @Value("${qwen.model.name}") private String model;
    @Value("${proxy.enabled:false}") private boolean proxyEnabled;
    @Value("${proxy.host:}") private String proxyHost;
    @Value("${proxy.port:1080}") private int proxyPort;
    @Value("${ai.mock.enabled:true}") private boolean mockEnabled;

    private final ObjectMapper om = new ObjectMapper();nabled || apiKey == null || apiKey.isEmpty()) {
            Map<String, Object> r = new HashMap<>();
            r.put("explanation", "Привет! Я Бобр-Профессор. Разберём тему \"" + req.getSubject()
                    + "\" для " + req.getExamType() + ". Вспомни правило по этой теме и попробуй ещё раз."
                    + " Какой первый шаг ты бы сделал?");
            r.put("mascot_state", "professor_glasses");
            r.put("mock", true);
            return ResponseEntity.ok(r);
        }

        String system = "Ты — дружелюбный ИИ-репетитор по имени Бобр-Профессор. "
                + "Помогаешь готовиться к " + req.getExamType() + " по предмету \"" + req.getSubject() + "\". "
                + "Задание: " + req.getTaskText() + ". Ответ ученика: " + req.getUserAnswer()
                + ". Правильный ответ: " + req.getCorrectAnswer() + ". "
                + "Объясни теорию, не давай готовый ответ, задай 1-2 наводящих вопроса. Не более 150 слов.";

        try {
            ObjectNode payload = om.createObjectNode();
            payload.put("model", model);
            ArrayNode msgs = payload.putArray("messages");
            ObjectNode m = msgs.addObject();
            m.put("role", "system");
            m.put("content", system);
            payload.put("max_tokens", 300);
            payload.put("temperature", 0.7);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "https://github.com/BobrBulba612/BobrExam")
                    .header("X-Title", "BobrExam")
                    .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> resp = client().send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                Map<String, Object> err = new HashMap<>();
                err.put("explanation", "ИИ временно недоступен (код " + resp.statusCode() + "). Попробуй позже.");
                err.put("mock", true);
                return ResponseEntity.ok(err);
            }

            JsonNode root = om.readTree(resp.body());
            String text = root.path("choices").path(0).path("message").path("content").asText("");

            Map<String, Object> r = new HashMap<>();
            r.put("explanation", text);
            r.put("mascot_state", "professor_glasses");
            r.put("mock", false);
            return ResponseEntity.ok(r);

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("explanation", "Не удалось связаться с ИИ: " + e.getMessage());
            err.put("mock", true);
            return ResponseEntity.ok(err);
        }
    }
}
