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
        private int streak;
    }

    @Data
    public static class SetupRequest {
        private Integer grade;
        private List<String> subjects;
    }

    @Data
    public static class ProfileUpdateRequest {
        private String nickname;
        private String email;
        private String avatar;
        private String language;
    }

    @Data
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
    }
}