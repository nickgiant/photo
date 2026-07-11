package com.photo.act.photo_act.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
public class EmailSendService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSendService.class);
    private final JavaMailSender emailSender;

    @Autowired
    public EmailSendService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public String sendSimpleMail(String strFrom, String to, String subject, String text) {


        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(strFrom);//("noreply@baeldung.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Mail sent Successfully");
            return to;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while sending mail." + e.getMessage());
            return null;
        }
    }

    public Exception sendSimpleMailOrException(String strFrom, String to, String subject, String text) {


        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(strFrom);//("noreply@baeldung.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Mail sent Successfully");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while sending mail." + e.getMessage());
            return e;
        }
    }

    public String sendMailWithAttachment(String strFrom, String to, String subject, String text, String pathToAttachment, String sentAsFilename) {
       // emailSender = new JavaMailSenderImpl();
        try {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(strFrom);//("noreply@baeldung.com");
            helper.setTo(to);
            // message.setTo(new String[] {"recipient1@example.com", "recipient2@example.com", "recipient3@example.com"});
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file
                    = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment(sentAsFilename, file);

            emailSender.send(message);
            logger.info("Mail sent Successfully to " + to);
            return to;
        } catch (MessagingException e) {
            logger.error("Error while sending mail." + e.getMessage());
            return null;
        }
    }


}


