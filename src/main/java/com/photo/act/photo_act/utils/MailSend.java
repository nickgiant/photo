package com.photo.act.photo_act.utils;

import com.photo.act.photo_act.db.RecordService;
import com.photo.act.photo_act.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
    public class MailSend implements EmailService {

    @Autowired
    private JavaMailSender emailSender;


    @Override
    public String sendSimpleMail(String to, String subject, String text) {


        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ngiant@gmail.com");//("noreply@baeldung.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            return "Mail sent Successfully";
        }
        catch (Exception e) {

            return "Error while sending mail.";
        }
    }


    @Override
    public String sendMailWithAttachment(String to, String subject, String text, String pathToAttachment, String sentAsFilename) {

            try{
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("ngiant@gmail.com");//("noreply@baeldung.com");
            helper.setTo(to);
            // message.setTo(new String[] {"recipient1@example.com", "recipient2@example.com", "recipient3@example.com"});
            helper.setSubject(subject);
            helper.setText(text);

            FileSystemResource file
                    = new FileSystemResource(new File(pathToAttachment));
            helper.addAttachment(sentAsFilename, file);

            emailSender.send(message);
            return "Mail sent Successfully to "+to+".";
            }
            catch (MessagingException e) {
//                e.printStackTrace();

                return "Error while sending mail!!! "+e.getMessage();
            }

        }

    private void logErrorInDb(Exception e, String function, String info, int userId, String strUsername) {

//        Notification.show(" logErrorInDb  .  " + function + "  .  " + info);
//        recordService.logErrorInDb(e,"",function,userId,strUsername,"","",info);
    }


}


