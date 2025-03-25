package id.co.bcaf.goceng.securities;

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

    @Autowired
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authHeader = httpRequest.getHeader("Authorization");
        String requestURI = httpRequest.getRequestURI();

        // Log untuk debugging
        System.out.println("Incoming Request URI: " + requestURI);
        System.out.println("Raw Authorization Header: [" + authHeader + "]");

        // Bypass filter untuk endpoint yang tidak membutuhkan autentikasi
        if (requestURI.startsWith("/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        // Validasi Authorization Header
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            System.out.println("Authorization header missing or invalid.");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        // Ambil token setelah "Bearer " dan hapus spasi ekstra
        String token = authHeader.replaceFirst("(?i)^Bearer\\s+", "").trim();
        System.out.println("Processed Token jwt Filter: [" + token + "]");

        try {
            String email = jwtUtil.extractEmail(token);
            System.out.println("Extracted Email from Token: " + email);

            if (email == null || !jwtUtil.validateToken(token, email)) {
                System.out.println("Token validation failed.");
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            // Set user ke SecurityContext agar tidak mendapat error 403
            UserDetails userDetails = new User(email, "", Collections.emptyList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("User authenticated successfully: " + email);
        } catch (Exception e) {
            System.out.println("Error processing token: " + e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
            return;
        }

        chain.doFilter(request, response);
    }
}