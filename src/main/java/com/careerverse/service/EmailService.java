
package com.careerverse.service;

import com.careerverse.config.DB;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailService {

    private static final ExecutorService POOL =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "careerverse-mail");
                t.setDaemon(true);
                return t;
            });

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /*
     * Provider priority:
     * 1. Gmail API
     * 2. Brevo
     * 3. SMTP
     */

    public static String provider() {

        if (!DB.prop("gmail.client.id").isEmpty()
                && !DB.prop("gmail.client.secret").isEmpty()
                && !DB.prop("gmail.refresh.token").isEmpty()
                && !DB.prop("mail.username").isEmpty()) {
            return "gmail-api";
        }

        if (!DB.prop("brevo.api.key").isEmpty()) {
            return "brevo";
        }

        if (!DB.prop("mail.username").isEmpty()
                && !DB.prop("mail.password").isEmpty()) {
            return "smtp";
        }

        return "none";
    }

    public static void sendAsync(
            String to, String toName, String subject,
            String html, String text) {

        POOL.submit(() -> {
            try {
                send(to, toName, subject, html, text);
            } catch (Exception e) {
                System.err.println(
                        "CareerVerse email to " + to
                        + " failed (" + provider() + "): "
                        + e.getMessage());
            }
        });
    }

    public static void send(
            String to, String subject, String body)
            throws Exception {

        send(
                to,
                "",
                subject,
                EmailTemplates.wrap(
                        subject,
                        "<p>" + EmailTemplates.esc(body)
                        .replace("\n", "<br>") + "</p>"),
                body
        );
    }

    public static void send(
            String to, String toName, String subject,
            String html, String text) throws Exception {

        switch (provider()) {

            case "gmail-api":
                sendGmailApi(to, toName, subject, html, text);
                break;

            case "brevo":
                sendBrevo(to, toName, subject, html, text);
                break;

            case "smtp":
                sendSmtp(to, subject, html, text);
                break;

            default:
                System.out.println(
                        "CareerVerse email not sent - "
                        + "no email provider configured: "
                        + subject + " -> " + to);
        }
    }

    private static String fromEmail() {
        String from = DB.prop("mail.from");
        return from.isEmpty()
                ? DB.prop("mail.username")
                : from;
    }

    private static String fromName() {
        return DB.prop("mail.from.name", "CareerVerse AI");
    }

    /* =========================================
       GMAIL API
       ========================================= */

    private static String gmailAccessToken()
            throws IOException, InterruptedException {

        String form =
                "client_id=" + formEncode(DB.prop("gmail.client.id"))
                + "&client_secret=" + formEncode(
                        DB.prop("gmail.client.secret"))
                + "&refresh_token=" + formEncode(
                        DB.prop("gmail.refresh.token"))
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder(
                URI.create("https://oauth2.googleapis.com/token"))
                .timeout(Duration.ofSeconds(20))
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        form, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        JsonObject json =
                JsonParser.parseString(response.body())
                .getAsJsonObject();

        if (response.statusCode() < 200
                || response.statusCode() >= 300
                || !json.has("access_token")) {

            String detail = json.has("error_description")
                    ? json.get("error_description").getAsString()
                    : json.has("error")
                    ? json.get("error").getAsString()
                    : "Unknown OAuth error";

            throw new IOException(
                    "Gmail token refresh failed: " + detail);
        }

        return json.get("access_token").getAsString();
    }

    private static String formEncode(String value) {
        return java.net.URLEncoder.encode(
                value, StandardCharsets.UTF_8);
    }

    private static void sendGmailApi(
            String to, String toName, String subject,
            String html, String text) throws Exception {

        String senderEmail = DB.prop("mail.username");

        if (senderEmail.isEmpty()) {
            throw new IOException(
                    "MAIL_USERNAME is not configured.");
        }

        Properties properties = new Properties();
        Session session = Session.getInstance(properties);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(
                senderEmail, fromName(), "UTF-8"));

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to));

        message.setSubject(subject, "UTF-8");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(
                text == null ? "" : text, "UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(
                html == null ? "" : html,
                "text/html; charset=UTF-8");

        MimeMultipart alternative =
                new MimeMultipart("alternative");

        alternative.addBodyPart(textPart);
        alternative.addBodyPart(htmlPart);

        message.setContent(alternative);
        message.saveChanges();

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        message.writeTo(output);

        String raw = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(output.toByteArray());

        JsonObject payload = new JsonObject();
        payload.addProperty("raw", raw);

        HttpRequest request = HttpRequest.newBuilder(
                URI.create(
                    "https://gmail.googleapis.com/gmail/v1/"
                    + "users/me/messages/send"))
                .timeout(Duration.ofSeconds(25))
                .header(
                        "Authorization",
                        "Bearer " + gmailAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        payload.toString(),
                        StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Gmail API returned HTTP "
                    + response.statusCode()
                    + ": " + response.body());
        }
    }

    /* =========================================
       BREVO API - RETAINED AS FALLBACK
       ========================================= */

    private static void sendBrevo(
            String to, String toName, String subject,
            String html, String text)
            throws IOException, InterruptedException {

        if (fromEmail().isEmpty()) {
            throw new IOException(
                    "MAIL_FROM is not configured.");
        }

        JsonObject sender = new JsonObject();
        sender.addProperty("name", fromName());
        sender.addProperty("email", fromEmail());

        JsonObject recipient = new JsonObject();
        recipient.addProperty("email", to);

        if (toName != null && !toName.isBlank()) {
            recipient.addProperty("name", toName);
        }

        JsonArray toList = new JsonArray();
        toList.add(recipient);

        JsonObject body = new JsonObject();
        body.add("sender", sender);
        body.add("to", toList);
        body.addProperty("subject", subject);
        body.addProperty("htmlContent", html);
        body.addProperty("textContent", text);

        HttpRequest request = HttpRequest.newBuilder(
                URI.create(DB.prop(
                        "brevo.api.url",
                        "https://api.brevo.com/v3/smtp/email")))
                .timeout(Duration.ofSeconds(20))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", DB.prop("brevo.api.key"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        body.toString(),
                        StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Brevo returned HTTP "
                    + response.statusCode()
                    + ": " + response.body());
        }
    }

    /* =========================================
       SMTP - RETAINED AS FALLBACK
       ========================================= */

    private static void sendSmtp(
            String to, String subject,
            String html, String text) throws Exception {

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put(
                "mail.smtp.host",
                DB.prop("mail.host", "smtp.gmail.com"));
        properties.put(
                "mail.smtp.port",
                DB.prop("mail.port", "587"));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(
                properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                DB.prop("mail.username"),
                                DB.prop("mail.password"));
                    }
                });

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(
                fromEmail(), fromName(), "UTF-8"));

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to));

        message.setSubject(subject, "UTF-8");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(text, "UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(
                html, "text/html; charset=UTF-8");

        MimeMultipart alternative =
                new MimeMultipart("alternative");

        alternative.addBodyPart(textPart);
        alternative.addBodyPart(htmlPart);

        message.setContent(alternative);

        Transport.send(message);
    }
}
