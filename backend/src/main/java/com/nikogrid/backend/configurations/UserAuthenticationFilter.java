package com.nikogrid.backend.configurations;

import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.entities.UserDetailsImpl;
import com.nikogrid.backend.repositories.UserRepository;
import com.nikogrid.backend.services.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class UserAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenService jwtTokenService;
    private UserRepository userRepository;
    private RequestMatcher publicEndpoints;

    @Autowired
    public UserAuthenticationFilter(JwtTokenService jwtTokenService, UserRepository userRepository) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;

        RequestMatcher[] matchers = Arrays.stream(SecurityConfig.ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED)
                .map(AntPathRequestMatcher::new)
                .toArray(RequestMatcher[]::new);

        this.publicEndpoints = new OrRequestMatcher(matchers);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isProtectedEndpoint(request)) {
            String token = recoveryToken(request);
            if (token != null) {
                try {
                    String subject = jwtTokenService.validateToken(token);
                    User user = userRepository.findByEmail(subject)
                            .orElseThrow(() -> new UsernameNotFoundException("User with email " + subject + " not found"));;
                    UserDetailsImpl userDetails = new UserDetailsImpl(user);

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        filterChain.doFilter(request, response);
    }


    private String recoveryToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null ) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

    private boolean isProtectedEndpoint(HttpServletRequest request) {
        return !publicEndpoints.matches(request);
    }

}

