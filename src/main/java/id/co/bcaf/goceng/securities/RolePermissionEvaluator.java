package id.co.bcaf.goceng.securities;

import id.co.bcaf.goceng.services.RoleFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionEvaluator {

    @Autowired
    private RoleFeatureService roleFeatureService;

    public boolean hasRoleFeaturePermission(String featureName) {
        // Get current authenticated user
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String roleName = user.getAuthorities().iterator().next().getAuthority();

        return roleFeatureService.hasFeature(roleName, featureName);
    }
}
