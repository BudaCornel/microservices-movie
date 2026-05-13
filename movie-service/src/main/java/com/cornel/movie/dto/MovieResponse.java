package com.cornel.movie.dto;

import java.util.List;

public record MovieResponse(
        String id,
        String title,
        String description,
        List<String> similarMovies,
        String recommendationSource
) {}
