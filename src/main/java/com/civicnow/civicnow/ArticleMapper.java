package com.civicnow.civicnow;

import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {

    /**
     * Converts a NewsApiResponse DTO to an Article entity.
     * @param newsApiArticle The article DTO from the external NewsAPI.
     * @return A new Article entity ready to be saved.
     */
    public Article toEntity(NewsApiResponse.Article newsApiArticle) {
        if (newsApiArticle == null) {
            return null;
        }

        String sourceName = (newsApiArticle.getSource() != null) ? newsApiArticle.getSource().getName() : null;

        return Article.builder()
                .url(newsApiArticle.getUrl())
                .title(newsApiArticle.getTitle())
                .description(newsApiArticle.getDescription())
                .author(newsApiArticle.getAuthor())
                .sourceName(sourceName)
                .urlToImage(newsApiArticle.getUrlToImage())
                .publishedAt(newsApiArticle.getPublishedAt())
                .build();
    }
}