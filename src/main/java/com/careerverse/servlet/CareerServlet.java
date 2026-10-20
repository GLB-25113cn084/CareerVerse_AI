package com.careerverse.servlet;

import com.careerverse.dao.ProfileDAO;
import com.careerverse.model.CareerData;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

/**
 * GET /app/api/careers        -> careers that suit the branch saved in the student's profile
 * GET /app/api/careers?all=1  -> every career
 */
@WebServlet("/app/api/careers")
public class CareerServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String branch = "";
        Object uid = request.getSession().getAttribute("userId");
        if (uid != null && !"1".equals(request.getParameter("all"))) {
            try {
                branch = String.valueOf(ProfileDAO.get(((Number) uid).intValue()).getOrDefault("branch", ""));
            } catch (Exception e) {
                branch = "";
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (CareerData.Career c : CareerData.forBranch(branch)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.id());
            m.put("name", c.name());
            m.put("icon", c.icon());
            m.put("description", c.description());
            m.put("core", CareerData.isCore(c, branch));
            m.put("languages", c.languages());
            m.put("skills", c.skills());
            m.put("projects", c.projects());
            m.put("certifications", c.certifications());
            m.put("roles", c.roles());
            out.add(m);
        }
        // Core careers of the branch first, then the rest.
        out.sort((a, b) -> Boolean.compare((Boolean) b.get("core"), (Boolean) a.get("core")));

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(gson.toJson(out));
    }
}
