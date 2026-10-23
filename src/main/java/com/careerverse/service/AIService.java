package com.careerverse.service;

import com.careerverse.config.DB;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Talks to the large language model behind the CareerVerse chatbot.
 *
 * Three providers are supported and chosen with the AI_PROVIDER variable:
 *   groq   - free cloud API, works on Render (needs GROQ_API_KEY)
 *   gemini - Google's cloud API, works on Render (needs GEMINI_API_KEY)
 *   ollama - a model running on your own laptop (only works locally)
 * When AI_PROVIDER is empty the first provider that has a key is used.
 */
public class AIService {

    public record Message(String role, String content) {}

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    static final String SYSTEM_PROMPT = """
            You are CareerVerse AI, the friendly career mentor inside the CareerVerse AI web app \
            built for engineering students in India.
            Help with careers, skills, learning roadmaps, projects, internships, resumes, interviews \
            and coding practice (including LeetCode).
            Rules:
            - Use the student details below to personalise the answer. Do not invent facts about the student.
            - Be practical: give concrete next steps, resources and small milestones.
            - Keep answers short (under 200 words) unless the student asks for detail. Use short bullet points.
            - If a question has nothing to do with studies or careers, answer briefly and steer back politely.
            - Never ask for passwords. Never claim to be a human.
            """;

    public static String provider() {
        String p = DB.prop("ai.provider").toLowerCase();
        if (!p.isEmpty()) return p;
        if (!DB.prop("groq.api.key").isEmpty()) return "groq";
        if (!DB.prop("gemini.api.key").isEmpty()) return "gemini";
        return "ollama";
    }

    public static String ask(String studentContext, List<Message> history, String question)
            throws IOException, InterruptedException {
        String system = SYSTEM_PROMPT + "\nStudent details:\n" + studentContext;
        return switch (provider()) {
            case "groq" -> openAiCompatible(
                    "https://api.groq.com/openai/v1/chat/completions",
                    DB.prop("groq.api.key"),
                    DB.prop("groq.model", "openai/gpt-oss-120b"),
                    system, history, question);
            case "gemini" -> gemini(system, history, question);
            case "ollama" -> ollama(system, history, question);
            default -> throw new IOException("Unknown AI_PROVIDER '" + provider() + "'. Use groq, gemini or ollama.");
        };
    }

    /* ---------- Groq (OpenAI-compatible chat API) ---------- */

    private static String openAiCompatible(String url, String key, String model, String system,
                                           List<Message> history, String question)
            throws IOException, InterruptedException {
        if (key.isEmpty()) throw new IOException("GROQ_API_KEY is not set.");
        JsonArray messages = new JsonArray();
        messages.add(msg("system", system));
        for (Message m : history) messages.add(msg(m.role(), m.content()));
        messages.add(msg("user", question));

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("temperature", 0.6);
        body.addProperty("max_tokens", 1500);
        if (model.startsWith("openai/gpt-oss")) body.addProperty("reasoning_effort", "low");

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + key)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        JsonObject json = send(req);
        return json.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString().trim();
    }

    /* ---------- Google Gemini ---------- */

    private static String gemini(String system, List<Message> history, String question)
            throws IOException, InterruptedException {
        String key = DB.prop("gemini.api.key");
        if (key.isEmpty()) throw new IOException("GEMINI_API_KEY is not set.");
        String model = DB.prop("gemini.model", "gemini-2.5-flash");

        JsonArray contents = new JsonArray();
        for (Message m : history) contents.add(geminiPart("assistant".equals(m.role()) ? "model" : "user", m.content()));
        contents.add(geminiPart("user", question));

        JsonObject sys = new JsonObject();
        JsonArray sysParts = new JsonArray();
        JsonObject sysText = new JsonObject();
        sysText.addProperty("text", system);
        sysParts.add(sysText);
        sys.add("parts", sysParts);

        JsonObject config = new JsonObject();
        config.addProperty("temperature", 0.6);
        config.addProperty("maxOutputTokens", 900);

        JsonObject body = new JsonObject();
        body.add("system_instruction", sys);
        body.add("contents", contents);
        body.add("generationConfig", config);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + URLEncoder.encode(model, StandardCharsets.UTF_8) + ":generateContent";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", key)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        JsonObject json = send(req);
        StringBuilder out = new StringBuilder();
        JsonArray parts = json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                .getAsJsonObject("content").getAsJsonArray("parts");
        for (JsonElement p : parts) {
            JsonObject o = p.getAsJsonObject();
            if (o.has("text")) out.append(o.get("text").getAsString());
        }
        return out.toString().trim();
    }

    /* ---------- Ollama (local) ---------- */

    private static String ollama(String system, List<Message> history, String question)
            throws IOException, InterruptedException {
        String base = DB.prop("ollama.url", "http://localhost:11434").replaceAll("/+$", "");
        JsonArray messages = new JsonArray();
        messages.add(msg("system", system));
        for (Message m : history) messages.add(msg(m.role(), m.content()));
        messages.add(msg("user", question));

        JsonObject body = new JsonObject();
        body.addProperty("model", DB.prop("ollama.model", "llama3.2"));
        body.add("messages", messages);
        body.addProperty("stream", false);

        HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/chat"))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        JsonObject json = send(req);
        return json.getAsJsonObject("message").get("content").getAsString().trim();
    }

    /* ---------- helpers ---------- */

    private static JsonObject send(HttpRequest req) throws IOException, InterruptedException {
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            String text = res.body() == null ? "" : res.body();
            throw new IOException("AI provider returned HTTP " + res.statusCode() + ": "
                    + text.substring(0, Math.min(300, text.length())));
        }
        return JsonParser.parseString(res.body()).getAsJsonObject();
    }

    private static JsonObject msg(String role, String content) {
        JsonObject o = new JsonObject();
        o.addProperty("role", role);
        o.addProperty("content", content);
        return o;
    }

    private static JsonObject geminiPart(String role, String text) {
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject o = new JsonObject();
        o.addProperty("role", role);
        o.add("parts", parts);
        return o;
    }
}
