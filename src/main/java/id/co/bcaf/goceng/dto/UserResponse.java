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
    private AccountStatus account_status;
    private RoleDto role;
    private BranchDto branch;
    private EmployeeDto employee;

    @Getter
    @Setter
    public static class RoleDto {
        private Integer id;      // If your Role entity uses Integer for id
        private String roleName;
    }

    @Getter
    @Setter
    public static class BranchDto {
        private UUID id;             // Changed to UUID to match entity
        private String name;
    }

    @Getter
    @Setter
    public static class EmployeeDto {
        private UUID id;             // Changed to UUID to match entity
        private String name;
    }
}
