package com.tenten.studybadge.common.token.controller;

import com.tenten.studybadge.common.token.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
@Tag(name = "Token API", description = "Token API")
public class TokenController {

    private final TokenService tokenService;
    @Operation(summary = "토큰 재발급", description = "토큰 재발급", security = @SecurityRequirement(name = "bearerToken"))
    @PostMapping("/re-issue")
    public ResponseEntity<String> reissue(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {

        String newAccessToken = tokenService.reissue(refreshToken);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Authorization", "Bearer " + newAccessToken)
                .body(newAccessToken);
    }
}
