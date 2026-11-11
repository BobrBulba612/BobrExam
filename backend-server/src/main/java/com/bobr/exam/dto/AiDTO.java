package com.bobr.exam.dto;

import lombok.Data;

public class AiDTO {
    @Data
    public static class ExplainRequest {
        private String taskId;
        private String examType;
        private String subject;
        private String taskText;
        private String userAnswer;
        private String correctAnswer;
    }
}
