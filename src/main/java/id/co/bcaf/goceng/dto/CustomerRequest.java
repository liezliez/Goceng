package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CustomerRequest {
    private String userId;
    private String nik;
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String telpNo;
    private String address;
    private String motherMaidenName;
    private String occupation;
    private BigDecimal salary;
    private String homeOwnershipStatus;
    private String emergencyCall;
    private BigDecimal creditLimit;
    private String accountNo;
}
