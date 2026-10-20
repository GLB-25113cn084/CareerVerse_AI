package com.careerverse.servlet;

import com.careerverse.dao.LeetCodeDAO;
import com.careerverse.dao.ProfileDAO;
import com.careerverse.model.CareerData;
import com.careerverse.service.CareerEngine;
import com.careerverse.service.LeetCodeClient;
import com.careerverse.service.PracticeCatalog;
import com.careerverse.service.StreakCalculator;
import com.careerverse.service.StudentContext;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * LeetCode practice tracker.
 *
 * GET  /app/api/leetcode?career=software[&refresh=1][&cached=1]
 * POST /app/api/leetcode  {"action":"link","username":"..."} | {"action":"unlink"} | {"action":"sync-skills"}
 *
 * Only the public username is stored. Stats are refreshed at most every 15 minutes
 * (or when the student presses Refresh). If LeetCode cannot be reached the last saved
 * data is shown instead.
 */
@WebServlet("/app/api/leetcode")
public class LeetCodeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Duration CACHE_TIME = Duration.ofMinutes(15);
    private static final int HEATMAP_DAYS = 365;

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = userId(request);
        if (userId == null) {
            send(response, 401, error("Please log in again."));
            return;
        }
        try {
            Map<String, Object> profile = ProfileDAO.get(userId);
            Map<String, Integer> skills = StudentContext.skillMap(profile);

            String careerId = request.getParameter("career");
            if (careerId == null || CareerData.get(careerId) == null) {
                List<Map<String, Object>> recs = CareerEngine.recommend(StudentContext.latestAptitude(userId), skills,
                        String.valueOf(profile.getOrDefault("branch", "")));
                careerId = recs.isEmpty() ? "software" : String.valueOf(recs.get(0).get("id"));
            }

            JsonObject out = new JsonObject();
            JsonObject career = new JsonObject();
            career.addProperty("id", careerId);
            career.addProperty("name", CareerData.get(careerId).name());
            out.add("career", career);

            LeetCodeDAO.Link link = LeetCodeDAO.get(userId);
            if (link == null) {
                out.addProperty("linked", false);
                out.add("practice", PracticeCatalog.practiceJson(careerId, Set.of()));
                out.add("targets", PracticeCatalog.targetsJson(careerId, Map.of(), 0));
                send(response, 200, out);
                return;
            }

            boolean cachedOnly = "1".equals(request.getParameter("cached"));
            boolean forceRefresh = "1".equals(request.getParameter("refresh"));
            boolean old = link.lastSynced() == null
                    || Duration.between(link.lastSynced().toInstant(), Instant.now()).compareTo(CACHE_TIME) > 0;

            if (!cachedOnly && (forceRefresh || old || link.stats() == null)) {
                try {
                    refresh(userId, link);
                    link = LeetCodeDAO.get(userId);
                } catch (Exception e) {
                    out.addProperty("warning", "Could not reach LeetCode right now (" + e.getMessage()
                            + "). Showing your last saved data.");
                }
            }

            out.addProperty("linked", true);
            out.addProperty("username", link.username());
            out.addProperty("profileUrl", "https://leetcode.com/u/" + link.username() + "/");
            if (link.lastSynced() != null) out.addProperty("lastSynced", link.lastSynced().toInstant().toString());

            JsonObject stats = link.stats();
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            Map<LocalDate, Integer> days = LeetCodeDAO.daily(userId, today.minusDays(HEATMAP_DAYS + 7));
            StreakCalculator.Result streak = StreakCalculator.calculate(days, today);

            JsonObject streakJson = new JsonObject();
            streakJson.addProperty("current", streak.current());
            streakJson.addProperty("longest", streak.longest());
            streakJson.addProperty("activeDays", streak.activeDays());
            streakJson.addProperty("activeDaysThisWeek", streak.activeDaysThisWeek());
            streakJson.addProperty("submissionsThisWeek", streak.submissionsThisWeek());
            streakJson.addProperty("activeToday", streak.activeToday());
            streakJson.addProperty("atRisk", streak.atRisk());
            out.add("streak", streakJson);

            JsonArray heat = new JsonArray();
            for (var e : days.entrySet()) {
                if (e.getKey().isBefore(today.minusDays(HEATMAP_DAYS))) continue;
                JsonArray cell = new JsonArray();
                cell.add(e.getKey().toString());
                cell.add(e.getValue());
                heat.add(cell);
            }
            out.add("heatmap", heat);
            out.addProperty("today", today.toString());

            Set<String> solvedSlugs = new HashSet<>();
            Map<String, Integer> done = new HashMap<>();
            if (stats != null) {
                out.add("stats", stats);
                if (stats.has("seenSolved")) {
                    for (JsonElement s : stats.getAsJsonArray("seenSolved")) solvedSlugs.add(s.getAsString());
                }
                JsonObject solved = stats.getAsJsonObject("solved");
                if (link.baselineSet()) {
                    done.put("easy", Math.max(0, solved.get("easy").getAsInt() - link.baseEasy()));
                    done.put("medium", Math.max(0, solved.get("medium").getAsInt() - link.baseMedium()));
                    done.put("hard", Math.max(0, solved.get("hard").getAsInt() - link.baseHard()));
                    int sql = stats.has("sqlSolved") ? stats.get("sqlSolved").getAsInt() : 0;
                    done.put("sql", Math.max(0, sql - link.baseSql()));
                }
                out.add("suggestedSkills", PracticeCatalog.suggestedSkills(stats, skills));
            }
            JsonObject sinceLinking = new JsonObject();
            done.forEach(sinceLinking::addProperty);
            out.add("sinceLinking", sinceLinking);

            out.add("practice", PracticeCatalog.practiceJson(careerId, solvedSlugs));
            out.add("targets", PracticeCatalog.targetsJson(careerId, done, streak.activeDaysThisWeek()));
            send(response, 200, out);

        } catch (Exception e) {
            e.printStackTrace();
            send(response, 500, error("Could not load your practice data."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = userId(request);
        if (userId == null) {
            send(response, 401, error("Please log in again."));
            return;
        }
        try {
            JsonObject in = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String action = in.has("action") ? in.get("action").getAsString() : "";
            JsonObject out = new JsonObject();

            switch (action) {
                case "link" -> {
                    String username = in.has("username") ? in.get("username").getAsString().trim() : "";
                    if (username.startsWith("https://") || username.contains("leetcode.com/")) {
                        username = username.replaceAll(".*leetcode\\.com/(u/)?", "").replaceAll("/.*", "");
                    }
                    if (!LeetCodeDAO.validUsername(username)) {
                        send(response, 400, error("Please enter a valid LeetCode username (letters, numbers, - or _)."));
                        return;
                    }
                    LeetCodeClient.Snapshot snap = null;
                    try {
                        snap = LeetCodeClient.fetch(username);
                    } catch (LeetCodeClient.UserNotFoundException notFound) {
                        send(response, 404, error(notFound.getMessage()));
                        return;
                    } catch (Exception network) {
                        out.addProperty("warning", "Saved, but LeetCode could not be reached right now. "
                                + "Your stats will appear on the next refresh.");
                    }
                    if (snap != null) username = snap.stats().get("username").getAsString();
                    LeetCodeDAO.link(userId, username);
                    if (snap != null) {
                        LeetCodeDAO.Link link = LeetCodeDAO.get(userId);
                        LeetCodeDAO.saveSnapshot(userId, withSeen(snap.stats(), null), snap.calendar(),
                                !link.baselineSet());
                    }
                    out.addProperty("ok", true);
                    out.addProperty("message", "LeetCode account '" + username + "' linked.");
                }
                case "unlink" -> {
                    LeetCodeDAO.unlink(userId);
                    out.addProperty("ok", true);
                    out.addProperty("message", "LeetCode account removed from CareerVerse.");
                }
                case "sync-skills" -> {
                    LeetCodeDAO.Link link = LeetCodeDAO.get(userId);
                    if (link == null || link.stats() == null) {
                        send(response, 400, error("Link your LeetCode account first."));
                        return;
                    }
                    Map<String, Integer> skills = StudentContext.skillMap(ProfileDAO.get(userId));
                    Map<String, Integer> raise = new LinkedHashMap<>();
                    for (JsonElement e : PracticeCatalog.suggestedSkills(link.stats(), skills)) {
                        JsonObject o = e.getAsJsonObject();
                        if (o.get("canRaise").getAsBoolean()) {
                            raise.put(o.get("skill").getAsString(), o.get("suggested").getAsInt());
                        }
                    }
                    LeetCodeDAO.raiseSkills(userId, raise);
                    out.addProperty("ok", true);
                    out.add("updated", gson.toJsonTree(raise));
                    out.addProperty("message", raise.isEmpty()
                            ? "Your profile skills are already up to date."
                            : "Updated " + String.join(", ", raise.keySet()) + " in your profile.");
                }
                default -> {
                    send(response, 400, error("Unknown action."));
                    return;
                }
            }
            send(response, 200, out);
        } catch (Exception e) {
            e.printStackTrace();
            send(response, 500, error("Something went wrong. Please try again."));
        }
    }

    /* ---------- helpers ---------- */

    private void refresh(int userId, LeetCodeDAO.Link link) throws Exception {
        LeetCodeClient.Snapshot snap = LeetCodeClient.fetch(link.username());
        LeetCodeDAO.saveSnapshot(userId, withSeen(snap.stats(), link.stats()), snap.calendar(), !link.baselineSet());
    }

    /** Remembers every accepted problem we have ever seen, so solved ticks survive beyond LeetCode's recent list. */
    static JsonObject withSeen(JsonObject fresh, JsonObject previous) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (JsonElement r : fresh.getAsJsonArray("recent")) seen.add(r.getAsJsonObject().get("slug").getAsString());
        if (previous != null && previous.has("seenSolved")) {
            for (JsonElement s : previous.getAsJsonArray("seenSolved")) {
                if (seen.size() >= 1000) break;
                seen.add(s.getAsString());
            }
        }
        JsonArray arr = new JsonArray();
        seen.forEach(arr::add);
        fresh.add("seenSolved", arr);
        return fresh;
    }

    private static Integer userId(HttpServletRequest request) {
        Object v = request.getSession().getAttribute("userId");
        return v == null ? null : ((Number) v).intValue();
    }

    private static JsonObject error(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("error", message);
        return o;
    }

    private void send(HttpServletResponse response, int status, JsonObject body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(body));
    }
}
