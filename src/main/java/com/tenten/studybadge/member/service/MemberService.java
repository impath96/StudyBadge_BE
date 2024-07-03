package com.tenten.studybadge.member.service;

import com.tenten.studybadge.common.email.MailService;
import com.tenten.studybadge.common.exception.member.*;
import com.tenten.studybadge.common.redis.RedisService;
import com.tenten.studybadge.member.dto.MemberSignUpRequest;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.type.member.MemberStatus;
import com.tenten.studybadge.type.member.Platform;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MailService mailComponent;
    private final RedisService redisService;
    private final MemberRepository memberRepository;

    public void signUp(MemberSignUpRequest signUpRequest, Platform platform) {

        if(!isValidEmail(signUpRequest.getEmail())) {
            throw new InvalidEmailException();
        }

        if(!signUpRequest.getPassword().equals(signUpRequest.getCheckPassword())) {
            throw new NotMatchPasswordException();
        }

        Member member = null;

        Optional<Member> byEmail = memberRepository.findByEmailAndPlatform(signUpRequest.getEmail(), platform);
        if (byEmail.isPresent()) {
            member = byEmail.get();

          if (!member.getStatus().equals(MemberStatus.WITHDRAWN)) {
              throw new DuplicateEmailException();
          }
        } else {

            member = new Member();

        }

        memberRepository.save(MemberSignUpRequest.toEntity(member, signUpRequest));

        String authCode = null;
        authCode = redisService.generateAuthCode();
        redisService.saveAuthCode(signUpRequest.getEmail(), authCode);

        mailComponent.sendMail(signUpRequest, authCode);
    }

    public void auth(String email, String code, Platform platform) {

        Member member = memberRepository.findByEmailAndPlatform(email, platform).orElseThrow(NotFoundMemberException::new);

        if (member.getIsAuth()) {
            throw new AlreadyAuthException();
        }

        if (!code.equals(redisService.getAuthCode(email))) {
            throw new InvalidAuthCodeException();
        }

        Member authMember = member.toBuilder()
                .isAuth(true)
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(authMember);

        redisService.deleteAuthCode(email);
    }


    private boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}


