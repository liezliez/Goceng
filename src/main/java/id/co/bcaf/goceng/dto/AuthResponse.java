package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private Long expiresAt;
    private List<String> features; // Add features field

    // Constructor with features
    public AuthResponse(String token, String refreshToken, String username, Long expiresAt, List<String> features) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.expiresAt = expiresAt;
        this.features = features;
    }

}
