package com.civicnow.civicnow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // Check for existence by URL is more efficient than fetching the whole entity.
    boolean existsByUrl(String url);

    List<Article> findByIsLocalTrueAndCityNameIgnoreCase(String cityName);

    List<Article> findByIsLocalFalse();
}