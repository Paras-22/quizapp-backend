package com.example.quizapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/users/login", "/users/register").permitAll()

                        // Admin-only tournament management
                        .requestMatchers(HttpMethod.POST, "/tournaments/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tournaments/**").hasRole("ADMIN")

                        // Tournament GETs - allow any authenticated user (both PLAYER and ADMIN)
                        .requestMatchers(HttpMethod.GET, "/tournaments/**").authenticated()

                        // Admin-only question management
                        .requestMatchers(HttpMethod.POST, "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")

                        // Player-only endpoints
                        .requestMatchers("/player/**").hasRole("PLAYER")
                        .requestMatchers(HttpMethod.POST, "/tournaments/like/**", "/tournaments/unlike/**").hasRole("PLAYER")

                        // Role check endpoints - any authenticated user
                        .requestMatchers("/users/is-admin/**", "/users/is-player/**").authenticated()

                        // Any other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
