package com.bobr.exam.service;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Value("${mail.username:}")
    private String username;

    @Value("${mail.password:}")
    private String password;

    @Value("${mail.host:smtp.gmail.com}")
    private String host;

    @Value("${mail.port:587}")
    private String port;

    public boolean configured() {
        return username != null && !username.isEmpty()
                && password != null && !password.isEmpty();
    }

    public void sendCode(String to, String code) throws Exception {
        Properties p = new Properties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", port);
        p.put("mail.smtp.ssl.trust", host);

        Session session = Session.getInstance(p, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });

        MimeMessage m = new MimeMessage(session);
        m.setFrom(new InternetAddress(username));
        m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        m.setSubject("BobrExam: код подтверждения", "UTF-8");
        m.setText("Ваш код: " + code + "\nКод действует 10 минут.", "UTF-8");
        Transport.send(m);
    }
}
