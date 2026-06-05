package com.sparkminds.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String email;
    private String username;
    private String role;
    private String message;

    public static AuthResponse success(String accessToken, String refreshToken,
                                       String email, String username, String role) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(email)
                .username(username)
                .role(role)
                .build();
    }

    public static AuthResponse message(String message) {
        return AuthResponse.builder()
                .message(message)
                .build();
    }
}