package com.careerverse.dao;

import com.careerverse.config.DB;

import java.sql.*;

public class UserDAO {

    public record Result(int id, String name, String email, String hash) {}

    public static Integer findIdByEmail(String email) throws SQLException {
        Result r = find(email);
        return r == null ? null : r.id();
    }

    public static Result find(String email) throws SQLException {
        if (email == null) return null;
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT id, name, email, password_hash FROM users WHERE email=?")) {
            s.setString(1, email.trim().toLowerCase());
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? new Result(r.getInt(1), r.getString(2), r.getString(3), r.getString(4)) : null;
            }
        }
    }

    public static Result findById(int id) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT id, name, email, password_hash FROM users WHERE id=?")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? new Result(r.getInt(1), r.getString(2), r.getString(3), r.getString(4)) : null;
            }
        }
    }

    /** Creates the user and an empty profile row. Returns the new user id. */
    public static int create(String name, String email, String hash) throws SQLException {
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO users(name, email, password_hash) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1, name);
                    s.setString(2, email.trim().toLowerCase());
                    s.setString(3, hash);
                    s.executeUpdate();
                    try (ResultSet r = s.getGeneratedKeys()) {
                        r.next();
                        id = r.getInt(1);
                    }
                }
                try (PreparedStatement p = c.prepareStatement("INSERT INTO profiles(user_id) VALUES(?)")) {
                    p.setInt(1, id);
                    p.executeUpdate();
                }
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static void changePassword(int id, String hash) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement s = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
            s.setString(1, hash);
            s.setInt(2, id);
            s.executeUpdate();
        }
    }
}
