package com.careerverse.service;
import com.careerverse.config.DB;
import jakarta.mail.*; import jakarta.mail.internet.*; import java.util.*;
public class EmailService {
  public static void send(String to,String subject,String body) throws MessagingException {
    Properties props=new Properties(); props.put("mail.smtp.auth","true"); props.put("mail.smtp.starttls.enable","true"); props.put("mail.smtp.host",DB.prop("mail.host")); props.put("mail.smtp.port",DB.prop("mail.port"));
    Session session=Session.getInstance(props,new Authenticator(){protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(DB.prop("mail.username"),DB.prop("mail.password"));}});
    Message msg=new MimeMessage(session); msg.setFrom(new InternetAddress(DB.prop("mail.username"))); msg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to)); msg.setSubject(subject); msg.setText(body); Transport.send(msg);
  }
}
