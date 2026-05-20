package com.cornel.movie.breaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    public enum EventType { SUCCESS, ERROR, NOT_PERMITTED, STATE_TRANSITION }

    public record Event(Instant timestamp, EventType type, String detail) {}

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);


    private final String name;
    private final int slidingWindowSize;
    private final int minimumNumberOfCalls;
    private final double failureRateThreshold;
    private final Duration waitDurationInOpenState;
    private final int permittedCallsInHalfOpen;

    private final ReentrantLock lock = new ReentrantLock();
    private State state = State.CLOSED;
    private final Deque<Boolean> window = new ArrayDeque<>();
    private Instant openedAt;
    private int halfOpenPermitsGranted;
    private int halfOpenSuccesses;
    private int halfOpenFailures;

    private static final int EVENT_CAPACITY = 200;
    private final Deque<Event> events = new ArrayDeque<>();

    private final ScheduledExecutorService scheduler;

    public CircuitBreaker(String name,
                          int slidingWindowSize,
                          int minimumNumberOfCalls,
                          double failureRateThreshold,
                          Duration waitDurationInOpenState,
                          int permittedCallsInHalfOpen) {
        this.name = name;
        this.slidingWindowSize = slidingWindowSize;
        this.minimumNumberOfCalls = minimumNumberOfCalls;
        this.failureRateThreshold = failureRateThreshold;
        this.waitDurationInOpenState = waitDurationInOpenState;
        this.permittedCallsInHalfOpen = permittedCallsInHalfOpen;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "breaker-" + name);
            t.setDaemon(true);
            return t;
        });
    }


    public <T> T execute(Supplier<T> call, Supplier<T> fallback) {
        if (!tryAcquirePermission()) {
            recordEvent(EventType.NOT_PERMITTED, null);
            return fallback.get();
        }
        try {
            T result = call.get();
            recordSuccess();
            return result;
        } catch (RuntimeException e) {
            recordFailure(e);
            return fallback.get();
        }
    }

    private boolean tryAcquirePermission() {
        lock.lock();
        try {
            if (state == State.OPEN
                    && Duration.between(openedAt, Instant.now()).compareTo(waitDurationInOpenState) >= 0) {
                transitionTo(State.HALF_OPEN);
            }

            return switch (state) {
                case CLOSED -> true;
                case OPEN -> false;
                case HALF_OPEN -> {
                    if (halfOpenPermitsGranted < permittedCallsInHalfOpen) {
                        halfOpenPermitsGranted++;
                        yield true;
                    }
                    yield false;
                }
            };
        } finally {
            lock.unlock();
        }
    }

    private void recordSuccess() {
        lock.lock();
        try {
            recordEvent(EventType.SUCCESS, null);
            if (state == State.HALF_OPEN) {
                halfOpenSuccesses++;
                evaluateHalfOpen();
            } else if (state == State.CLOSED) {
                addToWindow(true);
                evaluateClosed();
            }
        } finally {
            lock.unlock();
        }
    }

    private void recordFailure(Throwable cause) {
        lock.lock();
        try {
            recordEvent(EventType.ERROR, cause.getClass().getSimpleName() + ": " + cause.getMessage());
            if (state == State.HALF_OPEN) {
                halfOpenFailures++;
                evaluateHalfOpen();
            } else if (state == State.CLOSED) {
                addToWindow(false);
                evaluateClosed();
            }
        } finally {
            lock.unlock();
        }
    }

    private void addToWindow(boolean success) {
        if (window.size() >= slidingWindowSize) window.removeFirst();
        window.addLast(success);
    }

    private void evaluateClosed() {
        if (window.size() < minimumNumberOfCalls) return;
        long failures = window.stream().filter(b -> !b).count();
        double rate = 100.0 * failures / window.size();
        if (rate >= failureRateThreshold) {
            transitionTo(State.OPEN);
        }
    }

    private void evaluateHalfOpen() {
        int total = halfOpenSuccesses + halfOpenFailures;
        if (total < permittedCallsInHalfOpen) return;
        double rate = 100.0 * halfOpenFailures / total;
        if (rate >= failureRateThreshold) {
            transitionTo(State.OPEN);
        } else {
            transitionTo(State.CLOSED);
        }
    }

    private void transitionTo(State next) {
        State prev = state;
        if (prev == next) return;
        String detail = prev + "_TO_" + next;
        log.info("[CB:{}] STATE_TRANSITION {}", name, detail);
        recordEvent(EventType.STATE_TRANSITION, detail);
        state = next;

        switch (next) {
            case OPEN -> {
                openedAt = Instant.now();
                halfOpenPermitsGranted = 0;
                halfOpenSuccesses = 0;
                halfOpenFailures = 0;
                scheduler.schedule(this::attemptTransitionToHalfOpen,
                        waitDurationInOpenState.toMillis(), TimeUnit.MILLISECONDS);
            }
            case HALF_OPEN -> {
                halfOpenPermitsGranted = 0;
                halfOpenSuccesses = 0;
                halfOpenFailures = 0;
            }
            case CLOSED -> {
                window.clear();
                halfOpenPermitsGranted = 0;
                halfOpenSuccesses = 0;
                halfOpenFailures = 0;
            }
        }
    }

    private void recordEvent(EventType type, String detail) {
        if (events.size() >= EVENT_CAPACITY) events.removeFirst();
        events.addLast(new Event(Instant.now(), type, detail));
    }


    public String getName() { return name; }

    public State getState() {
        lock.lock();
        try { return state; } finally { lock.unlock(); }
    }

    public int getBufferedCalls() {
        lock.lock();
        try { return window.size(); } finally { lock.unlock(); }
    }

    public long getFailedCalls() {
        lock.lock();
        try { return window.stream().filter(b -> !b).count(); } finally { lock.unlock(); }
    }

    public double getFailureRate() {
        lock.lock();
        try {
            if (window.isEmpty()) return 0.0;
            return 100.0 * window.stream().filter(b -> !b).count() / window.size();
        } finally {
            lock.unlock();
        }
    }

    public List<Event> getEvents() {
        lock.lock();
        try { return new ArrayList<>(events); } finally { lock.unlock(); }
    }

    private void attemptTransitionToHalfOpen() {
        lock.lock();
        try {
            if (state == State.OPEN) {
                transitionTo(State.HALF_OPEN);
            }
        } finally {
            lock.unlock();
        }
    }
}