package com.eldoheiri.realtime_analytics.security;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtil {
    private String secretKey;

    public JWTUtil() {
        secretKey = System.getenv("JWT_SECRET_KEY");
    }

    public String generateTokenString(String sessionId) {
        return Jwts.builder()
        .claim("analytics", "allowed")
        .subject(sessionId)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 3))
        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), Jwts.SIG.HS256)
        .compact();
    }

    public String validateTokenAndExtractSubject(String token, String sessionId) {
        Jws<Claims> jwt = verifyAndParsePayload(token);
        if (!jwt.getPayload().getSubject().equals(sessionId)) {
            throw new JwtException("Invalid token");
        }

        if (jwt.getPayload().getExpiration().before(new Date())) {
            throw new ExpiredJwtException(jwt.getHeader(), jwt.getPayload(), "The token has expired!");
        }
        return jwt.getPayload().getSubject();
    }

    private Jws<Claims> verifyAndParsePayload(String token) {
        return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
        .build()
        .parseSignedClaims(token);
    }
}
