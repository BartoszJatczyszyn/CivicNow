package com.civicnow.civicnow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class NewsApiService {

    private static final Logger log = LoggerFactory.getLogger(NewsApiService.class);

    @Value("${newsapi.everything-url}")
    private String everythingUrl;

    @Value("${newsapi.key}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper;

    public NewsApiService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // This method will be used for fetching articles for analysis
    public NewsApiResponse getEverythingGeneral(String query, Integer page, String sortBy) throws IOException {
        int pageSize = 20; // Max per page for free plan

        String url = String.format("%s?q=%s&pageSize=%d&sortBy=%s&apiKey=%s&page=%d",
                                   everythingUrl, query, pageSize, sortBy, apiKey, page);

        log.info("Calling News API (General) URL: {}", url.replace(apiKey, "YOUR_API_KEY"));

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = Objects.requireNonNull(response.body()).string();
                log.error("News API returned error (HTTP {}): {}", response.code(), errorBody);
                throw new IOException("Unexpected code " + response.code() + " for URL: " + url.replace(apiKey, "YOUR_API_KEY") + ". Response body: " + errorBody);
            }
            String jsonResponse = Objects.requireNonNull(response.body()).string();
            return objectMapper.readValue(jsonResponse, NewsApiResponse.class);
        }
    }
}