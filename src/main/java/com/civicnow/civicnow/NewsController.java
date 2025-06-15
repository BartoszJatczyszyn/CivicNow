package com.civicnow.civicnow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class NewsController {

    private final ArticleRepository articleRepository;
    private final CityRepository cityRepository;

    @Autowired
    public NewsController(ArticleRepository articleRepository, CityRepository cityRepository) {
        this.articleRepository = articleRepository;
        this.cityRepository = cityRepository;
    }

    @GetMapping("/news/city/{cityName}")
    public List<Article> getNewsForCity(@PathVariable String cityName) {
        return articleRepository.findByIsLocalTrueAndCityNameIgnoreCase(cityName);
    }

    @GetMapping("/news/global")
    public List<Article> getGlobalNews() {
        return articleRepository.findByIsLocalFalse();
    }

    @GetMapping("/cities/search")
    public List<City> searchCities(@RequestParam String query) {
        return cityRepository.findByNameContainingIgnoreCase(query);
    }

    @GetMapping("/cities")
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
}