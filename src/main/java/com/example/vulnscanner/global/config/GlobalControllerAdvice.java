package com.example.vulnscanner.global.config;

import com.example.vulnscanner.module.user.NotificationService;
import com.example.vulnscanner.module.user.User;
import com.example.vulnscanner.module.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                // Check if user is Admin (by RoleTemplate or Legacy Role)
                // usage of deprecated getRole is acknowledged for backward compatibility
                boolean isAdmin = "ADMIN".equals(user.getRole()) ||
                        (user.getRoleTemplate() != null && "Admin".equalsIgnoreCase(user.getRoleTemplate().getName()));

                if (isAdmin) {
                    long uncheckedCount = notificationService.getUncheckedCount();
                    model.addAttribute("uncheckedNotificationCount", uncheckedCount);
                    model.addAttribute("notifications", notificationService.getUncheckedNotifications());
                }
            });
        }
    }
}
