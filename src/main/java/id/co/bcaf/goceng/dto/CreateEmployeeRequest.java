package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateEmployeeRequest {
    private UUID id_user;
    private Integer id_role;
}
