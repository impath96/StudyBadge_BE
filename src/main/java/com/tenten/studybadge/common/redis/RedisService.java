package com.tenten.studybadge.common.redis;

import com.tenten.studybadge.common.exception.InvalidTokenException;
import com.tenten.studybadge.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.tenten.studybadge.common.constant.TokenConstant.*;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public String generateAuthCode() {
        String result;

        do {
            int num = 0;
            try {
                num = SecureRandom.getInstanceStrong().nextInt(999999);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            result = String.valueOf(num);
        } while (result.length() != 6);

        return result;
    }

    public void saveAuthCode(String email, String authCode) {
        redisTemplate.opsForValue().set(EMAIL_KEY + email, authCode, 5, TimeUnit.MINUTES);
    }

    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get(EMAIL_KEY + email);
    }

    public void deleteAuthCode(String email) {
        redisTemplate.delete(EMAIL_KEY + email);
    }


    public void blackList(String accessToken) {

        long expiration = jwtTokenProvider.getExpiration(accessToken);
        long now = Instant.now().toEpochMilli();

        long accessTokenExpiresIn = expiration - now;

        if (accessTokenExpiresIn > 0) {

            redisTemplate.opsForValue().set(
                    LOGOUT_KEY + accessToken,
                    LOGOUT_VALUE,
                    accessTokenExpiresIn,
                    TimeUnit.MILLISECONDS);
        } else {
            throw new InvalidTokenException();
        }
    }
}
