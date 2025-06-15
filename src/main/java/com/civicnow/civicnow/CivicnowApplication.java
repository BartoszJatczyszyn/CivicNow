package com.civicnow.civicnow;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CivicnowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CivicnowApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(ArticleImporterService articleImporterService) {
        return args -> {
            // Import local articles based on cities in the database.
            int targetLocalArticles = 200;
            articleImporterService.importAndAnalyzeLocalArticles(targetLocalArticles);

            // Import global articles based on a predefined list of topics.
            List<String> generalTopics = List.of("Health", "Technology", "Science", "Business");
            int targetGlobalArticles = 80;
            articleImporterService.importAndAnalyzeGlobalArticles(generalTopics, targetGlobalArticles);
        };
    }
}