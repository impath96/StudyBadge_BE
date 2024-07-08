package com.tenten.studybadge.common.token.service;

import com.tenten.studybadge.common.jwt.JwtTokenCreator;
import com.tenten.studybadge.member.domain.type.MemberRole;
import com.tenten.studybadge.common.token.dto.TokenDto;
import com.tenten.studybadge.type.member.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.tenten.studybadge.common.constant.TokenConstant.REFRESH_TOKEN_EXPIRES_IN;
import static com.tenten.studybadge.common.constant.TokenConstant.REFRESH_TOKEN_FORMAT;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenCreator jwtTokenCreator;
    private final RedisTemplate redisTemplate;


    public TokenDto create(String email, MemberRole role, Platform platform) {


        TokenDto token = jwtTokenCreator.createToken(email, role, platform);

        String refreshToken = token.getRefreshToken();
        String key = String.format(REFRESH_TOKEN_FORMAT, email, platform);

        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                REFRESH_TOKEN_EXPIRES_IN,
                TimeUnit.MILLISECONDS);

        return token;
    }
}
