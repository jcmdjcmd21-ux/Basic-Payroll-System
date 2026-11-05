
package com.student.payroll.config;

import com.student.payroll.StaffAccount;
import com.student.payroll.repository.StaffAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            @Value("${ADMIN_USERNAME:admin}") String adminUsername,
            @Value("${ADMIN_PASSWORD:}") String adminPassword,
            StaffAccountRepository staffAccounts,
            PasswordEncoder encoder) {

        if (adminPassword == null || adminPassword.length() < 8) {
            throw new IllegalStateException(
                "ADMIN_PASSWORD must be configured with at least 8 characters."
            );
        }

        return username -> {
            // Admin account comes from Render environment variables.
            if (username.equals(adminUsername)) {
                return User.withUsername(adminUsername)
                    .password(encoder.encode(adminPassword))
                    .roles("ADMIN")
                    .build();
            }

            // Staff accounts come from the MySQL database.
            StaffAccount staff = staffAccounts.findByUsername(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Account not found.")
                );

            return User.withUsername(staff.getUsername())
                .password(staff.getPassword())
                .disabled(!staff.isEnabled())
                .roles("STAFF")
                .build();
        };
    }

    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN")
                );

            if (isAdmin) {
                response.sendRedirect("/");
            } else {
                response.sendRedirect("/staff/dashboard");
            }
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler successHandler)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/style.css",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/login"
                ).permitAll()

                .requestMatchers("/staff/**").hasRole("STAFF")

                // All existing payroll management pages remain Admin-only.
                .anyRequest().hasRole("ADMIN")
            )

            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
