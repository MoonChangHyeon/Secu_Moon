package com.example.vulnscanner.service;

import com.example.vulnscanner.entity.User;
import com.example.vulnscanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.vulnscanner.util.PasswordValidator passwordValidator;
    private final SettingsService settingsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getLockTime() != null) {
            int lockoutDuration = Integer
                    .parseInt(settingsService.getSetting(SettingsService.KEY_LOGIN_LOCKOUT_DURATION));
            if (user.getLockTime().plusMinutes(lockoutDuration).isBefore(java.time.LocalDateTime.now())) {
                user.setLockTime(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .accountLocked(user.getLockTime() != null)
                .build();
    }

    public User loadUserEntityByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public User createUser(String username, String password, String role, String name, String team, String email) {
        passwordValidator.validate(password);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setName(name);
        user.setTeam(team);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateUser(Long id, String name, String team, String email, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(name);
        user.setTeam(team);
        user.setEmail(email);
        user.setRole(role);
        userRepository.save(user);
    }

    // 초기 관리자 계정 생성 (필요 시 호출)
    @Transactional
    public void createAdminIfNotExists() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Admin password might not meet complexity requirements, so we bypass
            // validation for initial setup or ensure it meets them.
            // Here we bypass for simplicity or update the password to meet requirements.
            // Let's use a compliant password or bypass validation for this specific method
            // if needed.
            // For now, let's just create it directly without validation call since it's
            // internal.
            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("Admin123!")); // Changed to meet complexity
            user.setRole("ADMIN");
            user.setName("Administrator");
            user.setTeam("IT Security");
            user.setEmail("admin@example.com");
            userRepository.save(user);
        }
    }

    @Transactional
    public void increaseFailedAttempts(User user) {
        int maxAttempts = Integer.parseInt(settingsService.getSetting(SettingsService.KEY_LOGIN_MAX_ATTEMPTS));
        int newFailures = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailures);

        if (newFailures >= maxAttempts) {
            user.setLockTime(java.time.LocalDateTime.now());
        }
        userRepository.save(user);
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        });
    }
}
