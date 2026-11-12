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
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", port);
        p.put("mail.smtp.ssl.trust", host);
        if ("465".equals(port)) {
            p.put("mail.smtp.ssl.enable", "true");
        } else {
            p.put("mail.smtp.starttls.enable", "true");
        }
        p.put("mail.smtp.connectiontimeout", "15000");
        p.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(p, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });

        String html = "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;"
                + "background:#FFF8E1;border:2px solid #8D6E63;border-radius:16px;padding:24px'>"
                + "<h2 style='color:#5D4037;margin:0 0 8px'>БобрЭкзамен</h2>"
                + "<p style='color:#4E342E'>Привет! Ты запросил код подтверждения.</p>"
                + "<div style='font-size:32px;font-weight:bold;letter-spacing:8px;color:#5D4037;"
                + "background:#FFFFFF;border-radius:12px;padding:16px;text-align:center'>"
                + code + "</div>"
                + "<p style='color:#6D4C41;font-size:13px;margin-top:16px'>"
                + "Код действует 10 минут. Если ты не запрашивал код — просто проигнорируй это письмо.</p>"
                + "<p style='color:#8D6E63;font-size:12px'>С заботой, твой Бобр-профессор</p>"
                + "</div>";

        MimeMessage m = new MimeMessage(session);
        m.setFrom(new InternetAddress(username, "БобрЭкзамен"));
        m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        m.setSubject("БобрЭкзамен: твой код " + code, "UTF-8");
        m.setContent(html, "text/html; charset=UTF-8");
        Transport.send(m);
    }
}
