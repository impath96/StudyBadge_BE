package com.tenten.studybadge.member.service;

import com.tenten.studybadge.common.component.MailComponent;
import com.tenten.studybadge.common.exception.member.*;
import com.tenten.studybadge.member.domain.dto.MemberSignUpRequest;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.type.member.MemberStatus;
import com.tenten.studybadge.type.member.Platform;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MailComponent mailComponent;
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
        }

        if (member == null) {

            member = new Member();
        }

        member = memberRepository.save(MemberSignUpRequest.toEntity(member, signUpRequest));

        sendMail(signUpRequest, member);
    }

    public void auth(String email, String code, Platform platform) {

        Member member = memberRepository.findByEmailAndPlatform(email, platform).orElseThrow(NotFoundMemberException::new);

        if (member.getAuth()) {
            throw new AlreadyAuthException();
        }

        if (!code.equals(member.getAuthCode())) {
            throw new InvalidAuthCodeException();
        }

        Member authMember = member.toBuilder()
                .auth(true)
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(authMember);
    }


    private boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    private void sendMail(MemberSignUpRequest signUpRequest, Member member) {

        String email = signUpRequest.getEmail();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String title = "Study Badge 회원가입 인증 메일입니다.";
        String message = "<h3> 안녕하세요, Study Badge 가입을 환영합니다. 아래 링크를 클릭해, 인증을 완료해주세요.</h3>"
                        + "<div><a href='" + baseUrl + "/members/auth?email=" + email + "&code="
                        + member.getAuthCode() + "'> 인증 링크 </a></div>";

        mailComponent.sendMail(email, title, message);
    }


}
