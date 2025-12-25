package com.example.vulnscanner.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomAuthenticationFailureHandler failureHandler;
        private final CustomAuthenticationSuccessHandler successHandler;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/h2-console/**",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/settings/users/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(successHandler)
                                                .failureHandler(failureHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .expiredUrl("/login?expired"))
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**") // H2 console requires
                                                                                           // disabling CSRF
                                )
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // H2 console
                                                                                                         // requires
                                                                                                         // frame
                                                                                                         // options
                                );

                return http.build();
        }

}