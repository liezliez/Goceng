package id.co.bcaf.goceng.dto;

import id.co.bcaf.goceng.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private AccountStatus account_status; // Enum
    private String role;
}
