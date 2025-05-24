package com.jcapucho.backend.auth.configs;

import com.jcapucho.backend.auth.repository.UserRepository;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserAuthenticationFilter userAuthenticationFilter;

    public SecurityConfiguration(UserAuthenticationFilter userAuthenticationFilter) {
        this.userAuthenticationFilter = userAuthenticationFilter;
    }

    public static final String [] ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED = {
            "/api/auth/login",
            "/api/auth/register"
    };

    private final String [] ENDPOINTS_WITH_AUTHENTICATION_REQUIRED = {
    };

    private final String [] ENDPOINTS_ADMIN = {

    };

    private final String [] ENDPOINTS_OWNER = {
            "/api/auth/owner",
    };

    private final String [] ENDPOINTS_COLLABORATOR = {
            "/api/auth/collaborator",
    };


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, ENDPOINTS_WITH_AUTHENTICATION_NOT_REQUIRED).permitAll()
                        .requestMatchers(HttpMethod.POST, "api/auth/customers").authenticated()
                        .requestMatchers(HttpMethod.POST, ENDPOINTS_ADMIN).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, ENDPOINTS_OWNER).hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, ENDPOINTS_COLLABORATOR).hasRole("COLLABORATOR")
                        .anyRequest().denyAll())
                .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
