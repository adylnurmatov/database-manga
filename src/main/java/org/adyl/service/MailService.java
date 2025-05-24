package org.adyl.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.adyl.model.MailStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String username;

    public void sendResetLink(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(username);
            helper.setSubject("Password Reset Request");
            String htmlContent = "<p>To reset your password, click the link below:</p>" +
                    "<p><a href=\"" + resetLink + "\">Reset Password</a></p>" +
                    "<p>If you didn't request this, you can ignore this email.</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset email", e);
        }
    }

    public void sendMail(String mail, MailStructure mailStructure) throws MessagingException {

        MimeMessage simpleMailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        helper = new MimeMessageHelper(simpleMailMessage, false);
        helper.setTo("botannicolai22@gmail.com");
        helper.setSubject(mailStructure.getSubject());
        helper.setText(mailStructure.getMessage());

        mailSender.send(simpleMailMessage);

    }
}
