package com.careerverse.servlet;

import com.careerverse.config.DB;
import com.careerverse.dao.ProfileDAO;
import com.careerverse.service.CareerEngine;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/app/api/recommendations")
public class RecommendationServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {

            Object sessionUser =
                    request.getSession().getAttribute("userId");

            if (sessionUser == null) {
                response.sendError(401, "Not logged in.");
                return;
            }

            int userId =
                    ((Number) sessionUser).intValue();

            var profile =
                    ProfileDAO.get(userId);

            Map<String, Integer> userSkills =
                    new HashMap<>();

            Object skillsObject =
                    profile.get("skills");

            if (skillsObject instanceof List<?>) {

                for (Object item :
                        (List<?>) skillsObject) {

                    if (!(item instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<?, ?> skill =
                            (Map<?, ?>) item;

                    Object name =
                            skill.get("name");

                    Object level =
                            skill.get("level");

                    if (name != null && level instanceof Number) {

                        userSkills.put(
                                name.toString()
                                    .toLowerCase()
                                    .trim(),
                                ((Number) level).intValue()
                        );
                    }
                }
            }

            Map<String, Integer> aptitude =
                    getLatestAssessment(userId);

            /*
             * If the student hasn't taken the aptitude test,
             * use neutral scores rather than returning nothing.
             */
            if (aptitude.isEmpty()) {

                aptitude = new LinkedHashMap<>();

                aptitude.put("logical", 50);
                aptitude.put("programming", 50);
                aptitude.put("communication", 50);
                aptitude.put("mathematics", 50);
                aptitude.put("creativity", 50);
                aptitude.put("analytical", 50);
            }

            List<Map<String, Object>> recommendations =
                    CareerEngine.recommend(
                            aptitude,
                            userSkills
                    );

            sendJson(
                    response,
                    recommendations
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            sendJson(
                    response,
                    List.of()
            );
        }
    }

    private Map<String, Integer> getLatestAssessment(
            int userId
    ) throws SQLException {

        Map<String, Integer> result =
                new LinkedHashMap<>();

        String sql =
                "SELECT logical_score, programming_score, " +
                "communication_score, mathematics_score, " +
                "creativity_score, analytical_score " +
                "FROM assessments " +
                "WHERE user_id=? " +
                "ORDER BY id DESC LIMIT 1";

        try (
            Connection c = DB.getConnection();
            PreparedStatement q =
                    c.prepareStatement(sql)
        ) {

            q.setInt(1, userId);

            try (ResultSet r = q.executeQuery()) {

                if (r.next()) {

                    result.put(
                            "logical",
                            r.getInt("logical_score")
                    );

                    result.put(
                            "programming",
                            r.getInt("programming_score")
                    );

                    result.put(
                            "communication",
                            r.getInt("communication_score")
                    );

                    result.put(
                            "mathematics",
                            r.getInt("mathematics_score")
                    );

                    result.put(
                            "creativity",
                            r.getInt("creativity_score")
                    );

                    result.put(
                            "analytical",
                            r.getInt("analytical_score")
                    );
                }
            }
        }

        return result;
    }

    private void sendJson(
            HttpServletResponse response,
            Object object
    ) throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().print(
                gson.toJson(object)
        );
    }
}