package com.example.vulnscanner.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern
            .compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    private static final Pattern SEQUENTIAL_PATTERN = Pattern.compile("(.)\\1{2,}"); // 3 or more identical sequential
                                                                                     // characters

    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (!NUMBER_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one number.");
        }
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one special character.");
        }
        if (SEQUENTIAL_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must not contain 3 or more identical sequential characters.");
        }
        // Dictionary check is omitted for simplicity, but can be added here.
    }
}
