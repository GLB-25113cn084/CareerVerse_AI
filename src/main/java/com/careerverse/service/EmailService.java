package com.careerverse.service;

import com.careerverse.config.DB;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends CareerVerse emails (welcome, login alert, password reset, password changed).
 *
 * Two ways of sending are supported:
 *  1. Brevo HTTP API  - set BREVO_API_KEY and MAIL_FROM. Works on Render's FREE plan,
 *                       because it uses normal HTTPS (port 443).
 *  2. SMTP (e.g. Gmail) - set MAIL_USERNAME and MAIL_PASSWORD. Works on a laptop and on
 *                       paid Render plans. Render's free plan blocks SMTP ports 25/465/587.
 *
 * Emails are sent in the background so login and register stay fast.
 */
public class EmailService {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "careerverse-mail");
        t.setDaemon(true);
        return t;
    });

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Which provider will be used: brevo, smtp or none. */
    public static String provider() {
        if (!DB.prop("brevo.api.key").isEmpty()) return "brevo";
        if (!DB.prop("mail.username").isEmpty() && !DB.prop("mail.password").isEmpty()) return "smtp";
        return "none";
    }

    /** Sends in the background. Failures are logged and never break the page. */
    public static void sendAsync(String to, String toName, String subject, String html, String text) {
        POOL.submit(() -> {
            try {
                send(to, toName, subject, html, text);
            } catch (Exception e) {
                System.err.println("CareerVerse email to " + to + " failed (" + provider() + "): " + e.getMessage());
            }
        });
    }

    /** Old plain-text method, kept so older code still compiles. */
    public static void send(String to, String subject, String body) throws Exception {
        send(to, "", subject, EmailTemplates.wrap(subject, "<p>" + EmailTemplates.esc(body).replace("\n", "<br>") + "</p>"), body);
    }

    public static void send(String to, String toName, String subject, String html, String text) throws Exception {
        switch (provider()) {
            case "brevo" -> sendBrevo(to, toName, subject, html, text);
            case "smtp" -> sendSmtp(to, subject, html, text);
            default -> System.out.println("CareerVerse email (not sent - no email provider configured): "
                    + subject + " -> " + to);
        }
    }

    private static String fromEmail() {
        String from = DB.prop("mail.from");
        return from.isEmpty() ? DB.prop("mail.username") : from;
    }

    private static String fromName() {
        return DB.prop("mail.from.name", "CareerVerse AI");
    }

    /* ---------- Brevo (HTTPS API) ---------- */

    private static void sendBrevo(String to, String toName, String subject, String html, String text)
            throws IOException, InterruptedException {
        if (fromEmail().isEmpty()) throw new IOException("MAIL_FROM is not set (use the sender email verified in Brevo).");
        JsonObject sender = new JsonObject();
        sender.addProperty("name", fromName());
        sender.addProperty("email", fromEmail());

        JsonObject recipient = new JsonObject();
        recipient.addProperty("email", to);
        if (toName != null && !toName.isBlank()) recipient.addProperty("name", toName);
        JsonArray toList = new JsonArray();
        toList.add(recipient);

        JsonObject body = new JsonObject();
        body.add("sender", sender);
        body.add("to", toList);
        body.addProperty("subject", subject);
        body.addProperty("htmlContent", html);
        body.addProperty("textContent", text);

        HttpRequest req = HttpRequest.newBuilder(URI.create(DB.prop("brevo.api.url", "https://api.brevo.com/v3/smtp/email")))
                .timeout(Duration.ofSeconds(20))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", DB.prop("brevo.api.key"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IOException("Brevo returned HTTP " + res.statusCode() + ": " + res.body());
        }
    }

    /* ---------- SMTP (Gmail etc.) ---------- */

    private static void sendSmtp(String to, String subject, String html, String text) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", DB.prop("mail.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", DB.prop("mail.port", "587"));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(DB.prop("mail.username"), DB.prop("mail.password"));
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail(), fromName(), "UTF-8"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject, "UTF-8");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(text, "UTF-8");
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");
        MimeMultipart alternative = new MimeMultipart("alternative");
        alternative.addBodyPart(textPart);
        alternative.addBodyPart(htmlPart);
        msg.setContent(alternative);

        Transport.send(msg);
    }
}
