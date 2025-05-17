package id.co.bcaf.goceng.controllers;

import id.co.bcaf.goceng.services.RoleFeatureService;
import id.co.bcaf.goceng.securities.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoleFeatureControllerTest {

    @InjectMocks
    private RoleFeatureController controller;

    @Mock
    private RoleFeatureService roleFeatureService;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testAddFeatureToRole_Success() {
        when(roleFeatureService.addFeatureToRole("ROLE_ADMIN", "FEATURE_X")).thenReturn(true);

        ResponseEntity<String> response = controller.addFeatureToRole("ROLE_ADMIN", "FEATURE_X");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Feature successfully added to role.", response.getBody());
        verify(roleFeatureService).addFeatureToRole("ROLE_ADMIN", "FEATURE_X");
    }

    @Test
    public void testAddFeatureToRole_AlreadyExists() {
        when(roleFeatureService.addFeatureToRole("ROLE_ADMIN", "FEATURE_X")).thenReturn(false);

        ResponseEntity<String> response = controller.addFeatureToRole("ROLE_ADMIN", "FEATURE_X");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Feature already associated with the role or invalid inputs.", response.getBody());
    }

    @Test
    public void testAddFeatureToRole_InvalidInput() {
        ResponseEntity<String> response = controller.addFeatureToRole("", "");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Role name and feature name must not be empty.", response.getBody());
        verifyNoInteractions(roleFeatureService);
    }

    @Test
    public void testRemoveFeatureFromRole_Success() {
        when(roleFeatureService.removeFeatureFromRole("ROLE_USER", "FEATURE_Y")).thenReturn(true);

        ResponseEntity<String> response = controller.removeFeatureFromRole("ROLE_USER", "FEATURE_Y");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Feature successfully removed from role.", response.getBody());
    }

    @Test
    public void testRemoveFeatureFromRole_NotFound() {
        when(roleFeatureService.removeFeatureFromRole("ROLE_USER", "FEATURE_Y")).thenReturn(false);

        ResponseEntity<String> response = controller.removeFeatureFromRole("ROLE_USER", "FEATURE_Y");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Feature not associated with the role or invalid inputs.", response.getBody());
    }

    @Test
    public void testHasFeature_Found() {
        when(roleFeatureService.hasFeature("ROLE_MANAGER", "FEATURE_Z")).thenReturn(true);

        ResponseEntity<String> response = controller.hasFeature("ROLE_MANAGER", "FEATURE_Z");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Role has the feature.", response.getBody());
    }

    @Test
    public void testHasFeature_NotFound() {
        when(roleFeatureService.hasFeature("ROLE_MANAGER", "FEATURE_Z")).thenReturn(false);

        ResponseEntity<String> response = controller.hasFeature("ROLE_MANAGER", "FEATURE_Z");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Role does not have the feature.", response.getBody());
    }

    @Test
    public void testGetFeaturesForCurrentUser_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(userDetailsServiceImpl.loadUserByUsername("user@example.com")).thenReturn(
                new User(
                        "user@example.com",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        when(roleFeatureService.getFeaturesByRole("ADMIN")).thenReturn(List.of("FEATURE_1", "FEATURE_2"));

        ResponseEntity<?> response = controller.getFeaturesForCurrentUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        assertEquals(2, ((List<?>) response.getBody()).size());
    }

    @Test
    public void testGetFeaturesForCurrentUser_Unauthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        ResponseEntity<?> response = controller.getFeaturesForCurrentUser();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not authenticated.", response.getBody());
    }

    @Test
    public void testGetFeaturesForCurrentUser_UserNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(userDetailsServiceImpl.loadUserByUsername("user@example.com")).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<?> response = controller.getFeaturesForCurrentUser();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    public void testGetFeaturesForCurrentUser_NoRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(userDetailsServiceImpl.loadUserByUsername("user@example.com")).thenReturn(
                new org.springframework.security.core.userdetails.User(
                        "user@example.com",
                        "password",
                        List.of() // no roles
                )
        );

        ResponseEntity<?> response = controller.getFeaturesForCurrentUser();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No role associated with the user.", response.getBody());
    }

    @Test
    public void testGetFeaturesForCurrentUser_NoFeatures() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(userDetailsServiceImpl.loadUserByUsername("user@example.com")).thenReturn(
                new org.springframework.security.core.userdetails.User(
                        "user@example.com",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        when(roleFeatureService.getFeaturesByRole("USER")).thenReturn(List.of());

        ResponseEntity<?> response = controller.getFeaturesForCurrentUser();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No features found for this role.", response.getBody());
    }
}
