package id.co.bcaf.goceng.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final long CLOCK_SKEW_SECONDS = 300; // 5 minutes

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refreshExpiration}")
    private long refreshExpirationMs;

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getEncoder().encode(secret.getBytes());
        this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    // 🔐 Generate Access Token with role
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔁 Generate Refresh Token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 📤 Extract token from Bearer header
    public String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.toLowerCase().startsWith("bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Bearer token");
        }
        return bearerToken.substring(7).trim();
    }

    // 📧 Extract Email (Subject)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 👑 Extract Role
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // 📅 Extract Expiration
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ✅ Validate Token
    public boolean validateToken(String token, String email) {
        try {
            String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // ⚠️ Check Expiration
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 🔍 Generic Claim Extractor
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // 🔓 Decode All Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .setAllowedClockSkewSeconds(CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Calendar extractExpiration(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expirationDate);
        return calendar;
    }
}
