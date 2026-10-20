package com.careerverse.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The CareerVerse emails. Each method returns {subject, html, text}.
 * All values from users are HTML-escaped.
 */
public class EmailTemplates {

    private static final DateTimeFormatter WHEN =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a 'IST'", Locale.ENGLISH);

    public static String now() {
        return ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(WHEN);
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String button(String url, String label) {
        return "<p style=\"margin:28px 0\"><a href=\"" + esc(url) + "\" style=\"background:#5b5ce2;color:#ffffff;"
                + "text-decoration:none;padding:12px 22px;border-radius:10px;font-weight:600;display:inline-block\">"
                + esc(label) + "</a></p>";
    }

    private static String details(String... rows) {
        StringBuilder sb = new StringBuilder("<table style=\"border-collapse:collapse;margin:16px 0;font-size:14px\">");
        for (int i = 0; i + 1 < rows.length; i += 2) {
            sb.append("<tr><td style=\"padding:6px 16px 6px 0;color:#7b8598\">").append(esc(rows[i]))
              .append("</td><td style=\"padding:6px 0;color:#11152b;font-weight:600\">").append(esc(rows[i + 1]))
              .append("</td></tr>");
        }
        return sb.append("</table>").toString();
    }

    /** Common layout for every email. */
    public static String wrap(String heading, String bodyHtml) {
        return "<!doctype html><html><body style=\"margin:0;background:#f4f5fb;font-family:Segoe UI,Arial,sans-serif;color:#11152b\">"
             + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f5fb;padding:24px 12px\"><tr><td align=\"center\">"
             + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:560px;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e7e9f0\">"
             + "<tr><td style=\"background:#11152b;padding:20px 28px;color:#ffffff;font-size:20px;font-weight:700\">"
             + "&#10022; Career<span style=\"color:#8b8cf7\">Verse</span> AI</td></tr>"
             + "<tr><td style=\"padding:28px\">"
             + "<h2 style=\"margin:0 0 12px;font-size:20px\">" + esc(heading) + "</h2>"
             + "<div style=\"font-size:15px;line-height:1.6\">" + bodyHtml + "</div>"
             + "</td></tr>"
             + "<tr><td style=\"padding:16px 28px;background:#f7f8fd;color:#7b8598;font-size:12px\">"
             + "You received this email because you have a CareerVerse AI account. "
             + "CareerVerse will never ask for your password by email.</td></tr>"
             + "</table></td></tr></table></body></html>";
    }

    public static String[] welcome(String name, String appUrl) {
        String subject = "Welcome to CareerVerse AI, " + name + "!";
        String html = wrap("Welcome aboard, " + name + " 🎉",
                "<p>Your CareerVerse AI account has been created successfully.</p>"
              + "<p>Here is how to get the most out of it:</p>"
              + "<ol><li>Complete your <b>Student Profile</b> – branch, year, skills and interests.</li>"
              + "<li>Take the <b>Aptitude Test</b> to get career matches for your branch.</li>"
              + "<li>Open your <b>Learning Roadmap</b> – a semester-by-semester plan until final year.</li>"
              + "<li>Link your LeetCode username and keep your <b>practice streak</b> going.</li></ol>"
              + (appUrl.isEmpty() ? "" : button(appUrl + "/login.jsp", "Open CareerVerse")));
        String text = "Hi " + name + ",\n\nYour CareerVerse AI account has been created successfully.\n"
                + "Complete your profile, take the aptitude test and open your learning roadmap.\n"
                + (appUrl.isEmpty() ? "" : "\nLog in: " + appUrl + "/login.jsp\n")
                + "\nRegards,\nCareerVerse AI";
        return new String[]{subject, html, text};
    }

    public static String[] loginAlert(String name, String when, String device, String ip, String appUrl) {
        String subject = "New login to your CareerVerse AI account";
        String html = wrap("Hi " + name + ", you just logged in",
                "<p>We noticed a new login to your CareerVerse AI account.</p>"
              + details("When", when, "Device", device, "IP address", ip)
              + "<p>If this was you, you can ignore this email.</p>"
              + "<p><b>If this wasn't you</b>, reset your password right away.</p>"
              + (appUrl.isEmpty() ? "" : button(appUrl + "/forgot-password.jsp", "Reset my password")));
        String text = "Hi " + name + ",\n\nNew login to your CareerVerse AI account.\n"
                + "When: " + when + "\nDevice: " + device + "\nIP address: " + ip + "\n\n"
                + "If this wasn't you, reset your password"
                + (appUrl.isEmpty() ? "." : ": " + appUrl + "/forgot-password.jsp") + "\n\nCareerVerse AI";
        return new String[]{subject, html, text};
    }

    public static String[] resetLink(String name, String link) {
        String subject = "Reset your CareerVerse AI password";
        String html = wrap("Reset your password",
                "<p>Hi " + esc(name) + ", we received a request to reset your CareerVerse AI password.</p>"
              + button(link, "Choose a new password")
              + "<p style=\"font-size:13px;color:#7b8598\">This link works for <b>15 minutes</b> and can be used only once. "
              + "If the button doesn't work, copy this link into your browser:<br>"
              + "<span style=\"word-break:break-all\">" + esc(link) + "</span></p>"
              + "<p>If you didn't ask for this, ignore this email – your password stays the same.</p>");
        String text = "Hi " + name + ",\n\nReset your CareerVerse AI password using this link (valid for 15 minutes):\n"
                + link + "\n\nIf you didn't ask for this, ignore this email.\n\nCareerVerse AI";
        return new String[]{subject, html, text};
    }

    public static String[] passwordChanged(String name, String when, String ip, String appUrl) {
        String subject = "Your CareerVerse AI password was changed";
        String html = wrap("Password changed",
                "<p>Hi " + esc(name) + ", the password for your CareerVerse AI account was just changed.</p>"
              + details("When", when, "IP address", ip)
              + "<p>If you did this, no action is needed.</p>"
              + "<p><b>If you didn't</b>, reset your password immediately.</p>"
              + (appUrl.isEmpty() ? "" : button(appUrl + "/forgot-password.jsp", "Secure my account")));
        String text = "Hi " + name + ",\n\nYour CareerVerse AI password was changed on " + when + " (IP " + ip + ").\n"
                + "If this wasn't you, reset your password immediately.\n\nCareerVerse AI";
        return new String[]{subject, html, text};
    }
}
