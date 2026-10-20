package com.careerverse.servlet;

import com.careerverse.service.AIService;
import com.careerverse.service.StudentContext;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * POST /app/api/ai   body: {"query": "...", "history": [{"role":"user|assistant","content":"..."}]}
 * Returns {"answer": "..."}.
 *
 * It sits under /app/* so AuthFilter makes sure only logged-in students can use it.
 */
@WebServlet("/app/api/ai")
public class AIQueryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_QUESTION = 2000;
    private static final int MAX_HISTORY = 8;

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Object uid = request.getSession().getAttribute("userId");
            if (uid == null) {
                send(response, 401, Map.of("error", "Please log in again."));
                return;
            }
            int userId = ((Number) uid).intValue();
            String name = (String) request.getSession().getAttribute("userName");

            JsonObject input = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String question = text(input, "query");
            if (question.isEmpty()) question = text(input, "message");
            if (question.isEmpty()) {
                send(response, 400, Map.of("error", "Please type a question."));
                return;
            }
            if (question.length() > MAX_QUESTION) question = question.substring(0, MAX_QUESTION);

            List<AIService.Message> history = new ArrayList<>();
            if (input.has("history") && input.get("history").isJsonArray()) {
                JsonArray arr = input.getAsJsonArray("history");
                int start = Math.max(0, arr.size() - MAX_HISTORY);
                for (int i = start; i < arr.size(); i++) {
                    JsonElement e = arr.get(i);
                    if (!e.isJsonObject()) continue;
                    String role = text(e.getAsJsonObject(), "role");
                    String content = text(e.getAsJsonObject(), "content");
                    if (content.isEmpty()) continue;
                    if (content.length() > MAX_QUESTION) content = content.substring(0, MAX_QUESTION);
                    history.add(new AIService.Message("assistant".equals(role) ? "assistant" : "user", content));
                }
            }

            String context = StudentContext.describe(userId, name);
            String answer = AIService.ask(context, history, question);
            if (answer.isBlank()) answer = "Sorry, I could not come up with an answer. Please try asking in a different way.";

            result.put("answer", answer);
            result.put("provider", AIService.provider());
            send(response, 200, result);

        } catch (Exception e) {
            System.err.println("CareerVerse AI error: " + e.getMessage());
            String provider = AIService.provider();
            String hint = switch (provider) {
                case "ollama" -> "The AI model is not reachable. On Render, set AI_PROVIDER=groq and GROQ_API_KEY.";
                case "groq" -> "The Groq AI service did not respond. Check GROQ_API_KEY on Render.";
                case "gemini" -> "The Gemini AI service did not respond. Check GEMINI_API_KEY on Render.";
                default -> "Check the AI_PROVIDER setting.";
            };
            send(response, 503, Map.of("error", "CareerVerse AI is unavailable right now. " + hint));
        }
    }

    private static String text(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return "";
        return o.get(key).getAsString().trim();
    }

    private void send(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(body));
    }
}
