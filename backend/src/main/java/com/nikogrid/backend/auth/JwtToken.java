package com.nikogrid.backend.auth;

import lombok.Getter;

@Getter
public class JwtToken {
    private final String token;
    private final int expirationTime;

    public JwtToken(String token, int expirationTime) {
        this.token = token;
        this.expirationTime = expirationTime;
    }

}
