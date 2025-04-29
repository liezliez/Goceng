package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String username;
    private Long expiresAt; // 🕒 (timestamp in milliseconds)

    public AuthResponse(String token, String refreshToken, String username, Long expiresAt) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.expiresAt = expiresAt;
    }
}
