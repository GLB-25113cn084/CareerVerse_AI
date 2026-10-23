package com.careerverse.servlet;

import com.careerverse.dao.ChatDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Saved chats.
 *
 * GET  /app/api/chats            list of the student's chats
 * GET  /app/api/chats?id=5       one chat with all its messages
 * POST /app/api/chats            {"action":"rename","id":5,"title":"..."}
 *                                {"action":"pin","id":5,"pinned":true}
 *                                {"action":"delete","id":5}
 *                                {"action":"deleteAll"}
 */
@WebServlet("/app/api/chats")
public class ChatServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = userId(request);
        if (userId == null) {
            send(response, 401, Map.of("error", "Please log in again."));
            return;
        }
        try {
            String id = request.getParameter("id");
            if (id == null || id.isBlank()) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (ChatDAO.Conversation c : ChatDAO.list(userId)) list.add(toJson(c));
                send(response, 200, list);
                return;
            }
            ChatDAO.Conversation c = ChatDAO.get(userId, Integer.parseInt(id));
            if (c == null) {
                send(response, 404, Map.of("error", "Chat not found."));
                return;
            }
            List<Map<String, Object>> messages = new ArrayList<>();
            for (ChatDAO.Message m : ChatDAO.messages(userId, c.id())) {
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("id", m.id());
                mm.put("role", m.role());
                mm.put("content", m.content());
                mm.put("createdAt", iso(m.createdAt()));
                messages.add(mm);
            }
            Map<String, Object> out = toJson(c);
            out.put("messages", messages);
            send(response, 200, out);
        } catch (NumberFormatException e) {
            send(response, 400, Map.of("error", "Invalid chat id."));
        } catch (Exception e) {
            e.printStackTrace();
            send(response, 500, Map.of("error", "Could not load chats."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = userId(request);
        if (userId == null) {
            send(response, 401, Map.of("error", "Please log in again."));
            return;
        }
        try {
            JsonObject in = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String action = in.has("action") ? in.get("action").getAsString() : "";
            int id = in.has("id") ? in.get("id").getAsInt() : 0;
            if (!Set.of("rename", "pin", "delete", "deleteAll").contains(action)) {
                send(response, 400, Map.of("error", "Unknown action."));
                return;
            }
            boolean ok = switch (action) {
                case "rename" -> ChatDAO.rename(userId, id, in.has("title") ? in.get("title").getAsString() : "");
                case "pin" -> ChatDAO.pin(userId, id, in.has("pinned") && in.get("pinned").getAsBoolean());
                case "delete" -> ChatDAO.delete(userId, id);
                default -> ChatDAO.deleteAll(userId) >= 0;
            };
            send(response, ok ? 200 : 404, ok ? Map.of("ok", true) : Map.of("error", "Chat not found."));
        } catch (Exception e) {
            e.printStackTrace();
            send(response, 500, Map.of("error", "Something went wrong."));
        }
    }

    static Map<String, Object> toJson(ChatDAO.Conversation c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.id());
        m.put("title", c.title());
        m.put("pinned", c.pinned());
        m.put("createdAt", iso(c.createdAt()));
        m.put("updatedAt", iso(c.updatedAt()));
        return m;
    }

    static String iso(Timestamp t) {
        return t == null ? null : t.toInstant().toString();
    }

    private static Integer userId(HttpServletRequest request) {
        Object v = request.getSession().getAttribute("userId");
        return v == null ? null : ((Number) v).intValue();
    }

    private void send(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(body));
    }
}
