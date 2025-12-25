package com.example.vulnscanner.global.config;

import com.example.vulnscanner.module.user.User;
import com.example.vulnscanner.module.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        User user = userService.loadUserEntityByUsername(username);

        String errorMessage = "Invalid username or password.";

        if (user != null) {
            if (user.getLockTime() != null) {
                errorMessage = "Account is locked. Please try again later.";
            } else {
                userService.increaseFailedAttempts(user);
                if (user.getLockTime() != null) {
                    errorMessage = "Account has been locked due to multiple failed login attempts.";
                } else {
                    errorMessage = "Invalid username or password.";
                }
            }
        } else {
            errorMessage = "Invalid username or password.";
        }

        // If the exception is already LockedException (thrown by UserDetailsService)
        if (exception instanceof LockedException) {
            errorMessage = "Account is locked. Please try again later.";
        }

        setDefaultFailureUrl("/login?error=true&message=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
        super.onAuthenticationFailure(request, response, exception);
    }
}