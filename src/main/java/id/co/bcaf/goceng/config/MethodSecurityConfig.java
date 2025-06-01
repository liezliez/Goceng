package id.co.bcaf.goceng.config;


import id.co.bcaf.goceng.securities.RolePermissionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Configures method-level security using a custom PermissionEvaluator.
 * This configuration enables @PreAuthorize and @PostAuthorize annotations,
 * and integrates a RolePermissionEvaluator to handle fine-grained permission checks
 * based on role-feature mappings.
 */

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {

    @Autowired
    private RolePermissionEvaluator rolePermissionEvaluator;

    protected DefaultMethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(new PermissionEvaluator() {
            @Override
            public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                return rolePermissionEvaluator.hasRoleFeaturePermission(permission.toString());
            }

            @Override
            public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                return rolePermissionEvaluator.hasRoleFeaturePermission(permission.toString());
            }
        });
        return handler;
    }
}
