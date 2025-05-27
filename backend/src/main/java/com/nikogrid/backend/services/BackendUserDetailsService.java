package com.nikogrid.backend.services;

import com.nikogrid.backend.entities.BackendUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface BackendUserDetailsService extends UserDetailsService {

    BackendUserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

}
