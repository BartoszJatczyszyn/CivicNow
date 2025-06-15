package com.civicnow.civicnow;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ArticleImporterService {

    private static final Logger log = LoggerFactory.getLogger(ArticleImporterService.class);
    private static final int API_PAGE_SIZE = 20; // Default page size for NewsAPI.
    private static final int MAX_PAGES_PER_QUERY = 1; // Limit pages to fetch per city/topic.

    private final NewsApiService newsApiService;
    private final OpenAiService openAiService;
    private final ArticleRepository articleRepository;
    private final CityRepository cityRepository;
    private final ArticleMapper articleMapper;

    @Transactional
    public void importAndAnalyzeLocalArticles(int maxArticles) {
        List<City> cities = cityRepository.findAll();
        if (cities.isEmpty()) {
            log.warn("No cities found in the database. Skipping local article import.");
            return;
        }

        log.info("Starting local article import. Target: {} unique articles.", maxArticles);
        Stream<String> cityNames = cities.stream().map(City::getName);
        importArticles(cityNames, maxArticles, "city");
    }

    @Transactional
    public void importAndAnalyzeGlobalArticles(List<String> topics, int maxArticles) {
        if (topics == null || topics.isEmpty()) {
            log.warn("No global topics defined. Skipping global article import.");
            return;
        }

        log.info("Starting global article import for topics: {}. Target: {} unique articles.", topics, maxArticles);
        importArticles(topics.stream(), maxArticles, "topic");
    }

    private void importArticles(Stream<String> queries, int maxArticles, String queryType) {
        int importedCount = 0;
        int newsApiRequestCount = 0;
        int openAiRequestCount = 0;

        for (String query : (Iterable<String>) queries::iterator) {
            if (importedCount >= maxArticles) {
                log.info("Target of {} articles reached. Halting further imports.", maxArticles);
                break;
            }

            log.info("Fetching articles for {} '{}'", queryType, query);
            try {
                // Fetch a limited number of pages per query
                for (int page = 1; page <= MAX_PAGES_PER_QUERY; page++) {
                    NewsApiResponse response = newsApiService.getEverythingGeneral(query, page, "publishedAt");
                    newsApiRequestCount++;

                    if (response == null || response.getArticles() == null || response.getArticles().isEmpty()) {
                        log.warn("No articles returned for {} '{}' on page {}.", queryType, query, page);
                        break; // No more articles for this query
                    }

                    for (NewsApiResponse.Article newsApiArticle : response.getArticles()) {
                        if (importedCount >= maxArticles) break;

                        if (!articleRepository.existsByUrl(newsApiArticle.getUrl())) {
                            processArticle(newsApiArticle);
                            importedCount++;
                            openAiRequestCount++; // Assuming analysis is always attempted for new articles.
                        } else {
                            log.debug("Article with URL '{}' already exists. Skipping.", newsApiArticle.getUrl());
                        }
                    }

                    if (response.getArticles().size() < API_PAGE_SIZE) {
                        log.info("Last page of results reached for query '{}'.", query);
                        break; // No more pages available
                    }
                }
            } catch (IOException e) {
                log.error("Error fetching articles for {} '{}': {}", queryType, query, e.getMessage(), e);
            } catch (Exception e) {
                log.error("An unexpected error occurred while processing {} '{}': {}", queryType, query, e.getMessage(), e);
            }
        }
        log.info("Import process finished for {}. Total new articles: {}. NewsAPI calls: {}. OpenAI calls: {}.",
                queryType, importedCount, newsApiRequestCount, openAiRequestCount);
    }

    private void processArticle(NewsApiResponse.Article newsApiArticle) {
        Article article = articleMapper.toEntity(newsApiArticle);
        String contentToAnalyze = getContentForAnalysis(newsApiArticle);

        if (contentToAnalyze.isEmpty()) {
            log.warn("Article '{}' has no content for analysis. It will be marked as non-local.", article.getTitle());
            article.setIsLocal(false);
        } else {
            analyzeAndPopulateArticle(article, contentToAnalyze);
        }

        articleRepository.save(article);
        log.info("Saved new article: '{}'", article.getTitle());
        
        // Add a delay to respect API rate limits.
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted during API delay.", e);
        }
    }
    
    private void analyzeAndPopulateArticle(Article article, String contentToAnalyze) {
        try {
            OpenAiService.ArticleAnalysis analysis = openAiService.analyzeArticle(article.getTitle(), contentToAnalyze);
            article.setIsLocal(analysis.isLocal());
            article.setCityName(analysis.name());
            article.setStateAbbreviation(analysis.state());
            log.info("Analysis complete for '{}': isLocal={}, city={}, state={}",
                     article.getTitle(), analysis.isLocal(), analysis.name(), analysis.state());
        } catch (IOException e) {
            log.error("OpenAI analysis failed for article '{}'. Marking as non-local. Error: {}", 
                      article.getTitle(), e.getMessage());
            article.setIsLocal(false);
        }
    }

    private String getContentForAnalysis(NewsApiResponse.Article newsApiArticle) {
        if (newsApiArticle.getContent() != null && !newsApiArticle.getContent().isEmpty()) {
            return newsApiArticle.getContent();
        }
        if (newsApiArticle.getDescription() != null && !newsApiArticle.getDescription().isEmpty()) {
            return newsApiArticle.getDescription();
        }
        if (newsApiArticle.getTitle() != null && !newsApiArticle.getTitle().isEmpty()) {
            return newsApiArticle.getTitle();
        }
        return "";
    }
}