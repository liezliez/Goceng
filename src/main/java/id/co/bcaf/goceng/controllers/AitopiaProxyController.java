package id.co.bcaf.goceng.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Proxy controller to interact with the Aitopia external API.
 *
 * Provides an endpoint to forward POST requests to Aitopia's `/ai/model_settings` endpoint,
 * acting as a backend proxy to avoid CORS issues and securely include headers like API keys.
 *
 * The proxy forwards the request, handles errors gracefully, and returns the remote
 * response directly to the client.
 *
 * Methods:
 * - {@link #proxyModelSettings()}: Forwards a POST request with an empty JSON body to Aitopia's model settings endpoint.
 */

@RestController
@RequestMapping("/api/aitopia")
public class AitopiaProxyController {

    @PostMapping("/model-settings")
    public ResponseEntity<?> proxyModelSettings() {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://extensions.aitopia.ai/ai/model_settings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // If Aitopia requires API key or auth token, you can add it here:
        // headers.set("Authorization", "Bearer <token>");

        HttpEntity<String> requestEntity = new HttpEntity<>("{}", headers); // empty JSON body if required

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error contacting Aitopia API: " + e.getMessage());
        }
    }
}
