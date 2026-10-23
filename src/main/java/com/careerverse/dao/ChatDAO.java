package com.careerverse.dao;

import com.careerverse.config.DB;

import java.sql.*;
import java.util.*;

/**
 * Saved AI chats. Every query checks user_id, so students can only see their own chats.
 */
public class ChatDAO {

    public record Conversation(int id, String title, boolean pinned, Timestamp createdAt, Timestamp updatedAt) {}
    public record Message(int id, String role, String content, Timestamp createdAt) {}

    public static final int MAX_TITLE = 120;

    /** All chats of a student: pinned first, then most recently used. */
    public static List<Conversation> list(int userId) throws SQLException {
        List<Conversation> out = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT id, title, pinned, created_at, updated_at FROM chat_conversations "
                   + "WHERE user_id=? ORDER BY pinned DESC, updated_at DESC, id DESC LIMIT 200")) {
            s.setInt(1, userId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) out.add(read(r));
            }
        }
        return out;
    }

    public static Conversation get(int userId, int id) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT id, title, pinned, created_at, updated_at FROM chat_conversations WHERE id=? AND user_id=?")) {
            s.setInt(1, id);
            s.setInt(2, userId);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? read(r) : null;
            }
        }
    }

    public static int create(int userId, String title) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "INSERT INTO chat_conversations (user_id, title) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, userId);
            s.setString(2, cleanTitle(title));
            s.executeUpdate();
            try (ResultSet r = s.getGeneratedKeys()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    public static List<Message> messages(int userId, int conversationId) throws SQLException {
        List<Message> out = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT m.id, m.role, m.content, m.created_at FROM chat_messages m "
                   + "JOIN chat_conversations cc ON cc.id = m.conversation_id "
                   + "WHERE cc.id=? AND cc.user_id=? ORDER BY m.id")) {
            s.setInt(1, conversationId);
            s.setInt(2, userId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) out.add(new Message(r.getInt(1), r.getString(2), r.getString(3), r.getTimestamp(4)));
            }
        }
        return out;
    }

    /** Last few messages, oldest first, used as the AI's memory of the conversation. */
    public static List<Message> recent(int userId, int conversationId, int limit) throws SQLException {
        List<Message> all = messages(userId, conversationId);
        return all.subList(Math.max(0, all.size() - limit), all.size());
    }

    public static void addMessage(int conversationId, String role, String content) throws SQLException {
        try (Connection c = DB.getConnection()) {
            try (PreparedStatement s = c.prepareStatement(
                    "INSERT INTO chat_messages (conversation_id, role, content) VALUES (?, ?, ?)")) {
                s.setInt(1, conversationId);
                s.setString(2, role);
                s.setString(3, content);
                s.executeUpdate();
            }
            try (PreparedStatement s = c.prepareStatement(
                    "UPDATE chat_conversations SET updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
                s.setInt(1, conversationId);
                s.executeUpdate();
            }
        }
    }

    /** Removes the last AI answer (used by "Regenerate"). Returns the question it answered, or null. */
    public static String removeLastAnswer(int userId, int conversationId) throws SQLException {
        List<Message> all = messages(userId, conversationId);
        if (all.isEmpty()) return null;
        Message last = all.get(all.size() - 1);
        if ("assistant".equals(last.role())) {
            try (Connection c = DB.getConnection();
                 PreparedStatement s = c.prepareStatement("DELETE FROM chat_messages WHERE id=? AND conversation_id=?")) {
                s.setInt(1, last.id());
                s.setInt(2, conversationId);
                s.executeUpdate();
            }
            all = all.subList(0, all.size() - 1);
        }
        for (int i = all.size() - 1; i >= 0; i--) {
            if ("user".equals(all.get(i).role())) return all.get(i).content();
        }
        return null;
    }

    public static boolean rename(int userId, int id, String title) throws SQLException {
        return update("UPDATE chat_conversations SET title=? WHERE id=? AND user_id=?", cleanTitle(title), id, userId);
    }

    public static boolean pin(int userId, int id, boolean pinned) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement("UPDATE chat_conversations SET pinned=? WHERE id=? AND user_id=?")) {
            s.setBoolean(1, pinned);
            s.setInt(2, id);
            s.setInt(3, userId);
            return s.executeUpdate() > 0;
        }
    }

    public static boolean delete(int userId, int id) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM chat_conversations WHERE id=? AND user_id=?")) {
            s.setInt(1, id);
            s.setInt(2, userId);
            return s.executeUpdate() > 0;
        }
    }

    public static int deleteAll(int userId) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM chat_conversations WHERE user_id=?")) {
            s.setInt(1, userId);
            return s.executeUpdate();
        }
    }

    /** Turns the first question into a short chat title. */
    public static String titleFrom(String question) {
        String t = question.replaceAll("\\s+", " ").trim();
        t = t.replaceAll("^(?i)(hi|hello|hey|hii+)[,!. ]+", "");
        if (t.isEmpty()) t = "New chat";
        t = Character.toUpperCase(t.charAt(0)) + t.substring(1);
        if (t.length() > 48) {
            int cut = t.lastIndexOf(' ', 48);
            t = t.substring(0, cut > 20 ? cut : 48) + "…";
        }
        return t;
    }

    static String cleanTitle(String title) {
        String t = title == null ? "" : title.replaceAll("\\s+", " ").trim();
        if (t.isEmpty()) t = "New chat";
        return t.length() > MAX_TITLE ? t.substring(0, MAX_TITLE) : t;
    }

    private static boolean update(String sql, String text, int id, int userId) throws SQLException {
        try (Connection c = DB.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, text);
            s.setInt(2, id);
            s.setInt(3, userId);
            return s.executeUpdate() > 0;
        }
    }

    private static Conversation read(ResultSet r) throws SQLException {
        return new Conversation(r.getInt("id"), r.getString("title"), r.getBoolean("pinned"),
                r.getTimestamp("created_at"), r.getTimestamp("updated_at"));
    }
}
