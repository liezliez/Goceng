package id.co.bcaf.goceng.securities;

import id.co.bcaf.goceng.repositories.BlacklistedTokenRepository;
import id.co.bcaf.goceng.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    );

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

        logger.info("🔹 Incoming Request URI: {}", requestURI);
        logger.debug("🔹 Raw Authorization Header: {}", authHeader);

        // Allow public endpoints
        if (isPublicEndpoint(requestURI)) {
            logger.info("✅ Public endpoint accessed: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // Validate Authorization header
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            logger.warn("⛔ Authorization header missing or invalid.");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        String token = authHeader.replaceFirst("(?i)^Bearer\\s+", "").trim();
        logger.debug("🔹 Processed Token: {}", token);

        // Check blacklist
        if (blacklistedTokenRepository.existsByToken(token)) {
            logger.warn("⛔ JWT is blacklisted: {}", token);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Blacklisted token");
            return;
        }

        try {
            String email = jwtUtil.extractEmail(token);
            logger.debug("🔹 Extracted Email from Token: {}", email);

            if (email == null || !jwtUtil.validateToken(token, email)) {
                logger.warn("⛔ Token validation failed.");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            UserDetails userDetails = new User(email, "", Collections.emptyList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("✅ User authenticated: {}", email);

        } catch (Exception e) {
            logger.error("⛔ Error processing token: {}", e.getMessage(), e);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        return PUBLIC_ENDPOINTS.contains(uri) || uri.matches("^/users(/[^/]+)?$");
    }
}
