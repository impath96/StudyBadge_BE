package com.tenten.studybadge.member.controller;

import com.tenten.studybadge.common.jwt.JwtTokenProvider;
import com.tenten.studybadge.member.dto.MemberLoginRequest;
import com.tenten.studybadge.member.dto.MemberSignUpRequest;
import com.tenten.studybadge.member.dto.TokenCreateDto;
import com.tenten.studybadge.member.dto.TokenDto;
import com.tenten.studybadge.member.service.MemberService;
import com.tenten.studybadge.member.service.TokenService;
import com.tenten.studybadge.type.member.Platform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static com.tenten.studybadge.type.member.Platform.LOCAL;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member API", description = "Member API")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    @Operation(summary = "회원가입", description = "회원가입")
    @Parameter(name = "signUpRequest", description = "회원가입 요청 Dto" )
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@Valid @RequestPart(value = "signUpRequest") MemberSignUpRequest signUpRequest) {

        memberService.signUp(signUpRequest, LOCAL);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @Operation(summary = "인증", description = "인증 요청")
    @Parameter(name = "email", description = "이메일")
    @Parameter(name = "code", description = "인증코드")
    @PostMapping("/auth")
    public ResponseEntity<Void> auth(@RequestParam(name = "email") String email,
                                     @RequestParam(name = "code") String code) {

        memberService.auth(email, code, LOCAL);
        return ResponseEntity.status(HttpStatus.OK).build();

    }
    @Operation(summary = "로그인", description = "일반 로그인")
    @Parameter(name = "loginRequest", description = "로그인 요청 Dto")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody MemberLoginRequest loginRequest,
                                                              HttpServletResponse response) {

        TokenCreateDto createDto = memberService.login(loginRequest, LOCAL);
        TokenDto tokenDto = tokenService.create(createDto.getEmail(), LOCAL);

        Cookie cookie = new Cookie("accessToken", tokenDto.getAccessToken());
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setMaxAge(3600);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).body(tokenDto);
    }
    @Operation(summary = "로그아웃", description = "로그아웃" , security = @SecurityRequirement(name = "bearerToken"))
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = jwtTokenProvider.resolveToken(request);
        memberService.logout(accessToken);

        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
