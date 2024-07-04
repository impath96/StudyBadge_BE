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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.tenten.studybadge.common.constant.TokenConstant.BEARER;

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
        Collection<? extends GrantedAuthority> authorities = getRolesFromToken(accessToken)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(email, null, authorities);
    }

    public String resolveToken(HttpServletRequest request) {


        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith(BEARER)) {
            token = token.substring(BEARER.length());
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

    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("roles", List.class);
    }
}
