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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private AuthenticationService authenticationService;
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDTO> authenticateUser(@RequestBody @Valid AuthenticationDTO authenticationDTO) {
        RecoveryJwtTokenDTO token = authenticationService.authenticateUser(authenticationDTO);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterDTO registerDTO) {
        try{
            authenticationService.registerUser(registerDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }catch (DuplicateUserException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

}
