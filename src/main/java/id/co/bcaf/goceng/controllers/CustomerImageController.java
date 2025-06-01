package id.co.bcaf.goceng.controllers;


import id.co.bcaf.goceng.dto.CustomerResponse;
import id.co.bcaf.goceng.services.CustomerService;
import id.co.bcaf.goceng.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for uploading customer images.
 *
 * Provides endpoints to upload and update:
 * - Customer KTP image ({@link #uploadKtp})
 * - Customer selfie image ({@link #uploadSelfie})
 *
 * Images are uploaded via multipart file requests, and the returned
 * {@link CustomerResponse} reflects the updated customer information with
 * new image URLs.
 */

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
