package com.tenten.studybadge.common.component;

import com.tenten.studybadge.common.exception.member.SendMailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailComponent {

    private final JavaMailSender javaMailSender;

    public void sendMail(String email, String title, String text) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setText(text, true);

        } catch (MessagingException e) {
            throw new SendMailException();
        }

        javaMailSender.send(mimeMessage);

    }

}
