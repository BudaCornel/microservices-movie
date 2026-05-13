package com.cornel.movie.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;


@Service
public class RecommendationClient {

    private static final Logger log = LoggerFactory.getLogger(RecommendationClient.class);

    private static final List<String> TRENDING_FALLBACK =
            List.of("m1", "m2", "m3", "m4", "m5");

    private final RestClient client;

    public RecommendationClient(RestClient recommendationRestClient) {
        this.client = recommendationRestClient;
    }

    @CircuitBreaker(name = "recommendationService", fallbackMethod = "fallbackRecommendations")
    public List<String> getRecommendations(String movieId) {
        log.info("Calling Service B for movie {}", movieId);
        return client.get()
                .uri("/api/recommendations/{id}", movieId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {});
    }

    @SuppressWarnings("unused")
    public List<String> fallbackRecommendations(String movieId, Throwable t) {
        log.warn("Falling back to trending movies for {} (cause: {} - {})",
                movieId, t.getClass().getSimpleName(), t.getMessage());
        return TRENDING_FALLBACK;
    }
}
