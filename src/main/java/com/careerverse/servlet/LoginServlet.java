package com.careerverse.servlet;

import com.careerverse.dao.UserDAO;
import com.careerverse.util.PasswordUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            res.sendRedirect("login.jsp?error=Please+enter+your+email+and+password");
            return;
        }

        try {
            UserDAO.Result user = UserDAO.find(email.trim());

            if (user == null || !PasswordUtil.matches(password, user.hash())) {
                res.sendRedirect("login.jsp?error=Invalid+email+or+password");
                return;
            }

            // Start a fresh session after login (protects against session fixation).
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.id());
            session.setAttribute("userName", user.name());

            res.sendRedirect("app/dashboard.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("login.jsp?error=Login+failed.+Please+try+again");
        }
    }
}