package com.nikogrid.backend;

import com.nikogrid.backend.auth.JwtAuthEntryPoint;
import com.nikogrid.backend.auth.JwtAuthFilter;
import com.nikogrid.backend.auth.JwtGenerator;
import com.nikogrid.backend.repositories.UserRepository;
import com.nikogrid.backend.services.BackendUserDetailsService;
import com.nikogrid.backend.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerExceptionResolver;

@TestConfiguration
public class TestSecurityBeans {
    @Bean
    public JwtAuthEntryPoint authEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
        return new JwtAuthEntryPoint(handlerExceptionResolver);
    }

    @Bean
    public JwtGenerator jwtGenerator(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration-seconds}") int expirationSeconds) {
        return new JwtGenerator(secretKey, expirationSeconds);
    }

    @Bean
    @ConditionalOnBean(JwtAuthFilter.class)
    public BackendUserDetailsService backendUserDetailsService(UserRepository userRepository) {
        return new UserDetailsServiceImpl(userRepository);
    }
}
