package com.bobr.exam.dto;

import lombok.Data;

public class AuthDTO {
    @Data
    public static class RegisterRequest {
        private String email;
        private String nickname;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class TokenResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        public TokenResponse(String accessToken) { this.accessToken = accessToken; }
    }
}
