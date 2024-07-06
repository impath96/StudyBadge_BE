package com.tenten.studybadge.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

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
        redisTemplate.opsForValue().set("email: " + email, authCode, 5, TimeUnit.MINUTES);
    }

    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get("email: " + email);
    }

    public void deleteAuthCode(String email) {
        redisTemplate.delete("email: " + email);
    }
}
