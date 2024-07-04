package com.tenten.studybadge.common.jwt;

import com.tenten.studybadge.member.dto.TokenDto;
import com.tenten.studybadge.type.member.Platform;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.*;

import static com.tenten.studybadge.common.constant.TokenConstant.ACCESS_TOKEN_EXPIRES_IN;
import static com.tenten.studybadge.common.constant.TokenConstant.REFRESH_TOKEN_EXPIRES_IN;

@Component
public class JwtTokenCreator {

    private final Key key;

    public JwtTokenCreator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto createToken(String email, Boolean isAdmin, Platform platform) {

        Claims commonClaims = Jwts.claims().setSubject(email);
        commonClaims.put("platform", platform);

        List<String> roles = isAdmin != null && isAdmin ? Arrays.asList("ROLE_USER", "ROLE_ADMIN") : Collections.singletonList("ROLE_USER");

        Claims accessTokenClaims = new DefaultClaims(commonClaims);
        accessTokenClaims.put("roles", roles);

        Claims refreshTokenClaims = new DefaultClaims(commonClaims);
        refreshTokenClaims.put("roles", roles);

        Instant now = Instant.now();
        Date accessTokenExpiresIn = Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN));
        Date refreshTokenExpiresIn = Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN));

        String accessToken = Jwts.builder()
                .setClaims(accessTokenClaims)
                .setSubject(email)
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
