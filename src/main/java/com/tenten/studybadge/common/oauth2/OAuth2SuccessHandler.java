package com.tenten.studybadge.common.oauth2;

import com.tenten.studybadge.common.jwt.JwtTokenCreator;
import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.common.token.dto.TokenDto;
import com.tenten.studybadge.common.token.service.TokenService;
import com.tenten.studybadge.common.utils.CookieUtils;
import com.tenten.studybadge.member.domain.type.MemberRole;
import com.tenten.studybadge.type.member.MemberStatus;
import com.tenten.studybadge.type.member.Platform;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static com.tenten.studybadge.common.constant.Oauth2Contant.LOGIN_REDIRECT_URI;
import static com.tenten.studybadge.common.constant.Oauth2Contant.SIGN_UP_REDIRECT_URI;
import static com.tenten.studybadge.common.constant.TokenConstant.ACCESS_TOKEN;
import static com.tenten.studybadge.common.constant.TokenConstant.BEARER;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {


    private final JwtTokenCreator jwtTokenCreator;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Platform platform = userDetails.getPlatform();
        MemberRole role = userDetails.getRole();

        if(userDetails.getStatus() == MemberStatus.WAIT_FOR_APPROVAL) {

            TokenDto tokenDto = jwtTokenCreator.createToken(authentication.getName(), role, platform);
            String authorizationHeader = BEARER + tokenDto.getAccessToken();
            response.sendRedirect(SIGN_UP_REDIRECT_URI);
            response.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);

        } else {

            loginSuccess(response, userDetails);

        }
    }

    private void loginSuccess(HttpServletResponse response, CustomUserDetails userDetails) throws IOException {


        TokenDto tokenDto = tokenService.create(String.valueOf(userDetails.getId()), userDetails.getRole(), userDetails.getPlatform());

       String redirectUrl = UriComponentsBuilder.fromUriString(LOGIN_REDIRECT_URI)
                .queryParam(ACCESS_TOKEN, tokenDto.getAccessToken())
                .build().toUriString();

        ResponseCookie refreshCookie = CookieUtils.addCookie(tokenDto.getRefreshToken());
        String authorizationHeader = BEARER + tokenDto.getAccessToken();

        response.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.sendRedirect(redirectUrl);
    }
}