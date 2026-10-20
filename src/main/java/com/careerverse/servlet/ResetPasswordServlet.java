package com.careerverse.servlet;

import com.careerverse.config.DB;
import com.careerverse.dao.UserDAO;
import com.careerverse.service.EmailService;
import com.careerverse.service.EmailTemplates;
import com.careerverse.util.PasswordUtil;
import com.careerverse.util.RequestInfo;
import com.careerverse.util.TokenUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    /** The link in the email opens this URL; it simply shows the reset form. */
    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws IOException {
        String token = r.getParameter("token");
        s.sendRedirect("reset-password.jsp?token=" + URLEncoder.encode(token == null ? "" : token, StandardCharsets.UTF_8));
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws IOException {
        String token = r.getParameter("token");
        String password = r.getParameter("password");
        String confirm = r.getParameter("confirm");
        String back = "reset-password.jsp?token=" + URLEncoder.encode(token == null ? "" : token, StandardCharsets.UTF_8);

        if (password == null || password.length() < 8) {
            s.sendRedirect(back + "&error=Password+must+be+at+least+8+characters");
            return;
        }
        if (confirm != null && !confirm.equals(password)) {
            s.sendRedirect(back + "&error=Passwords+do+not+match");
            return;
        }

        try (Connection c = DB.getConnection();
             PreparedStatement q = c.prepareStatement(
                     "SELECT id, user_id FROM password_reset_tokens "
                   + "WHERE token_hash=? AND used=false AND expires_at>? ORDER BY id DESC LIMIT 1")) {
            q.setString(1, TokenUtil.sha256(token == null ? "" : token));
            q.setTimestamp(2, Timestamp.from(Instant.now()));
            try (ResultSet rs = q.executeQuery()) {
                if (!rs.next()) {
                    s.sendRedirect("reset-password.jsp?error=This+link+is+invalid+or+has+expired.+Please+request+a+new+one");
                    return;
                }
                int tokenId = rs.getInt(1);
                int userId = rs.getInt(2);
                UserDAO.changePassword(userId, PasswordUtil.hash(password));
                try (PreparedStatement u = c.prepareStatement(
                        "UPDATE password_reset_tokens SET used=true WHERE user_id=? AND used=false")) {
                    u.setInt(1, userId);
                    u.executeUpdate();
                }

                UserDAO.Result user = UserDAO.findById(userId);
                if (user != null) {
                    String[] mail = EmailTemplates.passwordChanged(user.name(), EmailTemplates.now(),
                            RequestInfo.ip(r), RequestInfo.appUrl(r));
                    EmailService.sendAsync(user.email(), user.name(), mail[0], mail[1], mail[2]);
                }
                s.sendRedirect("login.jsp?reset=1");
            }
        } catch (Exception e) {
            e.printStackTrace();
            s.sendRedirect(back + "&error=Could+not+reset+the+password.+Please+try+again");
        }
    }
}
