package com.api.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


public class JwtUtil {


        private static final String SECRET = "mysecretkeymysecretkeymysecretkey123";

        private final SecretKey key =
                Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        public Claims validateToken(final String token) {
            try {
                return Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e) {
                throw new RuntimeException("Invalid JWT Token", e);
            }
        }
        public boolean isRequestAuthorized(String path, Claims claims) {
//        String role = claims.get("role", String.class);
//
//        if (path.startsWith("/student/**") && !role.equals("ADMIN")) {
//            return false;
//        }

            return true;
        }






}
