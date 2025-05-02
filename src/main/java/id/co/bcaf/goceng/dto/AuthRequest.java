package id.co.bcaf.goceng.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotNull(message = "Email cannot be null")  // Ensure email is not null
    @Email(message = "Please provide a valid email")  // Ensure email follows proper format
    private String email;

    @NotNull(message = "Password cannot be null")  // Ensure password is not null
    @Size(min = 6, message = "Password must be at least 6 characters long")  // Ensure password is at least 6 characters
    private String password;

    public AuthRequest() {}

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
