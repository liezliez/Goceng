package id.co.bcaf.goceng.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private UUID branchId;
}
