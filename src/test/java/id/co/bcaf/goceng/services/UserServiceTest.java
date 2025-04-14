//package id.co.bcaf.goceng.services;
//
//import id.co.bcaf.goceng.enums.AccountStatus;
//import id.co.bcaf.goceng.models.Role;
//import id.co.bcaf.goceng.models.User;
//import id.co.bcaf.goceng.repositories.RoleRepository;
//import id.co.bcaf.goceng.repositories.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class UserServiceTest {
//
//    private UserRepository userRepository;
//    private RoleRepository roleRepository;
//    private PasswordEncoder passwordEncoder;
//    private UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        userRepository = mock(UserRepository.class);
//        roleRepository = mock(RoleRepository.class);
//        passwordEncoder = mock(PasswordEncoder.class);
//        userService = new UserService(userRepository, roleRepository, passwordEncoder);
//    }
//
//    @Test
//    void testCreateUser_success() {
//        // Arrange
//        Integer roleId = 2;
//        Role role = new Role();
//        role.setId_role(roleId);
//
//        User userToCreate = new User();
//        userToCreate.setEmail("test@example.com");
//        userToCreate.setName("Test User");
//        userToCreate.setPassword("plaintext");
//        userToCreate.setRole(role); // only ID is needed for lookup
//
//        String encodedPassword = "encoded-password";
//        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
//        when(passwordEncoder.encode("plaintext")).thenReturn(encodedPassword);
//
//        User savedUser = new User();
//        savedUser.setIdUser(UUID.randomUUID());
//        savedUser.setEmail("test@example.com");
//        savedUser.setName("Test User");
//        savedUser.setPassword(encodedPassword);
//        savedUser.setRole(role);
//        savedUser.setAccountStatus(AccountStatus.ACTIVE);
//
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//
//        // Act
//        User createdUser = userService.createUser(userToCreate);
//
//        // Assert
//        assertNotNull(createdUser);
//        assertEquals("test@example.com", createdUser.getEmail());
//        assertEquals("Test User", createdUser.getName());
//        assertEquals(AccountStatus.ACTIVE, createdUser.getAccountStatus());
//        assertEquals(encodedPassword, createdUser.getPassword());
//        assertEquals(role, createdUser.getRole());
//
//        verify(roleRepository).findById(roleId);
//        verify(passwordEncoder).encode("plaintext");
//        verify(userRepository).save(any(User.class));
//    }
//}
