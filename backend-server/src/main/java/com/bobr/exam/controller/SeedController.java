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

    private final TaskRepository taskRepository;

    public SeedController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> seedTasks() {
        taskRepository.deleteAll();

        Task t1 = new Task();
        t1.setSubject("Математика");
        t1.setExamType("ОГЭ");
        t1.setYear(2024);
        t1.setTopic("Дроби и рациональные выражения");
        t1.setDifficulty(1);
        t1.setType("text_input");
        t1.setQuestionText("Найдите значение выражения: (15/8 - 1/4) : 2");
        t1.setCorrectAnswer("0.8125");
        t1.setExplanation("Сначала выполните вычитание в скобках, приведя дроби к общему знаменателю. Затем разделите результат на 2.");

        Task t2 = new Task();
        t2.setSubject("Русский язык");
        t2.setExamType("ОГЭ");
        t2.setYear(2024);
        t2.setTopic("Орфография");
        t2.setDifficulty(2);
        t2.setType("multiple_choice");
        t2.setQuestionText("В каком слове верно выделена буква, обозначающая ударный гласный звук?");
        t2.setOptionsJson("{\"options\": [\"звонИт\", \"звОнит\", \"звонит\"]}");
        t2.setCorrectAnswer("звонИт");
        t2.setExplanation("Глагол 'звонить' относится к группе глаголов с неподвижным ударением на окончании: звонИт, звонИшь, звонЯт.");

        Task t3 = new Task();
        t3.setSubject("Физика");
        t3.setExamType("ОГЭ");
        t3.setYear(2024);
        t3.setTopic("Механика");
        t3.setDifficulty(2);
        t3.setType("text_input");
        t3.setQuestionText("Автомобиль движется со скоростью 72 км/ч. Выразите эту скорость в м/с.");
        t3.setCorrectAnswer("20");
        t3.setExplanation("Для перевода км/ч в м/с нужно умножить на 1000 и разделить на 3600. Или просто разделить на 3.6.");

        Task t4 = new Task();
        t4.setSubject("Информатика");
        t4.setExamType("ОГЭ");
        t4.setYear(2024);
        t4.setTopic("Системы счисления");
        t4.setDifficulty(3);
        t4.setType("text_input");
        t4.setQuestionText("Переведите двоичное число 101101 в десятичную систему счисления.");
        t4.setCorrectAnswer("45");
        t4.setExplanation("Умножьте каждую цифру на соответствующую степень двойки: 1*32 + 0*16 + 1*8 + 1*4 + 0*2 + 1*1 = 45");

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);
        taskRepository.save(t4);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully seeded 4 tasks");
        response.put("count", taskRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/count")
    public ResponseEntity<?> getTasksCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("count", taskRepository.count());
        return ResponseEntity.ok(response);
    }
}
