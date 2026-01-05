package com.example.vulnscanner.module.user;

import com.example.vulnscanner.global.util.PasswordValidator;
import com.example.vulnscanner.module.settings.SettingsService;

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
    private final com.example.vulnscanner.global.util.PasswordValidator passwordValidator;
    private final SettingsService settingsService;
    private final RoleTemplateService roleTemplateService; // Added
    private final NotificationService notificationService; // Added

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

        boolean enabled = "APPROVED".equals(user.getStatus()) || "ADMIN".equals(user.getRole());

        List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
        if (user.getRoleTemplate() != null) {
            user.getRoleTemplate().getPrivileges().forEach(p -> authorities
                    .add(new org.springframework.security.core.authority.SimpleGrantedAuthority(p.getName())));
        } else if (user.getRole() != null) {
            authorities.add(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(user.getLockTime() != null)
                .disabled(!enabled) // Block if not approved
                .build();
    }

    public User loadUserEntityByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // Used for Admin Creation or Manual Add
    @Transactional
    public User createUser(String username, String password, String role, String name, String team, String email) {
        passwordValidator.validate(password);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role); // Legacy
        user.setName(name);
        user.setTeam(team);
        user.setEmail(email);
        user.setStatus("APPROVED"); // Admin created users are auto-approved? Or passed as arg? Assuming APPROVED
                                    // for backend create.
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(String username, String password, String name, String team, String email) {
        passwordValidator.validate(password);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); // Default legacy role
        user.setName(name);
        user.setTeam(team);
        user.setEmail(email);
        user.setStatus("PENDING");

        User savedUser = userRepository.save(user);

        // Notify Admins
        notificationService.createNotification(
                "New User Registration: " + username,
                "/user/list",
                "SIGNUP_REQUEST");

        return savedUser;
    }

    @Transactional
    public void approveUser(Long userId, Long roleTemplateId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleTemplate template = roleTemplateService.getTemplate(roleTemplateId);

        user.setStatus("APPROVED");
        user.setRoleTemplate(template);
        userRepository.save(user);
    }

    @Transactional
    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus("REJECTED");
        userRepository.save(user);
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
            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("Admin123!"));
            user.setRole("ADMIN");
            user.setName("Administrator");
            user.setTeam("IT Security");
            user.setEmail("admin@example.com");
            user.setStatus("APPROVED");
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