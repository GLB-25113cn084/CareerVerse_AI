package com.careerverse.config;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place for database connections and configuration values.
 *
 * Every value is read from an environment variable first (used on Render)
 * and falls back to src/main/resources/db.properties (used on your laptop).
 *
 * DB_URL can be either a normal JDBC URL
 *   jdbc:mysql://host:port/defaultdb?sslMode=REQUIRED
 * or the "Service URI" copied straight from the Aiven console
 *   mysql://avnadmin:password@host:port/defaultdb?ssl-mode=REQUIRED
 */
public class DB {

    private static final Properties p = new Properties();

    static {
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception e) {
            System.err.println("CareerVerse: could not read db.properties - " + e.getMessage());
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CareerVerse: MySQL driver not found on the classpath.");
        }
    }

    private static String value(String envName, String propertyName) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) return env.trim();
        return p.getProperty(propertyName, "").trim();
    }

    public static Connection getConnection() throws SQLException {
        String url = value("DB_URL", "db.url");
        String user = value("DB_USER", "db.user");
        String password = value("DB_PASSWORD", "db.password");

        if (url.startsWith("mysql://")) {
            String[] converted = fromServiceUri(url);
            url = converted[0];
            if (user.isEmpty()) user = converted[1];
            if (password.isEmpty()) password = converted[2];
        }

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("connectTimeout", "10000");
        return DriverManager.getConnection(url, props);
    }

    /** Converts an Aiven service URI into {jdbcUrl, user, password}. */
    static String[] fromServiceUri(String serviceUri) {
        URI uri = URI.create(serviceUri);
        String user = "";
        String password = "";
        if (uri.getRawUserInfo() != null) {
            String[] parts = uri.getRawUserInfo().split(":", 2);
            user = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            if (parts.length > 1) password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        }
        String database = uri.getPath() == null || uri.getPath().length() <= 1
                ? "defaultdb" : uri.getPath().substring(1);
        int port = uri.getPort() == -1 ? 3306 : uri.getPort();
        String jdbc = "jdbc:mysql://" + uri.getHost() + ":" + port + "/" + database
                + "?sslMode=REQUIRED&serverTimezone=UTC&characterEncoding=UTF-8";
        return new String[]{jdbc, user, password};
    }

    /** Reads a setting such as mail.host or ai.provider. */
    public static String prop(String key) {
        String envName = key.toUpperCase().replace('.', '_');
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) return env.trim();
        return p.getProperty(key, "").trim();
    }

    /** Reads a setting and returns a default when it is empty. */
    public static String prop(String key, String fallback) {
        String v = prop(key);
        return v.isEmpty() ? fallback : v;
    }
}
