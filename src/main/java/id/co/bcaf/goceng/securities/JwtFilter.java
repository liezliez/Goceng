package id.co.bcaf.goceng.securities;

import id.co.bcaf.goceng.repositories.BlacklistedTokenRepository;
import id.co.bcaf.goceng.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/users/register",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/users/whoami"
    );

    @Autowired
    public JwtFilter(
            JwtUtil jwtUtil,
            BlacklistedTokenRepository blacklistedTokenRepository,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        String requestURI = httpRequest.getRequestURI();

        // Skip public endpoints
        if (isPublicEndpoint(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Process Authorization header and validate token
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        String token = authHeader.replaceFirst("(?i)^Bearer\\s+", "").trim();
        logger.info("Checking token: {}", token);

        if (blacklistedTokenRepository.existsByToken(token)) {
            logger.warn("Token is blacklisted: {}", token);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Blacklisted token");
            return;
        }

        try {
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            if (email == null || role == null || !jwtUtil.validateToken(token, email)) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            // Set Spring Security context with role
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.singleton(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            logger.error("JWT Authentication error: {}", e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(uri::startsWith);
    }
}
