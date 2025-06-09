package com.nikogrid.backend.controllers;

import com.nikogrid.backend.auth.JwtGenerator;
import com.nikogrid.backend.auth.SecurityConstants;
import com.nikogrid.backend.dto.AuthResponse;
import com.nikogrid.backend.dto.LoginDTO;
import com.nikogrid.backend.dto.RegisterDTO;
import com.nikogrid.backend.dto.UserDTO;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final boolean secureCookies;

    @Autowired
    public AuthenticationController(
            AuthenticationService authenticationService,
            AuthenticationManager authenticationManager,
            JwtGenerator jwtGenerator,
            @Value("${jwt.secure-cookies:false}") boolean secureCookies
    ) {
        this.authenticationService = authenticationService;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
        this.secureCookies = secureCookies;
    }

    @PostMapping("/login")
    @PreAuthorize("!isAuthenticated()")
    public AuthResponse authenticateUser(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody LoginDTO loginDTO) {
        final var authenticationRequest = UsernamePasswordAuthenticationToken
                .unauthenticated(loginDTO.getEmail(), loginDTO.getPassword());
        final var authentication = this.authenticationManager.authenticate(authenticationRequest);

        if (authentication == null) {
            throw new BadCredentialsException("Invalid Credentials");
        }

        final var token = this.jwtGenerator.generateToken(authentication.getName());

        final var cookie = configureCookie(token.token(), token.expirationTime(), request.isSecure());
        response.addCookie(cookie);

        return new AuthResponse();
    }

    @PostMapping("/register")
    @PreAuthorize("!isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerUser(@Valid @RequestBody RegisterDTO request) {
        authenticationService.registerUser(request);
        return new AuthResponse();
    }

    @GetMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {

        final var cookie = configureCookie(null, 0, request.isSecure());
        response.addCookie(cookie);
        return new AuthResponse();
    }

    @GetMapping("/me")
    @PostMapping("isAuthenticated()")
    public UserDTO getInfo() throws ResourceNotFound {
        final var principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return UserDTO.fromUser(
                this.authenticationService.
                        getUserByEmail(principal).
                        orElseThrow(ResourceNotFound::new)
        );
    }


    private Cookie configureCookie(String token, int maxAge, boolean isSecure) {
        final var cookie = new Cookie(SecurityConstants.AUTH_COOKIE, token);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setSecure(secureCookies || isSecure);
        cookie.setAttribute("SameSite", SameSite.STRICT.toString());
        return cookie;
    }
}
