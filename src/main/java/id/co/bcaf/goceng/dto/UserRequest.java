package id.co.bcaf.goceng.dto;

import id.co.bcaf.goceng.enums.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private AccountStatus account_status; // Enum

    private Integer idRole; // Use Integer (nullable)
    private UUID idBranch; // Use UUID for nullable branch ID
}
