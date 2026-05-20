package com.cornel.movie.breaker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/breaker")
public class CircuitBreakerController {

    private final CircuitBreaker breaker;

    public CircuitBreakerController(CircuitBreaker breaker) {
        this.breaker = breaker;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        return Map.of(
                "name", breaker.getName(),
                "state", breaker.getState().toString(),
                "bufferedCalls", breaker.getBufferedCalls(),
                "failedCalls", breaker.getFailedCalls(),
                "failureRate", breaker.getFailureRate()
        );
    }

    @GetMapping("/events")
    public List<CircuitBreaker.Event> events() {
        return breaker.getEvents();
    }
}