package com.tenten.studybadge.common.email;

import com.tenten.studybadge.common.exception.member.SendMailException;
import com.tenten.studybadge.member.dto.MemberSignUpRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(MemberSignUpRequest signUpRequest, String authCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            String email = signUpRequest.getEmail();
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(signUpRequest.getEmail());
            mimeMessageHelper.setSubject("Study Badge 가입 인증 메일입니다.");
            mimeMessageHelper.setText("<h3> 안녕하세요, Study Badge 가입을 환영합니다. 아래 링크를 클릭해, 인증을 완료해주세요.</h3>"
                        + "<div><a href='" + baseUrl + "/members/auth?email=" + email + "&code="
                        + authCode + "'> 인증하기 </a></div>", true);

        } catch (MessagingException e) {
            throw new SendMailException();
        }

        javaMailSender.send(mimeMessage);

    }

    public boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

}
