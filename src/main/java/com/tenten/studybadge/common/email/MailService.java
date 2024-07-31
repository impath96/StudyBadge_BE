package com.tenten.studybadge.common.email;

import com.tenten.studybadge.common.exception.member.SendMailException;
import com.tenten.studybadge.member.dto.MemberSignUpRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import static com.tenten.studybadge.common.constant.MailConstant.*;

@Service
@RequiredArgsConstructor
@EnableAsync
public class MailService {

    private final JavaMailSender javaMailSender;
    @Async
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

    @Async
    public void sendResetMail(String email, String authCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {

            String body = String.format(RESET_PASSWORD_BODY, authCode, email);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, UNICODE);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(RESET_PASSWORD_SUBJECT);
            mimeMessageHelper.setText(body, true);

        } catch (MessagingException e) {
            throw new SendMailException();
        }

        javaMailSender.send(mimeMessage);

    }

    @Async
    public void reSendMail(String email, String authCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {

            String body = String.format(SIGNUP_BODY, BASE_URL, email, authCode);
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, UNICODE);
            mimeMessageHelper.setTo(email);
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
