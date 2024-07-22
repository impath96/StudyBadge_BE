package com.tenten.studybadge.common.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.tenten.studybadge.common.constant.Oauth2Contant.*;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = OAUTH2_ERROR_MESSAGE + exception.getMessage();
        log.info(errorMessage);
        request.setAttribute(OAUTH2_ERROR_MESSAGE_NAME, errorMessage);

        response.sendRedirect(request.getContextPath() + OAUTH2_REDIRECT_PATH);
    }
}