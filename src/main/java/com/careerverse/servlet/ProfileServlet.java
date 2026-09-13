package com.careerverse.servlet;

import com.careerverse.dao.ProfileDAO;
import com.google.gson.*;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.util.*;

@WebServlet("/app/api/profile")
public class ProfileServlet extends HttpServlet {

    private final Gson gson = new Gson();

    private int getUserId(HttpServletRequest request) {

        Object value =
                request.getSession().getAttribute("userId");

        if (value == null) {
            throw new IllegalStateException("User is not logged in.");
        }

        return ((Number) value).intValue();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {

            int userId = getUserId(request);

            sendJson(
                    response,
                    ProfileDAO.get(userId)
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load profile."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {

            int userId = getUserId(request);

            JsonObject input =
                    JsonParser.parseReader(
                            request.getReader()
                    ).getAsJsonObject();

            String branch =
                    getString(input, "branch");

            int currentYear =
                    input.get("currentYear").getAsInt();

            String interests =
                    getString(input, "interests");

            if (currentYear < 1 || currentYear > 4) {
                throw new IllegalArgumentException(
                        "Invalid current year."
                );
            }

            /*
             * Only completed academic years are accepted.
             *
             * 1st year -> 0 CGPAs
             * 2nd year -> 1 CGPA
             * 3rd year -> 2 CGPAs
             * 4th year -> 3 CGPAs
             */
            int maxCompletedYear =
                    Math.max(0, currentYear - 1);

            List<Map<String, Object>> cgpa =
                    new ArrayList<>();

            JsonArray cgpaArray =
                    input.has("cgpa") &&
                    input.get("cgpa").isJsonArray()
                    ? input.getAsJsonArray("cgpa")
                    : new JsonArray();

            for (JsonElement element : cgpaArray) {

                JsonObject x =
                        element.getAsJsonObject();

                int year =
                        x.get("year").getAsInt();

                double value =
                        x.get("cgpa").getAsDouble();

                if (year < 1 ||
                    year > maxCompletedYear) {
                    continue;
                }

                if (value < 0 || value > 10) {
                    continue;
                }

                Map<String, Object> row =
                        new LinkedHashMap<>();

                row.put("year", year);
                row.put("cgpa", value);

                cgpa.add(row);
            }

            List<Map<String, Object>> skills =
                    new ArrayList<>();

            JsonArray skillsArray =
                    input.has("skills") &&
                    input.get("skills").isJsonArray()
                    ? input.getAsJsonArray("skills")
                    : new JsonArray();

            for (JsonElement element : skillsArray) {

                JsonObject x =
                        element.getAsJsonObject();

                if (!x.has("name")) {
                    continue;
                }

                String name =
                        x.get("name")
                         .getAsString()
                         .trim();

                if (name.isEmpty()) {
                    continue;
                }

                int level =
                        x.has("level")
                        ? x.get("level").getAsInt()
                        : 3;

                level =
                        Math.max(1, Math.min(5, level));

                Map<String, Object> row =
                        new LinkedHashMap<>();

                row.put("name", name);
                row.put("level", level);

                skills.add(row);
            }

            ProfileDAO.save(
                    userId,
                    branch,
                    currentYear,
                    interests,
                    cgpa,
                    skills
            );

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put("ok", true);
            result.put("message", "Profile saved successfully.");
            result.put(
                    "profile",
                    ProfileDAO.get(userId)
            );

            sendJson(response, result);

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            sendJson(
                    response,
                    Map.of(
                            "ok", false,
                            "error",
                            e.getMessage() == null
                            ? "Could not save profile."
                            : e.getMessage()
                    )
            );
        }
    }

    private String getString(
            JsonObject object,
            String key
    ) {

        if (!object.has(key) ||
            object.get(key).isJsonNull()) {

            return "";
        }

        return object.get(key).getAsString().trim();
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