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

@Configuration
//@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // ❌ Disable CSRF only for APIs
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Publicly accessible endpoints
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register"
                        ).permitAll()

                        .requestMatchers("/features", "/users").permitAll() // 🛑 Is this correct? Remove if not intended.

                        // 🔒 Role-based access control (use hasAuthority if roles are stored without "ROLE_")
                        .requestMatchers("/users/**").hasRole("SUPERADMIN")
                        .requestMatchers("/branch/**").hasRole("BRANCH_MANAGER")
                        .requestMatchers("/marketing/**").hasRole("MARKETING")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")

                        // 🔒 Secure all other endpoints
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
