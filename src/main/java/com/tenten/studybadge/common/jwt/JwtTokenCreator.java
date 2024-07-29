package com.tenten.studybadge.common.jwt;

import com.tenten.studybadge.type.member.MemberRole;
import com.tenten.studybadge.common.token.dto.TokenDto;
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

import static com.tenten.studybadge.common.constant.TokenConstant.*;

@Component
public class JwtTokenCreator {

    private final Key key;

    public JwtTokenCreator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto createToken(String memberId, MemberRole role, Platform platform) {

        Claims commonClaims = Jwts.claims().setSubject(memberId);
        commonClaims.put(PLATFORM, platform);


        List<String> roles = Arrays.asList(ROLE_PREFIX + role.name());

        Claims accessTokenClaims = new DefaultClaims(commonClaims);
        accessTokenClaims.put(ROLE, roles);

        Claims refreshTokenClaims = new DefaultClaims(commonClaims);
        refreshTokenClaims.put(ROLE, roles);

        Instant now = Instant.now();
        Date accessTokenExpiresIn = Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN));
        Date refreshTokenExpiresIn = Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN));

        String accessToken = Jwts.builder()
                .setClaims(accessTokenClaims)
                .setIssuedAt(Date.from(now))
                .setSubject(memberId)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        String refreshToken = Jwts.builder()
                .setClaims(refreshTokenClaims)
                .setIssuedAt(Date.from(now))
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String reissue(String memberId, MemberRole role, Platform platform) {

        Claims claims = Jwts.claims().setSubject(memberId);
        claims.put(PLATFORM, platform);
        List<String> roles = Arrays.asList(ROLE_PREFIX + role.name());
        claims.put(ROLE, roles);

        Instant now = Instant.now();
        Date accessTokenExpiresIn = Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
