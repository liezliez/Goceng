package id.co.bcaf.goceng.securities;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final id.co.bcaf.goceng.models.User user;

    public CustomUserDetails(id.co.bcaf.goceng.models.User user) {
        super(
                user.getEmail(),
                user.getPassword(),
                buildAuthorities(user)
        );
        this.user = user;
    }

    private static List<SimpleGrantedAuthority> buildAuthorities(id.co.bcaf.goceng.models.User user) {
        String roleName = user.getRole().getRoleName();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}
