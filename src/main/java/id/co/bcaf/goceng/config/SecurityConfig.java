package id.co.bcaf.goceng.config;

import id.co.bcaf.goceng.securities.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Ensure method-level security (enabling @PreAuthorize)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF
                .csrf(csrf -> csrf.disable())
                // Authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints that are accessible to anyone
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/users/register", "/users/whoami").permitAll()

                        // Allow specific roles to access certain endpoints
                        .requestMatchers("/users/role-back-office").hasRole("BACK_OFFICE")
                        .requestMatchers("/users/role-branch-manager").hasRole("BRANCH_MANAGER")
                        .requestMatchers("/users/role-marketing").hasRole("MARKETING")
                        .requestMatchers("/users/role-customer").hasRole("CUSTOMER")

                        // Allow super admins to access user management or any other admin-specific routes
                        .requestMatchers("/users/**").hasRole("SUPERADMIN")

                        // Require authentication for all other endpoints
                        .anyRequest().authenticated()
                )
                // Stateless session management (using JWT, no session stored)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Add JWT filter to intercept requests before authentication
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Ensure correct origin URL (adjust this if needed)
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Allow credentials to be sent with CORS requests

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // Apply CORS to all endpoints

        return source;
    }
}
