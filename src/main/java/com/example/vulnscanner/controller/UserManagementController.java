package com.example.vulnscanner.controller;

import com.example.vulnscanner.entity.User;
import com.example.vulnscanner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UserManagementController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
        log.info("Received request to create user: {}, Role: {}", userDto.getUsername(), userDto.getRole());
        try {
            User user = userService.createUser(userDto.getUsername(), userDto.getPassword(), userDto.getRole(),
                    userDto.getName(), userDto.getTeam(), userDto.getEmail());
            log.info("User created successfully: {}", user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userService.updateUser(id, userDto.getName(), userDto.getTeam(), userDto.getEmail(), userDto.getRole());
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserDto {
        private String username;
        private String password;
        private String role;
        private String name;
        private String team;
        private String email;
    }
}
