package com.careerverse.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {
    private static final Properties p = new Properties();

    static {
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) p.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String value(String envName, String propertyName) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) return env;
        return p.getProperty(propertyName, "");
    }

    public static Connection getConnection() throws SQLException {
        String url = value("DB_URL", "db.url");
        String user = value("DB_USER", "db.user");
        String password = value("DB_PASSWORD", "db.password");
        return DriverManager.getConnection(url, user, password);
    }

    public static String prop(String key) {
        String envName = switch (key) {
            case "mail.host" -> "MAIL_HOST";
            case "mail.port" -> "MAIL_PORT";
            case "mail.username" -> "MAIL_USERNAME";
            case "mail.password" -> "MAIL_PASSWORD";
            case "ollama.url" -> "OLLAMA_URL";
            case "ollama.model" -> "OLLAMA_MODEL";
            default -> "";
        };
        if (!envName.isEmpty()) {
            String env = System.getenv(envName);
            if (env != null && !env.isBlank()) return env;
        }
        return p.getProperty(key, "");
    }
}
