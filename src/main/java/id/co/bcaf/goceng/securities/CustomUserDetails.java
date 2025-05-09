package id.co.bcaf.goceng.securities;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final id.co.bcaf.goceng.models.User user;

    public CustomUserDetails(id.co.bcaf.goceng.models.User user) {
        super(user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName())));
        this.user = user;
    }

}
