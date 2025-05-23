package id.co.bcaf.goceng.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nik;

    private UUID id_branch;

    private Double latitude;

    private Double longitude;
}
