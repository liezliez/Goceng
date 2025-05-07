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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtFilter extends GenericFilterBean {

    public static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/be/auth/login",
            "/be/auth/register",
            "/be/users/register",
//            "/users/whoami",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public JwtFilter(JwtUtil jwtUtil,
                     BlacklistedTokenRepository blacklistedTokenRepository,
                     UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        logger.info("JWT Filter triggered for URI: {}", requestURI);

        if (isPublicEndpoint(requestURI)) {
            chain.doFilter(request, response);
            return;
        }


        try {
            String bearerToken = httpRequest.getHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            System.out.println("Auth Header: " + httpRequest.getHeader("Authorization"));
            System.out.println("Token: " + jwtUtil.extractToken(bearerToken));
            System.out.println("Email: " + jwtUtil.extractEmail(bearerToken));



            // Extract and trim token
            String token = bearerToken.substring(7).trim(); // skip "Bearer "

            logger.info("Extracted Token: {}", token);

            if (blacklistedTokenRepository.existsByToken(token)) {
                logger.warn("Blacklisted token: {}", token);
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Blacklisted token");
                return;
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            Date expiration = jwtUtil.getExpirationDateFromToken(token);

            if (email == null || role == null || expiration == null || !jwtUtil.validateToken(token, email)) {
                logger.warn("Invalid token: email={}, role={}, expiration={}", email, role, expiration);
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            if (expiration.before(new Date())) {
                logger.warn("Token expired at {}", expiration);
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            }

            // Set Spring Security authentication
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage(), e);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
