package com.tenten.studybadge.common.jwt;

import com.tenten.studybadge.common.exception.InvalidTokenException;
import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.tenten.studybadge.common.constant.TokenConstant.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {

            String isLogout = (String) redisTemplate.opsForValue().get(LOGOUT_KEY + token);


            if (ObjectUtils.isEmpty(isLogout)) {

                Authentication authentication = jwtTokenProvider.getAuthentication(token);


                if (authentication != null) {

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    throw new NotFoundMemberException();
                }
            } else {
                throw new InvalidTokenException();
            }
        }
        filterChain.doFilter(request, response);
    }
    public String resolveToken(HttpServletRequest request) {

        String token = request.getHeader(AUTHORIZATION);
        if (token != null && token.startsWith(BEARER)) {

            token = token.substring(BEARER.length());
        }
        return token;
    }
}
