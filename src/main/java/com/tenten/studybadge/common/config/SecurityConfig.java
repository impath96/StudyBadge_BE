package com.tenten.studybadge.common.config;

import com.tenten.studybadge.common.jwt.JwtTokenFilter;
import com.tenten.studybadge.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests( requests -> requests
                        .requestMatchers("/api/members/sign-up", "/api/members/auth/**", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**",
                            "/api/members/login/**", "/health-check").permitAll()
                        .requestMatchers("/api/study-channels/**", "/error", "/api/participation/**").permitAll()
                .requestMatchers("/api/members/logout", "api/study-channels/*/places",
                    "/api/study-channels/*/schedules", "/api/token/re-issue").hasRole("USER"))


                .cors(cors -> new CorsConfig())
                .headers(headers -> headers // h2-console 페이지 접속을 위한 설정
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self'")))

                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
  
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
