package com.tenten.studybadge.common.jwt;

import com.tenten.studybadge.member.dto.TokenDto;
import com.tenten.studybadge.type.member.Platform;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenCreator {

    private final Key key;

    public JwtTokenCreator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto createToken(String email, Platform platform) {

        Claims accessTokenClaims = Jwts.claims().setSubject(email);
        accessTokenClaims.put("platform", platform);

        Claims refreshTokenClaims = Jwts.claims().setSubject(email);
        refreshTokenClaims.put("platform", platform);

        Instant now = Instant.now();
        Date accessTokenExpiresIn = Date.from(now.plus(1, ChronoUnit.HOURS));
        Date refreshTokenExpiresIn = Date.from(now.plus(2,ChronoUnit.HOURS));

        String accessToken = Jwts.builder()
                .setClaims(accessTokenClaims)
                .setSubject(String.valueOf(email))
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        String refreshToken = Jwts.builder()
                .setClaims(refreshTokenClaims)
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();


    }
}
