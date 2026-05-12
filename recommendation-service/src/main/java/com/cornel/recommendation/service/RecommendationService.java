package com.cornel.recommendation.service;

import com.cornel.recommendation.entity.Recommendation;
import com.cornel.recommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository repository;

    public RecommendationService(RecommendationRepository repository) {
        this.repository = repository;
    }

    public List<String> getRecommendedMovieIds(String movieId) {
        return repository.findByMovieIdOrderByScoreDesc(movieId)
                .stream()
                .map(Recommendation::getRecommendedId)
                .toList();
    }
}