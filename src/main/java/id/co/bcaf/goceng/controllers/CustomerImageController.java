package id.co.bcaf.goceng.controllers;


import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.services.CustomerService;
import id.co.bcaf.goceng.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/customers/upload")
public class CustomerImageController {

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/{id}/ktp")
    public CustomerResponse uploadKtp(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        String url = imageUploadService.uploadImage(file);
        return customerService.updateKtpUrl(id, url);
    }

    @PostMapping("/{id}/selfie")
    public CustomerResponse uploadSelfie(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        String url = imageUploadService.uploadImage(file);
        return customerService.updateSelfieUrl(id, url);
    }

}
