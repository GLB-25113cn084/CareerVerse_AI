//package com.careerverse.servlet;
//import com.careerverse.dao.UserDAO; import com.careerverse.util.PasswordUtil; import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*; import java.io.*;
//@WebServlet("/login") public class LoginServlet extends HttpServlet{protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{try{UserDAO.Result u=UserDAO.find(req.getParameter("email"));if(u==null||!PasswordUtil.matches(req.getParameter("password"),u.hash())){res.sendRedirect("login.jsp?error=Invalid+email+or+password");return;}req.getSession().setAttribute("userId",u.id());req.getSession().setAttribute("userName",u.name());res.sendRedirect("app/dashboard.jsp");}catch(Exception e){res.sendRedirect("login.jsp?error=Login+failed");}}}
package com.careerverse.servlet;

import com.careerverse.dao.UserDAO;
import com.careerverse.util.PasswordUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res) throws IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        System.out.println("===== LOGIN DEBUG =====");
        System.out.println("Email received: " + email);
        System.out.println("Password received: " +
                (password == null ? "NULL" : "YES"));
        
        try {

            UserDAO.Result user = UserDAO.find(email);

            if (user == null) {
                System.out.println("LOGIN DEBUG: User NOT FOUND");
                res.sendRedirect(
                        "login.jsp?error=Invalid+email+or+password"
                );
                return;
            }

            System.out.println(
                    "LOGIN DEBUG: User found, ID = " + user.id()
            );

            boolean passwordMatches =
                    PasswordUtil.matches(password, user.hash());

            System.out.println(
                    "LOGIN DEBUG: Password matches = "
                            + passwordMatches
            );

            if (!passwordMatches) {
                res.sendRedirect(
                        "login.jsp?error=Invalid+email+or+password"
                );
                return;
            }

            req.getSession().setAttribute("userId", user.id());
            req.getSession().setAttribute("userName", user.name());

            System.out.println("LOGIN DEBUG: LOGIN SUCCESS");

            res.sendRedirect("app/dashboard.jsp");

        } catch (Exception e) {

            System.out.println("===== LOGIN ERROR =====");
            e.printStackTrace();

            res.sendRedirect(
                    "login.jsp?error=Login+failed"
            );
        }
    }
}