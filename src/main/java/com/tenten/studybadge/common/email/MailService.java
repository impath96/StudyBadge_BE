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

import static com.tenten.studybadge.common.constant.MailConstant.*;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(MemberSignUpRequest signUpRequest, String authCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            String email = signUpRequest.getEmail();
            String body = String.format(SIGNUP_BODY, BASE_URL, email, authCode);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, UNICODE);
            mimeMessageHelper.setTo(signUpRequest.getEmail());
            mimeMessageHelper.setSubject(SIGNUP_SUBJECT);
            mimeMessageHelper.setText(body, true);

        } catch (MessagingException e) {
            throw new SendMailException();
        }

        javaMailSender.send(mimeMessage);

    }

    public boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

}
