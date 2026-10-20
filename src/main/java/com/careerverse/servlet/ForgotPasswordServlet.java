package com.careerverse.servlet;

import com.careerverse.config.DB;
import com.careerverse.dao.UserDAO;
import com.careerverse.service.EmailService;
import com.careerverse.service.EmailTemplates;
import com.careerverse.util.RequestInfo;
import com.careerverse.util.TokenUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Sends a password-reset link by email.
 * The page always says "sent" so nobody can find out which emails are registered.
 * Only the SHA-256 hash of the token is stored; the link works once, for 15 minutes.
 */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws IOException {
        String email = r.getParameter("email");
        try {
            UserDAO.Result user = UserDAO.find(email);
            if (user != null) {
                String token = TokenUtil.randomToken();
                try (Connection c = DB.getConnection();
                     PreparedStatement q = c.prepareStatement(
                             "INSERT INTO password_reset_tokens(user_id, token_hash, expires_at) VALUES(?, ?, ?)")) {
                    q.setInt(1, user.id());
                    q.setString(2, TokenUtil.sha256(token));
                    q.setTimestamp(3, Timestamp.from(Instant.now().plusSeconds(15 * 60)));
                    q.executeUpdate();
                }
                String link = RequestInfo.appUrl(r) + "/reset-password?token=" + token;
                String[] mail = EmailTemplates.resetLink(user.name(), link);
                EmailService.sendAsync(user.email(), user.name(), mail[0], mail[1], mail[2]);
            }
            s.sendRedirect("forgot-password.jsp?sent=1");
        } catch (Exception e) {
            e.printStackTrace();
            s.sendRedirect("forgot-password.jsp?error=Something+went+wrong.+Please+try+again");
        }
    }
}
