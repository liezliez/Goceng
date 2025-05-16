package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class EmployeeDto {
    private UUID id;
    private String name;
    // add other employee fields you want to expose
}
