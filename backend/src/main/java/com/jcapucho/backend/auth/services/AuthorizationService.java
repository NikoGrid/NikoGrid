package com.jcapucho.backend.auth.services;

import com.jcapucho.backend.auth.entities.User;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import com.jcapucho.backend.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User " + username + "Not Found"));
        return new UserDetailsImpl(user);
    }
}
