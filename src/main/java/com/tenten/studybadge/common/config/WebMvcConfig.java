package com.tenten.studybadge.common.config;

import com.tenten.studybadge.common.security.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginUserArgumentResolver loginUserArgumentResolver;

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {

        resolvers.add(loginUserArgumentResolver);
    }
}