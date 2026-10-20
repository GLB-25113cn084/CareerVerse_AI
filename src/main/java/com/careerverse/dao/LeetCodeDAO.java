package com.careerverse.dao;

import com.careerverse.config.DB;
import com.careerverse.service.LeetCodeClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

/** Database access for the LeetCode tables (leetcode_profiles and leetcode_daily). */
public class LeetCodeDAO {

    public record Link(String username, Timestamp lastSynced, JsonObject stats,
                       int baseEasy, int baseMedium, int baseHard, int baseSql, boolean baselineSet) {}

    public static Link get(int userId) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT username, last_synced, stats_json, base_easy, base_medium, base_hard, base_sql, baseline_set "
                   + "FROM leetcode_profiles WHERE user_id=?")) {
            s.setInt(1, userId);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) return null;
                String json = r.getString("stats_json");
                JsonObject stats = null;
                if (json != null && !json.isBlank()) {
                    try {
                        stats = JsonParser.parseString(json).getAsJsonObject();
                    } catch (Exception ignored) {
                        stats = null;
                    }
                }
                return new Link(r.getString("username"), r.getTimestamp("last_synced"), stats,
                        r.getInt("base_easy"), r.getInt("base_medium"), r.getInt("base_hard"),
                        r.getInt("base_sql"), r.getBoolean("baseline_set"));
            }
        }
    }

    /** Saves (or changes) the LeetCode username. Changing it clears the old account's history. */
    public static void link(int userId, String username) throws SQLException {
        Link existing = get(userId);
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (existing != null && !existing.username().equalsIgnoreCase(username)) {
                    try (PreparedStatement d = c.prepareStatement("DELETE FROM leetcode_daily WHERE user_id=?")) {
                        d.setInt(1, userId);
                        d.executeUpdate();
                    }
                }
                if (existing == null || !existing.username().equalsIgnoreCase(username)) {
                    try (PreparedStatement s = c.prepareStatement(
                            "REPLACE INTO leetcode_profiles (user_id, username, linked_at, baseline_set, stats_json, last_synced) "
                          + "VALUES (?, ?, CURRENT_TIMESTAMP, FALSE, NULL, NULL)")) {
                        s.setInt(1, userId);
                        s.setString(2, username);
                        s.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static void unlink(int userId) throws SQLException {
        try (Connection c = DB.getConnection()) {
            for (String sql : new String[]{"DELETE FROM leetcode_daily WHERE user_id=?",
                                           "DELETE FROM leetcode_profiles WHERE user_id=?"}) {
                try (PreparedStatement s = c.prepareStatement(sql)) {
                    s.setInt(1, userId);
                    s.executeUpdate();
                }
            }
        }
    }

    /** Stores the latest stats and day-by-day counts. The first save also records the baseline. */
    public static void saveSnapshot(int userId, JsonObject stats, Map<LocalDate, Integer> calendar,
                                    boolean setBaseline) throws SQLException {
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE leetcode_profiles SET stats_json=?, last_synced=CURRENT_TIMESTAMP WHERE user_id=?")) {
                    s.setString(1, stats.toString());
                    s.setInt(2, userId);
                    s.executeUpdate();
                }
                if (setBaseline) {
                    JsonObject solved = stats.getAsJsonObject("solved");
                    try (PreparedStatement s = c.prepareStatement(
                            "UPDATE leetcode_profiles SET base_easy=?, base_medium=?, base_hard=?, base_sql=?, "
                          + "baseline_set=TRUE WHERE user_id=?")) {
                        s.setInt(1, solved.get("easy").getAsInt());
                        s.setInt(2, solved.get("medium").getAsInt());
                        s.setInt(3, solved.get("hard").getAsInt());
                        s.setInt(4, stats.has("sqlSolved") ? stats.get("sqlSolved").getAsInt() : 0);
                        s.setInt(5, userId);
                        s.executeUpdate();
                    }
                }
                if (!calendar.isEmpty()) {
                    try (PreparedStatement s = c.prepareStatement(
                            "INSERT INTO leetcode_daily (user_id, day, submissions) VALUES (?, ?, ?) "
                          + "ON DUPLICATE KEY UPDATE submissions=GREATEST(submissions, ?)")) {
                        for (var e : calendar.entrySet()) {
                            s.setInt(1, userId);
                            s.setDate(2, Date.valueOf(e.getKey()));
                            s.setInt(3, e.getValue());
                            s.setInt(4, e.getValue());
                            s.addBatch();
                        }
                        s.executeBatch();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Day -> submissions, from the given day onwards. */
    public static Map<LocalDate, Integer> daily(int userId, LocalDate from) throws SQLException {
        Map<LocalDate, Integer> out = new TreeMap<>();
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT day, submissions FROM leetcode_daily WHERE user_id=? AND day>=? ORDER BY day")) {
            s.setInt(1, userId);
            s.setDate(2, Date.valueOf(from));
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) out.put(r.getDate(1).toLocalDate(), r.getInt(2));
            }
        }
        return out;
    }

    /** Raises profile skills to the suggested level. Levels are never lowered. */
    public static int raiseSkills(int userId, Map<String, Integer> levels) throws SQLException {
        int changed = 0;
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "INSERT INTO profile_skills (user_id, skill_name, skill_level) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE skill_level=GREATEST(skill_level, ?)")) {
            for (var e : levels.entrySet()) {
                int level = Math.max(1, Math.min(5, e.getValue()));
                s.setInt(1, userId);
                s.setString(2, e.getKey());
                s.setInt(3, level);
                s.setInt(4, level);
                changed += s.executeUpdate() > 0 ? 1 : 0;
            }
        }
        return changed;
    }

    /** One-line summary used by the AI chatbot. Returns null when LeetCode is not linked. */
    public static String summary(int userId) throws SQLException {
        Link link = get(userId);
        if (link == null || link.stats() == null) return null;
        JsonObject solved = link.stats().getAsJsonObject("solved");
        int streak = 0;
        try {
            streak = com.careerverse.service.StreakCalculator
                    .calculate(daily(userId, LocalDate.now(java.time.ZoneOffset.UTC).minusDays(400)),
                               LocalDate.now(java.time.ZoneOffset.UTC)).current();
        } catch (Exception ignored) {
            // streak is optional
        }
        return "LeetCode (" + link.username() + "): " + solved.get("all").getAsInt() + " solved ("
                + solved.get("easy").getAsInt() + " easy, " + solved.get("medium").getAsInt() + " medium, "
                + solved.get("hard").getAsInt() + " hard), "
                + (link.stats().has("sqlSolved") ? link.stats().get("sqlSolved").getAsInt() : 0)
                + " SQL problems, current CareerVerse streak " + streak + " days.";
    }

    /** Validates a username before it is stored. */
    public static boolean validUsername(String username) {
        return username != null && LeetCodeClient.USERNAME.matcher(username).matches();
    }
}
