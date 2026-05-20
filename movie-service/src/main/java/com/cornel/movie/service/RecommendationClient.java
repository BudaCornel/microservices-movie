package com.cornel.movie.service;

import com.cornel.movie.breaker.CircuitBreaker;
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
    private final CircuitBreaker breaker;

    public RecommendationClient(RestClient recommendationRestClient, CircuitBreaker breaker) {
        this.client = recommendationRestClient;
        this.breaker = breaker;
    }

    public List<String> getRecommendations(String movieId) {
        return breaker.execute(
                () -> {
                    log.info("Calling Service B for movie {}", movieId);
                    return client.get()
                            .uri("/api/recommendations/{id}", movieId)
                            .retrieve()
                            .body(new ParameterizedTypeReference<List<String>>() {});
                },
                () -> {
                    log.warn("Falling back to trending movies for {}", movieId);
                    return TRENDING_FALLBACK;
                }
        );
    }
}