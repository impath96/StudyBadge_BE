package com.tenten.studybadge.common.jwt;

import com.tenten.studybadge.common.security.CustomUserDetailService;
import com.tenten.studybadge.type.member.Platform;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static com.tenten.studybadge.common.constant.TokenConstant.PLATFORM;


@Component
public class JwtTokenProvider {

    private final Key key;
    private final CustomUserDetailService customUserDetailService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailService customUserDetailService) {
        this.customUserDetailService = customUserDetailService;

        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    public Authentication getAuthentication(String accessToken) {

        UserDetails userDetails = customUserDetailService.loadUserByUsername(getUsername(accessToken));

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        Claims claims = parseClaims(token);

        return claims.getSubject();
    }

    public boolean validateToken(String token) {

    try {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        return true;
    } catch (Exception e) {

        return false;

        }
    }

    public Claims parseClaims(String accessToken) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Platform getPlatform(String token) {

        Claims claimsFromToken = parseClaims(token);
        String platform = claimsFromToken.get(PLATFORM, String.class);

        return Platform.valueOf(platform);
    }

    public long getExpiration(String token) {

        Claims claims = parseClaims(token);
        Date expirationDate = claims.getExpiration();
        return expirationDate.getTime();
    }
}
