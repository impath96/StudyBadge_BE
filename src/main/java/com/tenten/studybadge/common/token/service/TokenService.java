package com.tenten.studybadge.common.token.service;

import com.tenten.studybadge.common.exception.InvalidTokenException;
import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.jwt.JwtTokenCreator;
import com.tenten.studybadge.common.jwt.JwtTokenProvider;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.member.domain.type.MemberRole;
import com.tenten.studybadge.common.token.dto.TokenDto;
import com.tenten.studybadge.type.member.Platform;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.tenten.studybadge.common.constant.TokenConstant.*;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenCreator jwtTokenCreator;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;


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

    public String reissue(String accessToken, String refreshToken) {

        if (accessToken.startsWith(BEARER)) {
            accessToken = accessToken.substring(7);
        } else {
            throw new InvalidTokenException();
        }

        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new InvalidTokenException();
        }

        long accessTokenExpiration = jwtTokenProvider.getExpiration(accessToken);
        if (accessTokenExpiration >= 0) {
            throw new InvalidTokenException();
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        Claims refreshTokenClaims = jwtTokenProvider.parseClaims(refreshToken);
        String email = refreshTokenClaims.getSubject();
        Platform platform = jwtTokenProvider.getPlatform(refreshToken);

        String storedRefreshToken = (String) redisTemplate.opsForValue().get(String.format(REFRESH_TOKEN_FORMAT, email, platform));
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidTokenException();
        }

        long refreshTokenExpiration = jwtTokenProvider.getExpiration(refreshToken);
        if (refreshTokenExpiration <= 0) {
            throw new InvalidTokenException();
        }

        Member member = memberRepository.findByEmailAndPlatform(email, platform).orElseThrow(NotFoundMemberException::new);

        return jwtTokenCreator.reissue(member.getEmail(), member.getRole(), platform);
    }
}
