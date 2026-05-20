package com.cornel.movie.breaker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreaker recommendationBreaker(
            @Value("${breaker.sliding-window-size:10}") int slidingWindowSize,
            @Value("${breaker.minimum-calls:5}") int minimumCalls,
            @Value("${breaker.failure-rate-threshold:50}") double failureRateThreshold,
            @Value("${breaker.wait-duration:PT10S}") Duration waitDuration,
            @Value("${breaker.permitted-calls-in-half-open:3}") int permittedInHalfOpen) {

        return new CircuitBreaker(
                "recommendationService",
                slidingWindowSize,
                minimumCalls,
                failureRateThreshold,
                waitDuration,
                permittedInHalfOpen
        );
    }
}