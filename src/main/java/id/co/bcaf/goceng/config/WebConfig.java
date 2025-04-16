package id.co.bcaf.goceng.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow cross-origin requests from localhost:4200 (Angular development server)
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200") // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // Allow credentials (cookies, headers)
                .maxAge(3600); // Cache preflight response for 1 hour (optional)
    }
}
