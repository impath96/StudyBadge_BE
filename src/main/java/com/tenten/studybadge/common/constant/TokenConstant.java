package com.tenten.studybadge.common.constant;

import java.util.concurrent.TimeUnit;

public class TokenConstant {

    public static final String BEARER = "Bearer ";

    public static final String ROLE = "roles";

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String PLATFORM = "platform";

    public static final String EMAIL_KEY = "email: ";

    public static final String LOGOUT_KEY = "logout: ";

    public static final String LOGOUT_VALUE = "logout";

    public static final String AUTHORIZATION = "Authorization";

    public static final String REFRESH_TOKEN_FORMAT = "RefreshToken: %s : %s";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String ACCESS_TOKEN = "accessToken";

    public static final long ACCESS_TOKEN_EXPIRES_IN = TimeUnit.HOURS.toMillis(1);

    public static final long REFRESH_TOKEN_EXPIRES_IN = TimeUnit.DAYS.toMillis(14);

    public static final long REFRESH_TOKEN_EXPIRES_IN_COOKIE = TimeUnit.DAYS.toSeconds(14);

}
