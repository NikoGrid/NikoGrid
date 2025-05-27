package com.nikogrid.backend.auth;

import com.nikogrid.backend.services.BackendUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtGenerator jwtGenerator;

    private final BackendUserDetailsService userDetailsService;

    @Autowired
    public JwtAuthFilter(JwtGenerator jwtGenerator, BackendUserDetailsService userDetailsService) {
        this.jwtGenerator = jwtGenerator;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var token = getJwtFromRequest(request);
        try {
            final var email = jwtGenerator.getEmailFromToken(token);
            final var user = userDetailsService.loadUserByUsername(email);
            final var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (JwtException | IllegalArgumentException ignored) {
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        final var cookies = request.getCookies();
        if (cookies == null) return null;
        for (var cookie : cookies) {
            if (cookie.getName().equals(SecurityConfig.AUTH_COOKIE)) {
                final var token = cookie.getValue();
                if (token != null) {
                    return token;
                }
                break;
            }
        }
        return null;
    }
}
