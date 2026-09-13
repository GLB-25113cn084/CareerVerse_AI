package com.careerverse.filter;
import jakarta.servlet.*; import jakarta.servlet.annotation.WebFilter; import jakarta.servlet.http.*; import java.io.IOException;
@WebFilter("/app/*") public class AuthFilter implements Filter { public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{HttpServletRequest r=(HttpServletRequest)req;if(r.getSession().getAttribute("userId")==null){((HttpServletResponse)res).sendRedirect(r.getContextPath()+"/login.jsp");return;}chain.doFilter(req,res);}}
