package com.careerverse.servlet;

import com.careerverse.dao.UserDAO;
import com.careerverse.service.EmailService;
import com.careerverse.service.EmailTemplates;
import com.careerverse.util.PasswordUtil;
import com.careerverse.util.RequestInfo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String name = req.getParameter("name") == null ? "" : req.getParameter("name").trim();
        String email = req.getParameter("email") == null ? "" : req.getParameter("email").trim().toLowerCase();
        String pw = req.getParameter("password");

        if (name.isEmpty() || email.isEmpty() || pw == null || pw.length() < 8) {
            res.sendRedirect("register.jsp?error=Please+fill+in+all+fields+(password+at+least+8+characters)");
            return;
        }

        try {
            if (UserDAO.find(email) != null) {
                res.sendRedirect("register.jsp?error=Email+already+registered");
                return;
            }

            int id = UserDAO.create(name, email, PasswordUtil.hash(pw));

            String[] mail = EmailTemplates.welcome(name, RequestInfo.appUrl(req));
            EmailService.sendAsync(email, name, mail[0], mail[1], mail[2]);

            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", id);
            session.setAttribute("userName", name);

            res.sendRedirect("app/dashboard.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("register.jsp?error=Registration+failed.+Please+try+again");
        }
    }
}
