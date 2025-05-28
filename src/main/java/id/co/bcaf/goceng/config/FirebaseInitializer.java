package id.co.bcaf.goceng.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FirebaseInitializer {

    @PostConstruct
    public void initialize() {
        try {
//            ClassPathResource resource = new ClassPathResource("goceng-app-6e913-firebase-adminsdk-fbsvc-dd705f02ac.json");

            ClassPathResource resource = new ClassPathResource("goceng-app-6e913-firebase-adminsdk-fbsvc-6d0a7d908c.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // tes
        }
    }
}

