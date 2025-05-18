package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final ApplicationService applicationService;

    @GetMapping
    public String testService() {
        return applicationService == null ? "Service is null" : "Service is OK";
    }
}
