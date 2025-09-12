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

                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.POST, "/tournaments/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")

                        // Player-only endpoints
                        .requestMatchers(HttpMethod.POST, "/tournaments/like/**", "/tournaments/unlike/**").hasRole("PLAYER")
                        .requestMatchers("/player/**").hasRole("PLAYER")

                        // Role check endpoints - any authenticated user can call
                        .requestMatchers("/users/is-admin/**", "/users/is-player/**").authenticated()

                        // All others need to be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
