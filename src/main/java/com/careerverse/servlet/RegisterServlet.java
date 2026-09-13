package com.careerverse.servlet;

import com.careerverse.dao.UserDAO;
import com.careerverse.service.EmailService;
import com.careerverse.util.PasswordUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String pw = req.getParameter("password");

        try {

            if (UserDAO.find(email) != null) {
                res.sendRedirect("register.jsp?error=Email+already+registered");
                return;
            }

            int id = UserDAO.create(
                    name,
                    email,
                    PasswordUtil.hash(pw)
            );

            try {

                EmailService.send(
                        email,
                        "Welcome to CareerVerse AI",
                        "Hi " + name + ",\n\n"
                                + "Your CareerVerse AI account has been registered successfully.\n\n"
                                + "You can now complete your profile, assessment and career roadmap.\n\n"
                                + "Regards,\n"
                                + "CareerVerse AI"
                );

            } catch (Exception emailError) {

                emailError.printStackTrace();

            }

            req.getSession().setAttribute("userId", id);
            req.getSession().setAttribute("userName", name);

            res.sendRedirect("app/dashboard.jsp");

        } catch (Exception e) {

            e.printStackTrace();

            res.sendRedirect(
                    "register.jsp?error=Registration+failed"
            );
        }
    }
}