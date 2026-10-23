package com.careerverse.servlet;

import com.careerverse.dao.ChatDAO;
import com.careerverse.service.AIService;
import com.careerverse.service.StudentContext;
import com.google.gson.Gson;
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
 * POST /app/api/ai
 *   {"query": "...", "conversationId": 5}         ask in an existing chat
 *   {"query": "..."}                              start a new chat (title made from the question)
 *   {"conversationId": 5, "regenerate": true}     answer the last question again
 * Returns {"answer", "conversationId", "title"}.
 *
 * Every question and answer is saved in chat_conversations / chat_messages,
 * and the last 8 messages are sent to the AI so it remembers the conversation.
 */
@WebServlet("/app/api/ai")
public class AIQueryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_QUESTION = 2000;
    private static final int MAX_HISTORY = 8;

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object uid = request.getSession().getAttribute("userId");
        if (uid == null) {
            send(response, 401, Map.of("error", "Please log in again."));
            return;
        }
        int userId = ((Number) uid).intValue();
        String name = (String) request.getSession().getAttribute("userName");

        Integer conversationId = null;
        String title = null;

        try {
            JsonObject input = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            boolean regenerate = input.has("regenerate") && input.get("regenerate").getAsBoolean();
            if (input.has("conversationId") && !input.get("conversationId").isJsonNull()) {
                conversationId = input.get("conversationId").getAsInt();
            }

            String question = text(input, "query");
            if (question.isEmpty()) question = text(input, "message");
            if (question.length() > MAX_QUESTION) question = question.substring(0, MAX_QUESTION);

            // Check the chat belongs to this student.
            if (conversationId != null) {
                ChatDAO.Conversation c = ChatDAO.get(userId, conversationId);
                if (c == null) {
                    send(response, 404, Map.of("error", "Chat not found."));
                    return;
                }
                title = c.title();
            }

            List<AIService.Message> history = new ArrayList<>();

            if (regenerate) {
                if (conversationId == null) {
                    send(response, 400, Map.of("error", "Nothing to regenerate."));
                    return;
                }
                question = ChatDAO.removeLastAnswer(userId, conversationId);
                if (question == null) {
                    send(response, 400, Map.of("error", "Nothing to regenerate."));
                    return;
                }
                // History = everything before that last question.
                List<ChatDAO.Message> recent = ChatDAO.recent(userId, conversationId, MAX_HISTORY + 1);
                for (int i = 0; i < recent.size() - 1; i++) history.add(toAi(recent.get(i)));
            } else {
                if (question.isEmpty()) {
                    send(response, 400, Map.of("error", "Please type a question."));
                    return;
                }
                if (conversationId == null) {
                    title = ChatDAO.titleFrom(question);
                    conversationId = ChatDAO.create(userId, title);
                } else {
                    for (ChatDAO.Message m : ChatDAO.recent(userId, conversationId, MAX_HISTORY)) history.add(toAi(m));
                }
                ChatDAO.addMessage(conversationId, "user", question);
            }

            String context = StudentContext.describe(userId, name);
            String answer = AIService.ask(context, history, question);
            if (answer.isBlank()) answer = "Sorry, I could not come up with an answer. Please try asking in a different way.";
            ChatDAO.addMessage(conversationId, "assistant", answer);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("answer", answer);
            result.put("conversationId", conversationId);
            result.put("title", title);
            result.put("provider", AIService.provider());
            send(response, 200, result);

        } catch (Exception e) {
            System.err.println("CareerVerse AI error: " + e.getMessage());
            String hint = e instanceof java.sql.SQLException
                    ? "The chat could not be saved (database error) – check the Render logs."
                    : switch (AIService.provider()) {
                case "ollama" -> "No cloud AI key is set. On Render add GROQ_API_KEY (free from console.groq.com).";
                case "groq" -> "The Groq AI service did not respond. Check GROQ_API_KEY on Render.";
                case "gemini" -> "The Gemini AI service did not respond. Check GEMINI_API_KEY on Render.";
                default -> "Check the AI_PROVIDER setting.";
                    };
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "CareerVerse AI is unavailable right now. " + hint);
            if (conversationId != null) error.put("conversationId", conversationId);
            if (title != null) error.put("title", title);
            send(response, 503, error);
        }
    }

    private static AIService.Message toAi(ChatDAO.Message m) {
        String content = m.content().length() > MAX_QUESTION ? m.content().substring(0, MAX_QUESTION) : m.content();
        return new AIService.Message("assistant".equals(m.role()) ? "assistant" : "user", content);
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
