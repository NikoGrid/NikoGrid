package com.nikogrid.backend.auth;

public record JwtToken(String token, int expirationTime) {

}
