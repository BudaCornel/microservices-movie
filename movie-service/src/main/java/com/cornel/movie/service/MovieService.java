package com.cornel.movie.service;

import com.cornel.movie.dto.MovieResponse;
import com.cornel.movie.entity.Movie;
import com.cornel.movie.repository.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MovieService {

    private static final List<String> TRENDING_FALLBACK =
            List.of("m1", "m2", "m3", "m4", "m5");

    private final MovieRepository repository;
    private final RecommendationClient recommendationClient;

    public MovieService(MovieRepository repository, RecommendationClient recommendationClient) {
        this.repository = repository;
        this.recommendationClient = recommendationClient;
    }

    public MovieResponse getMovieWithRecommendations(String id) {
        Movie movie = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found: " + id));

        List<String> recs = recommendationClient.getRecommendations(id);
        String source = recs.equals(TRENDING_FALLBACK) ? "fallback-trending" : "service-b";

        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getDescription(), recs, source);
    }
}
