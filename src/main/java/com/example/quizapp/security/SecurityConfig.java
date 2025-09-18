package com.example.quizapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/users/login", "/users/register",
                                "/users/password-reset-request",
                                "/users/reset-password").permitAll()

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

                        // Mixed access endpoints
                        .requestMatchers(HttpMethod.GET, "/tournaments/**").authenticated()
                        .requestMatchers("/users/stats", "/users/profile").authenticated()

                        // Role check endpoints - any authenticated user can call
                        .requestMatchers("/users/is-admin/**", "/users/is-player/**").authenticated()

                        // All others need to be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}