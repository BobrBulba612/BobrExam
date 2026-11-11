package com.bobr.exam.dto;

import com.bobr.exam.model.Task;
import lombok.Data;
import java.util.UUID;

@Data
public class TaskPublic {
    private UUID id;
    private String subject;
    private String examType;
    private Integer examNumber;
    private Integer grade;
    private Integer year;
    private String topic;
    private Integer difficulty;
    private String type;
    private String questionText;
    private String optionsJson;
    private String source;

    public static TaskPublic of(Task t) {
        TaskPublic p = new TaskPublic();
        p.id = t.getId();
        p.subject = t.getSubject();
        p.examType = t.getExamType();
        p.examNumber = t.getExamNumber();
        p.grade = t.getGrade();
        p.year = t.getYear();
        p.topic = t.getTopic();
        p.difficulty = t.getDifficulty();
        p.type = t.getType();
        p.questionText = t.getQuestionText();
        p.optionsJson = t.getOptionsJson();
        p.source = t.getSource();
        return p;
    }
}
