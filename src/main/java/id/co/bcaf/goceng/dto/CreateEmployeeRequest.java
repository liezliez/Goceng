package id.co.bcaf.goceng.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateEmployeeRequest {
    private UUID id_user;
    private Integer id_role;

    private String name;
    private String email;
    private UUID branchId;
}
