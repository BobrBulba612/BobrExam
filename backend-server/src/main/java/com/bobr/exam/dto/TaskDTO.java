package com.bobr.exam.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

public class TaskDTO {
    @Data
    public static class SubmitRequest {
        private UUID taskId;
        private String userAnswer;
    }

    @Data
    public static class SubmitResponse {
        private boolean correct;
        private String correctAnswer;
        private String explanation;
        private int xp;
        private int hearts;
    }

    @Data
    public static class SetupRequest {
        private Integer grade;
        private List<String> subjects;
    }
}
