package com.careerverse.dao;

import com.careerverse.config.DB;

import java.sql.*;
import java.util.*;

public class ProfileDAO {

    public static Map<String, Object> get(int uid) throws SQLException {

        Map<String, Object> m = new LinkedHashMap<>();

        try (
            Connection c = DB.getConnection();
            PreparedStatement s = c.prepareStatement(
                "SELECT branch, current_year, interests " +
                "FROM profiles WHERE user_id=?"
            )
        ) {

            s.setInt(1, uid);

            try (ResultSet r = s.executeQuery()) {

                if (r.next()) {
                    m.put("branch", r.getString("branch"));
                    m.put("currentYear", r.getInt("current_year"));
                    m.put("interests", r.getString("interests"));
                } else {
                    m.put("branch", "");
                    m.put("currentYear", 1);
                    m.put("interests", "");
                }
            }
        }

        m.put("cgpa", years(uid));
        m.put("skills", skills(uid));

        return m;
    }

    public static void save(
            int uid,
            String branch,
            int year,
            String interests,
            List<Map<String, Object>> cgpa,
            List<Map<String, Object>> skills
    ) throws SQLException {

        /*
         * A student in:
         * 1st year -> no completed-year CGPA
         * 2nd year -> Year 1
         * 3rd year -> Year 1 + Year 2
         * 4th year -> Year 1 + Year 2 + Year 3
         */
        int maxCompletedYear = Math.max(0, year - 1);

        try (Connection c = DB.getConnection()) {

            c.setAutoCommit(false);

            try {

                // Save main profile
                try (
                    PreparedStatement s = c.prepareStatement(
                        "UPDATE profiles " +
                        "SET branch=?, current_year=?, interests=? " +
                        "WHERE user_id=?"
                    )
                ) {

                    s.setString(1, branch == null ? "" : branch);
                    s.setInt(2, year);
                    s.setString(3, interests == null ? "" : interests);
                    s.setInt(4, uid);

                    s.executeUpdate();
                }

                // Replace academic records
                try (
                    PreparedStatement d = c.prepareStatement(
                        "DELETE FROM academic_records WHERE user_id=?"
                    )
                ) {
                    d.setInt(1, uid);
                    d.executeUpdate();
                }

                try (
                    PreparedStatement s = c.prepareStatement(
                        "INSERT INTO academic_records " +
                        "(user_id, academic_year, cgpa) VALUES (?, ?, ?)"
                    )
                ) {

                    for (Map<String, Object> x : cgpa) {

                        int academicYear =
                                ((Number) x.get("year")).intValue();

                        double value =
                                ((Number) x.get("cgpa")).doubleValue();

                        // Only save completed academic years
                        if (academicYear >= 1 &&
                            academicYear <= maxCompletedYear &&
                            value >= 0 &&
                            value <= 10) {

                            s.setInt(1, uid);
                            s.setInt(2, academicYear);
                            s.setDouble(3, value);

                            s.addBatch();
                        }
                    }

                    s.executeBatch();
                }

                // Replace skills
                try (
                    PreparedStatement d = c.prepareStatement(
                        "DELETE FROM profile_skills WHERE user_id=?"
                    )
                ) {

                    d.setInt(1, uid);
                    d.executeUpdate();
                }

                try (
                    PreparedStatement s = c.prepareStatement(
                        "INSERT INTO profile_skills " +
                        "(user_id, skill_name, skill_level) VALUES (?, ?, ?)"
                    )
                ) {

                    for (Map<String, Object> x : skills) {

                        String name = String.valueOf(x.get("name")).trim();

                        if (name.isEmpty()) {
                            continue;
                        }

                        int level =
                                ((Number) x.getOrDefault("level", 3))
                                .intValue();

                        level = Math.max(1, Math.min(5, level));

                        s.setInt(1, uid);
                        s.setString(2, name);
                        s.setInt(3, level);

                        s.addBatch();
                    }

                    s.executeBatch();
                }

                c.commit();

            } catch (Exception e) {

                c.rollback();
                throw e;
            } finally {

                c.setAutoCommit(true);
            }
        }
    }

    private static List<Map<String, Object>> years(int uid)
            throws SQLException {

        List<Map<String, Object>> list = new ArrayList<>();

        try (
            Connection c = DB.getConnection();
            PreparedStatement s = c.prepareStatement(
                "SELECT academic_year, cgpa " +
                "FROM academic_records " +
                "WHERE user_id=? " +
                "ORDER BY academic_year"
            )
        ) {

            s.setInt(1, uid);

            try (ResultSet r = s.executeQuery()) {

                while (r.next()) {

                    Map<String, Object> m =
                            new LinkedHashMap<>();

                    m.put("year", r.getInt("academic_year"));
                    m.put("cgpa", r.getDouble("cgpa"));

                    list.add(m);
                }
            }
        }

        return list;
    }

    private static List<Map<String, Object>> skills(int uid)
            throws SQLException {

        List<Map<String, Object>> list = new ArrayList<>();

        try (
            Connection c = DB.getConnection();
            PreparedStatement s = c.prepareStatement(
                "SELECT skill_name, skill_level " +
                "FROM profile_skills " +
                "WHERE user_id=? " +
                "ORDER BY skill_name"
            )
        ) {

            s.setInt(1, uid);

            try (ResultSet r = s.executeQuery()) {

                while (r.next()) {

                    Map<String, Object> m =
                            new LinkedHashMap<>();

                    m.put("name", r.getString("skill_name"));
                    m.put("level", r.getInt("skill_level"));

                    list.add(m);
                }
            }
        }

        return list;
    }
}