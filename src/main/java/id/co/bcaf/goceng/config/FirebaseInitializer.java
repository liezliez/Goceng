package id.co.bcaf.goceng.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Initializes Firebase using service account JSON file configured in application.properties
 */
@Component
public class FirebaseInitializer {

    // Reads the path from application.properties
    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize Firebase. Please check your config path.");
        }
    }
}
