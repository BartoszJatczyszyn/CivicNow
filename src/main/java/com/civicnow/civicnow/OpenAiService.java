package com.civicnow.civicnow;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public record ArticleAnalysis(boolean isLocal, String name, String state) {}

    public ArticleAnalysis analyzeArticle(String articleTitle, String articleContent) throws IOException {
        String cleanedArticleContent = (articleContent != null && !articleContent.isEmpty()) ?
                articleContent.substring(0, Math.min(articleContent.length(), 2000)) : "";

        String prompt = String.format("""
            Analyze the following news article and determine if it is local news for a specific U.S. city.
            Respond ONLY with a JSON object with these fields:
            - "isLocal": boolean, true if it's local news, false otherwise.
            - "name": string, the name of the U.S. city if local, otherwise null.
            - "state": string, the 2-letter state abbreviation (e.g., "NY", "CA") if local, otherwise null.

            Title: %s
            Content: %s
            """, articleTitle, cleanedArticleContent);

        var requestBodyMap = Map.of(
            "model", "gpt-4o-mini",
            "messages", List.of(
                Map.of(
                    "role", "system",
                    "content", "You are a helpful assistant designed to output JSON that strictly adheres to the requested format. Identify only U.S. cities and states."
                ),
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            ),
            "response_format", Map.of("type", "json_object")
        );

        String requestBody = mapper.writeValueAsString(requestBodyMap);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = Objects.requireNonNull(response.body()).string();
                throw new IOException("Unexpected OpenAI API response code: " + response.code() + " Body: " + errorBody);
            }

            String responseBody = Objects.requireNonNull(response.body()).string();
            String jsonContent = mapper.readTree(responseBody)
                                       .get("choices").get(0)
                                       .get("message").get("content")
                                       .asText();

            return mapper.readValue(jsonContent, ArticleAnalysis.class);
        }
    }
}
