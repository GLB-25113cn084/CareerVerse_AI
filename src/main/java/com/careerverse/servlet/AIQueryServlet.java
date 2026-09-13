package com.careerverse.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ai-query")
public class AIQueryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String OLLAMA_MODEL =
            "llama3.2";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject result = new JSONObject();

        try {

            // ---------------------------------------------------------
            // Read request body
            // ---------------------------------------------------------

            StringBuilder body = new StringBuilder();

            try (BufferedReader reader = request.getReader()) {

                String line;

                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            JSONObject input;

            if (body.toString().trim().isEmpty()) {
                input = new JSONObject();
            } else {
                input = new JSONObject(body.toString());
            }

            // ---------------------------------------------------------
            // Get query
            // ---------------------------------------------------------

            String query =
                    input.optString("query", "").trim();

            if (query.isEmpty()) {
                query =
                        input.optString("message", "").trim();
            }

            // ---------------------------------------------------------
            // Validate
            // ---------------------------------------------------------

            if (query.isEmpty()) {

                result.put("success", false);
                result.put(
                        "error",
                        "Please provide a query."
                );

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                response.getWriter().write(
                        result.toString()
                );

                return;
            }

            // ---------------------------------------------------------
            // Ask Ollama
            // ---------------------------------------------------------

            String answer = askOllama(query);

            // ---------------------------------------------------------
            // Success response
            // ---------------------------------------------------------

            result.put("success", true);
            result.put("answer", answer);

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    result.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            result.put("success", false);

            result.put(
                    "error",
                    "CareerVerse AI is currently unavailable. "
                    + "Please make sure Ollama is running."
            );

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    result.toString()
            );
        }
    }

    // =========================================================
    // Ollama API
    // =========================================================

    private String askOllama(String query)
            throws IOException {

        URL url = new URL(OLLAMA_URL);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setDoOutput(true);

        // ---------------------------------------------------------
        // Prompt
        // ---------------------------------------------------------

        String prompt =
                "You are CareerVerse AI, a helpful career "
                + "advisor for students.\n\n"
                + "Give clear, practical and encouraging "
                + "career guidance.\n"
                + "Keep answers suitable for students.\n\n"
                + "Student question:\n"
                + query;

        JSONObject requestBody =
                new JSONObject();

        requestBody.put(
                "model",
                OLLAMA_MODEL
        );

        requestBody.put(
                "prompt",
                prompt
        );

        requestBody.put(
                "stream",
                false
        );

        // ---------------------------------------------------------
        // Send request
        // ---------------------------------------------------------

        try (OutputStream output =
                     connection.getOutputStream()) {

            byte[] input =
                    requestBody.toString()
                            .getBytes(StandardCharsets.UTF_8);

            output.write(input);
        }

        // ---------------------------------------------------------
        // Check response
        // ---------------------------------------------------------

        int status =
                connection.getResponseCode();

        InputStream inputStream;

        if (status >= 200 && status < 300) {

            inputStream =
                    connection.getInputStream();

        } else {

            inputStream =
                    connection.getErrorStream();
        }

        StringBuilder responseText =
                new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                responseText.append(line);
            }
        }

        connection.disconnect();

        // ---------------------------------------------------------
        // Handle HTTP error
        // ---------------------------------------------------------

        if (status < 200 || status >= 300) {

            throw new IOException(
                    "Ollama returned HTTP "
                    + status
                    + ": "
                    + responseText
            );
        }

        // ---------------------------------------------------------
        // Parse Ollama response
        // ---------------------------------------------------------

        JSONObject ollamaResponse =
                new JSONObject(
                        responseText.toString()
                );

        return ollamaResponse.optString(
                "response",
                "Sorry, I could not generate a response."
        ).trim();
    }
}