package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.core.Name;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
// For JSON parsing, you'd typically use a library like Jackson or Gson.
// For this example, we'll do very simple manual JSON parsing for the response.
// In a real app, use a library!
// import com.fasterxml.jackson.databind.ObjectMapper; // Example for Jackson

public class AiNameComparator implements NameComparator {

    private final HttpClient httpClient;
    private final String apiServerUrl;
    // private final ObjectMapper objectMapper; // If using Jackson

    public AiNameComparator(String serverUrl) {
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            this.apiServerUrl = "http://127.0.0.1:5000/similarity"; // Default
            System.out.println("AiNameComparator: serverUrl not provided, using default: " + this.apiServerUrl);
        } else {
            this.apiServerUrl = serverUrl.trim();
        }

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5)) // Timeout for establishing connection
                .build();
        // this.objectMapper = new ObjectMapper(); // If using Jackson
    }

    @Override
    public double calculateScore(Name name1, Name name2) {
        if (name1 == null || name2 == null) {
            return 0.0; // Or handle as per your project's error conventions
        }

        // For this AI cross-encoder, using the original names is usually best
        // as it's trained on natural language. Preprocessed tokens might be less effective.
        String originalName1 = name1.originalName();
        String originalName2 = name2.originalName();

        if (originalName1 == null || originalName1.isEmpty() || originalName2 == null || originalName2.isEmpty()) {
            return 0.0;
        }

        // Construct JSON payload manually (for simplicity without external library)
        // In a real app, use a JSON library like Jackson or Gson.
        String jsonPayload = String.format("{\"name1\": \"%s\", \"name2\": \"%s\"}",
                escapeJsonString(originalName1), escapeJsonString(originalName2));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiServerUrl))
                .timeout(Duration.ofSeconds(10)) // Timeout for the entire request-response
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                // Very basic manual JSON parsing (highly error-prone, use a library in production)
                if (responseBody.contains("\"similarity_score\"")) {
                    String scoreStr = responseBody.split(":")[1].replace("}", "").trim();
                    return Double.parseDouble(scoreStr);
                } else {
                    System.err.println("AiNameComparator: Unexpected JSON response: " + responseBody);
                    return 0.0; // Default score on parsing error
                }
            } else {
                System.err.println("AiNameComparator: API request failed with status code " + response.statusCode() +
                        " and body: " + response.body());
                return 0.0; // Default score on API error
            }
        } catch (HttpTimeoutException e) {
            System.err.println("AiNameComparator: API request timed out: " + e.getMessage());
            return 0.0;
        } catch (IOException | InterruptedException e) {
            System.err.println("AiNameComparator: Error sending API request or thread interrupted: " + e.getMessage());
            // e.printStackTrace(); // For debugging
            return 0.0; // Default score on network/other errors
        } catch (NumberFormatException e) {
            System.err.println("AiNameComparator: Error parsing score from API response: " + e.getMessage());
            return 0.0;
        }
    }

    // Helper to escape strings for JSON (very basic, not exhaustive)
    private String escapeJsonString(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    @Override
    public boolean isScoreDistance() {
        // The cross-encoder (sigmoid applied) returns a similarity score (0 to 1)
        return false;
    }

    @Override
    public String getName() {
        return "AI_CROSS_ENCODER_STSB"; // Identifier for StrategyFactory
    }
}