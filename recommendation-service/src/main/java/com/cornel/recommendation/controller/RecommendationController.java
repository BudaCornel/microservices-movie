package com.cornel.recommendation.controller;

import com.cornel.recommendation.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/{movieId}")
    public List<String> getRecommendations(@PathVariable String movieId) {
        return service.getRecommendedMovieIds(movieId);
    }
}