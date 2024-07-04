package com.tenten.studybadge.common.jwt;

import com.tenten.studybadge.type.member.Platform;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {

        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    public Authentication getAuthentication(String accessToken) {

        Claims claims = parseClaims(accessToken);
        String email = claims.getSubject();

        return new UsernamePasswordAuthenticationToken(email, null);
    }

    public String resolveToken(HttpServletRequest request) {
        final String bearer = "Bearer ";

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith(bearer)) {
            token = token.substring(bearer.length());
        }
        return token;
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
        String platform = claimsFromToken.get("platform", String.class);

        return Platform.valueOf(platform);
    }

    public long getExpiration(String token) {

        Claims claims = parseClaims(token);
        Date expirationDate = claims.getExpiration();
        return expirationDate.getTime();
    }
}
