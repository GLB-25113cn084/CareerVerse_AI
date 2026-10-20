package com.careerverse.service;

import com.careerverse.config.DB;
import com.careerverse.dao.LeetCodeDAO;
import com.careerverse.dao.ProfileDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Collects what CareerVerse knows about a student so that the AI chatbot
 * can give personal answers ("you are in 3rd year, your top match is Data Analyst...").
 */
public class StudentContext {

    /** Skill name (lower case) -> level 1..5 from the student profile. */
    public static Map<String, Integer> skillMap(Map<String, Object> profile) {
        Map<String, Integer> out = new HashMap<>();
        Object list = profile.get("skills");
        if (list instanceof List<?> items) {
            for (Object item : items) {
                if (item instanceof Map<?, ?> m && m.get("name") != null && m.get("level") instanceof Number n) {
                    out.put(m.get("name").toString().toLowerCase().trim(), n.intValue());
                }
            }
        }
        return out;
    }

    /** Latest aptitude scores, or 50 for every area when the test has not been taken. */
    public static Map<String, Integer> latestAptitude(int userId) {
        Map<String, Integer> out = new LinkedHashMap<>();
        String[] keys = {"logical", "programming", "communication", "mathematics", "creativity", "analytical"};
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "SELECT logical_score, programming_score, communication_score, mathematics_score, "
                   + "creativity_score, analytical_score FROM assessments WHERE user_id=? "
                   + "ORDER BY completed_at DESC, id DESC LIMIT 1")) {
            s.setInt(1, userId);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    for (int i = 0; i < keys.length; i++) out.put(keys[i], r.getInt(i + 1));
                    return out;
                }
            }
        } catch (Exception ignored) {
            // fall through to neutral scores
        }
        for (String k : keys) out.put(k, 50);
        return out;
    }

    /** A short plain-text summary that is added to the AI prompt. */
    public static String describe(int userId, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student name: ").append(name == null ? "Student" : name).append('\n');
        try {
            Map<String, Object> profile = ProfileDAO.get(userId);
            String branch = String.valueOf(profile.getOrDefault("branch", ""));
            if (!branch.isBlank()) sb.append("Branch: ").append(branch).append('\n');
            sb.append("Current year of study: ").append(profile.getOrDefault("currentYear", 1)).append('\n');
            String interests = String.valueOf(profile.getOrDefault("interests", ""));
            if (!interests.isBlank() && !"null".equals(interests)) sb.append("Interests: ").append(interests).append('\n');

            Object cgpa = profile.get("cgpa");
            if (cgpa instanceof List<?> l && !l.isEmpty()) {
                sb.append("CGPA by year: ");
                for (Object o : l) {
                    if (o instanceof Map<?, ?> m) sb.append("Y").append(m.get("year")).append('=').append(m.get("cgpa")).append(' ');
                }
                sb.append('\n');
            }

            Map<String, Integer> skills = skillMap(profile);
            if (!skills.isEmpty()) {
                sb.append("Skills (level out of 5): ");
                skills.forEach((k, v) -> sb.append(k).append('=').append(v).append(", "));
                sb.setLength(sb.length() - 2);
                sb.append('\n');
            } else {
                sb.append("Skills: not filled in yet\n");
            }

            List<Map<String, Object>> recs = CareerEngine.recommend(latestAptitude(userId), skills);
            sb.append("Top career matches from CareerVerse: ");
            for (int i = 0; i < Math.min(3, recs.size()); i++) {
                sb.append(recs.get(i).get("name")).append(" (").append(recs.get(i).get("match")).append("%)");
                if (i < 2) sb.append(", ");
            }
            sb.append('\n');
        } catch (Exception e) {
            sb.append("(Profile could not be loaded)\n");
        }

        try {
            String lc = LeetCodeDAO.summary(userId);
            if (lc != null) sb.append(lc).append('\n');
        } catch (Exception ignored) {
            // LeetCode is optional
        }
        return sb.toString();
    }
}
