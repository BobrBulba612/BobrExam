package com.bobr.exam.controller;

import com.bobr.exam.model.Task;
import com.bobr.exam.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seed")
@CrossOrigin(origins = "*")
public class SeedController {

    private final TaskRepository repo;

    public SeedController(TaskRepository repo) { this.repo = repo; }

    private Task t(String subj, String exam, int year, String topic, int diff,
                   String type, String q, String opts, String ans, String expl) {
        Task t = new Task();
        t.setSubject(subj); t.setExamType(exam); t.setYear(year); t.setTopic(topic);
        t.setDifficulty(diff); t.setType(type); t.setQuestionText(q);
        t.setOptionsJson(opts); t.setCorrectAnswer(ans); t.setExplanation(expl);
        return t;
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> seed() {
        if (repo.count() > 0) {
            Map<String,Object> r = new HashMap<>();
            r.put("message","already seeded"); r.put("count", repo.count());
            return ResponseEntity.ok(r);
        }
        repo.save(t("Математика","ОГЭ",2024,"Дроби",1,"text_input",
                "Найдите значение выражения: (15/8 - 1/4) : 2", null, "0.8125",
                "Приведи дроби к общему знаменателю, затем раздели на 2."));
        repo.save(t("Математика","ОГЭ",2024,"Проценты",2,"text_input",
                "Товар стоил 800 рублей и подешевел на 15%. Сколько стоит теперь?", null, "680",
                "Найди 15% от 800 и вычти из исходной цены."));
        repo.save(t("Русский язык","ОГЭ",2024,"Орфоэпия",2,"multiple_choice",
                "В каком слове верно выделена ударная гласная?",
                "{\"options\":[\"звонИт\",\"звОнит\",\"звонит\"]}", "звонИт",
                "У глагола звонить ударение падает на окончание."));
        repo.save(t("Физика","ОГЭ",2024,"Механика",2,"text_input",
                "Скорость 72 км/ч. Вырази в м/с.", null, "20",
                "Раздели значение на 3.6."));
        repo.save(t("Информатика","ОГЭ",2024,"Системы счисления",3,"text_input",
                "Переведи 101101 из двоичной в десятичную.", null, "45",
                "Сложи степени двойки для единичных разрядов."));

        Map<String,Object> r = new HashMap<>();
        r.put("message","seeded"); r.put("count", repo.count());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/tasks/count")
    public ResponseEntity<?> count() {
        Map<String,Object> r = new HashMap<>();
        r.put("count", repo.count());
        return ResponseEntity.ok(r);
    }
}
