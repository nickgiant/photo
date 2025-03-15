package com.photo.act.photo_act.services;
// Importing required classes



public interface EmailService {

    // To send a simple email
    String sendSimpleMail(String to, String subject, String text);

    // To send an email with attachment
    String sendMailWithAttachment(String to, String subject, String template, String pathToAttachment,String sentAsFilename);


}
