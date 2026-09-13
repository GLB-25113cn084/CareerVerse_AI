package com.careerverse.servlet;

import com.careerverse.dao.ProfileDAO;
import com.careerverse.model.CareerData;
import com.careerverse.service.CareerEngine;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.util.*;

@WebServlet("/app/api/career")
public class CareerDetailServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {

            String careerId =
                    request.getParameter("id");

            if (careerId == null ||
                careerId.isBlank()) {

                response.sendError(
                        400,
                        "Career ID is required."
                );

                return;
            }

            CareerData.Career career =
                    CareerData.get(careerId);

            if (career == null) {

                response.sendError(
                        404,
                        "Career not found."
                );

                return;
            }

            Object sessionUser =
                    request.getSession()
                           .getAttribute("userId");

            if (sessionUser == null) {

                response.sendError(401);
                return;
            }

            int userId =
                    ((Number) sessionUser).intValue();

            var profile =
                    ProfileDAO.get(userId);

            Map<String, Integer> skills =
                    new HashMap<>();

            Object skillObject =
                    profile.get("skills");

            if (skillObject instanceof List<?>) {

                for (Object item :
                        (List<?>) skillObject) {

                    if (!(item instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<?, ?> skill =
                            (Map<?, ?>) item;

                    Object name =
                            skill.get("name");

                    Object level =
                            skill.get("level");

                    if (name != null &&
                        level instanceof Number) {

                        skills.put(
                                name.toString()
                                    .toLowerCase()
                                    .trim(),
                                ((Number) level).intValue()
                        );
                    }
                }
            }

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("career", career);

            result.put(
                    "skillGap",
                    CareerEngine.gap(
                            career,
                            skills
                    )
            );

            result.put(
                    "roadmap",
                    CareerEngine.roadmap(career)
            );

            result.put(
                    "simulator",
                    career.stages()
            );

            sendJson(response, result);

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            sendJson(
                    response,
                    Map.of(
                            "error",
                            "Could not load career information."
                    )
            );
        }
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