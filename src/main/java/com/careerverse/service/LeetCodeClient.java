package com.careerverse.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reads PUBLIC LeetCode profile data through LeetCode's GraphQL endpoint.
 *
 * Only the username is used - never a password. The endpoint is unofficial,
 * so every caller must be ready for it to fail and fall back to cached data.
 */
public class LeetCodeClient {

    public static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{1,40}$");

    private static final String ENDPOINT = "https://leetcode.com/graphql";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    static final String QUERY = """
            query careerVerse($username: String!) {
              allQuestionsCount { difficulty count }
              matchedUser(username: $username) {
                username
                profile { ranking userAvatar }
                submitStatsGlobal { acSubmissionNum { difficulty count } }
                userCalendar { streak totalActiveDays submissionCalendar }
                tagProblemCounts {
                  advanced { tagName tagSlug problemsSolved }
                  intermediate { tagName tagSlug problemsSolved }
                  fundamental { tagName tagSlug problemsSolved }
                }
                languageProblemCount { languageName problemsSolved }
              }
              recentAcSubmissionList(username: $username, limit: 20) { title titleSlug timestamp }
            }""";

    public static class UserNotFoundException extends IOException {
        public UserNotFoundException(String username) {
            super("No LeetCode user called '" + username + "' was found.");
        }
    }

    /** Result of one fetch: cleaned-up stats plus the day-by-day calendar. */
    public record Snapshot(JsonObject stats, Map<LocalDate, Integer> calendar) {}

    public static Snapshot fetch(String username) throws IOException, InterruptedException {
        JsonObject variables = new JsonObject();
        variables.addProperty("username", username);
        JsonObject body = new JsonObject();
        body.addProperty("query", QUERY);
        body.add("variables", variables);

        HttpRequest req = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Referer", "https://leetcode.com/" + username + "/")
                .header("User-Agent", "Mozilla/5.0 (CareerVerse AI student project)")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("LeetCode answered with HTTP " + res.statusCode());
        }
        return parse(username, JsonParser.parseString(res.body()).getAsJsonObject());
    }

    /** Turns LeetCode's GraphQL answer into the small JSON object CareerVerse stores. */
    public static Snapshot parse(String username, JsonObject root) throws IOException {
        JsonObject data = root.has("data") && root.get("data").isJsonObject() ? root.getAsJsonObject("data") : null;
        if (data == null || !data.has("matchedUser") || data.get("matchedUser").isJsonNull()) {
            throw new UserNotFoundException(username);
        }
        JsonObject user = data.getAsJsonObject("matchedUser");
        JsonObject stats = new JsonObject();
        stats.addProperty("username", str(user, "username", username));

        JsonObject profile = obj(user, "profile");
        stats.addProperty("ranking", profile == null ? 0 : num(profile, "ranking"));
        stats.addProperty("avatar", profile == null ? "" : str(profile, "userAvatar", ""));

        stats.add("solved", byDifficulty(user.has("submitStatsGlobal")
                ? obj(user, "submitStatsGlobal").getAsJsonArray("acSubmissionNum") : null));
        stats.add("totals", byDifficulty(data.has("allQuestionsCount") && data.get("allQuestionsCount").isJsonArray()
                ? data.getAsJsonArray("allQuestionsCount") : null));

        // SQL problems solved: LeetCode tags them "Database".
        int sql = 0;
        JsonObject tags = obj(user, "tagProblemCounts");
        if (tags != null) {
            for (String level : new String[]{"fundamental", "intermediate", "advanced"}) {
                if (!tags.has(level) || !tags.get(level).isJsonArray()) continue;
                for (JsonElement t : tags.getAsJsonArray(level)) {
                    JsonObject o = t.getAsJsonObject();
                    if ("database".equalsIgnoreCase(str(o, "tagSlug", ""))) sql = Math.max(sql, num(o, "problemsSolved"));
                }
            }
        }

        JsonObject languages = new JsonObject();
        int sqlByLanguage = 0;
        if (user.has("languageProblemCount") && user.get("languageProblemCount").isJsonArray()) {
            for (JsonElement l : user.getAsJsonArray("languageProblemCount")) {
                JsonObject o = l.getAsJsonObject();
                String name = str(o, "languageName", "");
                int solved = num(o, "problemsSolved");
                languages.addProperty(name, solved);
                if (name.matches("(?i)mysql|ms ?sql ?server|oracle|postgresql")) sqlByLanguage += solved;
            }
        }
        stats.addProperty("sqlSolved", Math.max(sql, sqlByLanguage));
        stats.add("languages", languages);

        Map<LocalDate, Integer> calendar = new HashMap<>();
        JsonObject cal = obj(user, "userCalendar");
        if (cal != null) {
            stats.addProperty("leetcodeStreak", num(cal, "streak"));
            stats.addProperty("totalActiveDays", num(cal, "totalActiveDays"));
            calendar = parseCalendar(str(cal, "submissionCalendar", "{}"));
        }

        JsonArray recent = new JsonArray();
        if (data.has("recentAcSubmissionList") && data.get("recentAcSubmissionList").isJsonArray()) {
            for (JsonElement e : data.getAsJsonArray("recentAcSubmissionList")) {
                JsonObject o = e.getAsJsonObject();
                JsonObject r = new JsonObject();
                r.addProperty("title", str(o, "title", ""));
                r.addProperty("slug", str(o, "titleSlug", ""));
                r.addProperty("timestamp", Long.parseLong(str(o, "timestamp", "0")));
                recent.add(r);
            }
        }
        stats.add("recent", recent);
        stats.addProperty("fetchedAt", Instant.now().toString());
        return new Snapshot(stats, calendar);
    }

    /** submissionCalendar is a JSON string: {"1717200000": 3, ...} (unix seconds at UTC midnight). */
    public static Map<LocalDate, Integer> parseCalendar(String json) {
        Map<LocalDate, Integer> out = new HashMap<>();
        if (json == null || json.isBlank()) return out;
        JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        for (var e : o.entrySet()) {
            try {
                LocalDate day = Instant.ofEpochSecond(Long.parseLong(e.getKey())).atZone(ZoneOffset.UTC).toLocalDate();
                out.merge(day, e.getValue().getAsInt(), Integer::sum);
            } catch (NumberFormatException ignored) {
                // skip bad keys
            }
        }
        return out;
    }

    private static JsonObject byDifficulty(JsonArray arr) {
        JsonObject o = new JsonObject();
        o.addProperty("all", 0);
        o.addProperty("easy", 0);
        o.addProperty("medium", 0);
        o.addProperty("hard", 0);
        if (arr == null) return o;
        for (JsonElement e : arr) {
            JsonObject x = e.getAsJsonObject();
            o.addProperty(str(x, "difficulty", "all").toLowerCase(), num(x, "count"));
        }
        return o;
    }

    private static JsonObject obj(JsonObject o, String key) {
        return o != null && o.has(key) && o.get(key).isJsonObject() ? o.getAsJsonObject(key) : null;
    }

    private static String str(JsonObject o, String key, String fallback) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
    }

    private static int num(JsonObject o, String key) {
        try {
            return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
