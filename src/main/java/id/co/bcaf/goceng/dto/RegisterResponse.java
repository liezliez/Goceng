package id.co.bcaf.goceng.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterResponse {
    private UserResponse user;
    private CustomerResponse customer;
}
