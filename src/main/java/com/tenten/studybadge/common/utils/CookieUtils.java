package com.tenten.studybadge.common.utils;

import org.springframework.http.ResponseCookie;

public class CookieUtils {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String DOMAIN = "localhost";
    private static final String PATH = "/";

    public static ResponseCookie addCookie(String value) {

        return ResponseCookie.from(ACCESS_TOKEN, value)
                .httpOnly(true)
                .domain(DOMAIN)
                .maxAge(3600)
                .secure(true)
                .path(PATH)
                .build();
    }

    public static ResponseCookie deleteCookie(String value) {

        return ResponseCookie.from(ACCESS_TOKEN, value)
                .httpOnly(true)
                .domain(DOMAIN)
                .maxAge(0)
                .path(PATH)
                .build();
    }
}
