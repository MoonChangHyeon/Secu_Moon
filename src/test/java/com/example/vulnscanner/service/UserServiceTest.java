package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.User;
import com.example.vulnscanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        // Given
        String username = "testuser";
        String password = "password";
        String role = "USER";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User createdUser = userService.createUser(username, password, role, "Test Name", "Test Team",
                "test@example.com");

        // Then
        assertNotNull(createdUser);
        assertEquals(username, createdUser.getUsername());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals(role, createdUser.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateUsername_ThrowsException() {
        // Given
        String username = "existinguser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(username, "password", "USER", "Test Name", "Test Team", "test@example.com");
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Given
        Long userId = 1L;

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }
}
