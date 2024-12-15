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

    public void sendMail(String mail, MailStructure mailStructure) throws MessagingException {
//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
////        simpleMailMessage.setFrom(username);
//        simpleMailMessage.setTo(mail);
//        simpleMailMessage.setSubject(mailStructure.getSubject());
//        simpleMailMessage.setText(mailStructure.getMessage());

        MimeMessage simpleMailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        helper = new MimeMessageHelper(simpleMailMessage, false);
        helper.setTo("botannicolai22@gmail.com");
        helper.setSubject(mailStructure.getSubject());
        helper.setText(mailStructure.getMessage());

        mailSender.send(simpleMailMessage);

//        mailSender.send(simpleMailMessage);
    }
}
