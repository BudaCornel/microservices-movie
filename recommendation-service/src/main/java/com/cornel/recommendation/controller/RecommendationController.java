package com.cornel.recommendation.controller;

import com.cornel.recommendation.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService service;
    private final boolean chaosMode;

    public RecommendationController(RecommendationService service,
                                    @Value("${chaos.mode:false}") boolean chaosMode) {
        this.service = service;
        this.chaosMode = chaosMode;
        log.info("RecommendationController started with chaosMode={}", chaosMode);
    }

    @GetMapping("/{movieId}")
    public List<String> getRecommendations(@PathVariable String movieId) throws InterruptedException {
        if (chaosMode) {
            applyChaos();
        }
        return service.getRecommendedMovieIds(movieId);
    }

    /**
     * Chaos behavior (only when CHAOS_MODE=true):
     *   30% -> HTTP 503 (partial failure)
     *   30% -> latency spike between 3 and 10 seconds (network jitter)
     *   40% -> respond normally
     */
    private void applyChaos() throws InterruptedException {
        int dice = ThreadLocalRandom.current().nextInt(100);
        if (dice < 30) {
            log.warn("[CHAOS] Returning HTTP 503");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Chaos: simulated service unavailable");
        }
        if (dice < 60) {
            long delayMs = ThreadLocalRandom.current().nextLong(3_000, 10_001);
            log.warn("[CHAOS] Injecting latency: {} ms", delayMs);
            Thread.sleep(delayMs);
        }
    }
}