package com.tenten.studybadge.member.service;

import com.tenten.studybadge.common.jwt.JwtTokenCreator;
import com.tenten.studybadge.member.dto.TokenDto;
import com.tenten.studybadge.type.member.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenCreator jwtTokenCreator;
    private final RedisTemplate redisTemplate;

    public TokenDto create(String email, Platform platform) {

        TokenDto token = jwtTokenCreator.createToken(email, platform);

        String refreshToken = token.getRefreshToken();
        long refreshTokenExpiresIn = 7200000;

        redisTemplate.opsForValue().set(
                "RefreshToken: " + email + " : " + platform,
                refreshToken,
                refreshTokenExpiresIn,
                TimeUnit.MILLISECONDS);

        return token;
    }
}
