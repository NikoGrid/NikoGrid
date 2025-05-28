package com.nikogrid.backend.controllers;

import com.nikogrid.backend.dto.AuthenticationDTO;
import com.nikogrid.backend.dto.RecoveryJwtTokenDTO;
import com.nikogrid.backend.dto.RegisterDTO;
import com.nikogrid.backend.exceptions.DuplicateUserException;
import com.nikogrid.backend.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthenticationController {
    private AuthenticationService authenticationService;
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RecoveryJwtTokenDTO> authenticateUser(@Valid @RequestBody AuthenticationDTO authenticationDTO) {
        RecoveryJwtTokenDTO token = authenticationService.authenticateUser(authenticationDTO);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
        try{
            authenticationService.registerUser(registerDTO);
        }catch (DuplicateUserException e) {
            throw new DuplicateUserException("User already exists");
        }
    }

}
