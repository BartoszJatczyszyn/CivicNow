package com.civicnow.civicnow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsApiResponse {
    private String status;
    private int totalResults;
    private List<Article> articles;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Article {
        private Source source;
        private String author;
        private String title;
        private String description;
        private String url;
        private String urlToImage;
        private OffsetDateTime publishedAt;
        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String id;
        private String name;
    }
}