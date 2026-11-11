package com.bobr.exam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType = "Bearer";

        public TokenResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
