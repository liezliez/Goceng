package id.co.bcaf.goceng.securities;

import id.co.bcaf.goceng.repositories.BlacklistedTokenRepository;
import id.co.bcaf.goceng.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authHeader = httpRequest.getHeader("Authorization");
        String requestURI = httpRequest.getRequestURI();

        System.out.println("🔹 Incoming Request URI: " + requestURI);
        System.out.println("🔹 Raw Authorization Header: " + authHeader);

        if (requestURI.equals("/api/v1/auth/login") ||
                requestURI.equals("/api/v1/auth/register") ||
                requestURI.matches("^/users(/[^/]+)?$")) {
            System.out.println("✅ Public endpoint accessed: " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            System.out.println("⛔ Authorization header missing or invalid.");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        // Extract token
        String token = authHeader.replaceFirst("(?i)^Bearer\\s+", "").trim();
        System.out.println("🔹 Processed Token: " + token);

        // Check if token is blacklisted
        if (blacklistedTokenRepository.existsByToken(token)) {
            System.out.println("⛔ JWT is blacklisted: " + token);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Blacklisted token");
            return;
        }

        try {
            String email = jwtUtil.extractEmail(token);
            System.out.println("🔹 Extracted Email from Token: " + email);

            if (email == null || !jwtUtil.validateToken(token, email)) {
                System.out.println("⛔ Token validation failed.");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            UserDetails userDetails = new User(email, "", Collections.emptyList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("✅ User authenticated successfully: " + email);
        } catch (Exception e) {
            System.out.println("⛔ Error processing token: " + e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
            return;
        }

        chain.doFilter(request, response);
    }
}
