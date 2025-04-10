package id.co.bcaf.goceng.dto;

import id.co.bcaf.goceng.enums.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {
    private String name;
    private String branch;
    private WorkStatus workStatus; // assuming you're using an enum here
}
