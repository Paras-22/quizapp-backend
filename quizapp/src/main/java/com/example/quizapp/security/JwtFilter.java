package com.example.quizapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JwtFilter:
 * - Reads Authorization header
 * - Validates JWT using JwtUtil
 * - Extracts username and role from token
 * - Puts an Authentication into SecurityContext with ROLE_ prefix (ROLE_ADMIN / ROLE_PLAYER)
 *
 * Making sure to always prefix roles with "ROLE_" makes integration with hasRole(...) consistent.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Read Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Validate token
                if (jwtUtil.validateToken(token)) {
                    // Extract username & role from token
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token); // should return "ADMIN" or "PLAYER" etc.

                    // Normalize role to include ROLE_ prefix required by Spring Security's hasRole(...)
                    String grantedRole;
                    if (role == null || role.isBlank()) {
                        grantedRole = "ROLE_USER"; // fallback
                    } else {
                        grantedRole = role.startsWith("ROLE_") ? role : ("ROLE_" + role);
                    }

                    // Create authentication token with granted authority list
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(grantedRole))
                            );

                    // Put authentication into security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                // If token invalid/expired - clear context and continue (request will be rejected by security)
                SecurityContextHolder.clearContext();
                // Optional: log token exception
                logger.debug("JWT validation error: " + ex.getMessage());
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
