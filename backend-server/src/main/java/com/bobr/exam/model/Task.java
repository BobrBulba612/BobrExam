package com.bobr.exam.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String subject;
    private String examType;
    private Integer examNumber;
    private Integer grade;

    @Column(name = "exam_year")
    private Integer year;

    private String topic;
    private Integer difficulty;
    private String type;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private String source;
}
