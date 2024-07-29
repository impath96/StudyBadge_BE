package com.tenten.studybadge.common.utils;

import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;

import static com.tenten.studybadge.common.constant.TokenConstant.REFRESH_TOKEN;
import static com.tenten.studybadge.common.constant.TokenConstant.REFRESH_TOKEN_EXPIRES_IN_COOKIE;

public class CookieUtils {
    private static final String DOMAIN = "study-badge.shop";
    private static final String PATH = "/";

    public static ResponseCookie addCookie(String value) {

        return ResponseCookie.from(REFRESH_TOKEN, value)
                .httpOnly(true)
                .domain(DOMAIN)
                .maxAge(REFRESH_TOKEN_EXPIRES_IN_COOKIE)
                .sameSite(Cookie.SameSite.NONE.attributeValue())
                .secure(true)
                .path(PATH)
                .build();
    }

    public static ResponseCookie deleteCookie(String value) {

        return ResponseCookie.from(REFRESH_TOKEN, value)
                .httpOnly(true)
                .domain(DOMAIN)
                .maxAge(0)
                .sameSite(Cookie.SameSite.NONE.attributeValue())
                .path(PATH)
                .build();
    }
}
