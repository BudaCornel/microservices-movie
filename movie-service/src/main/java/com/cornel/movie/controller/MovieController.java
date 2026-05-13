package com.cornel.movie.controller;

import com.cornel.movie.entity.Movie;
import com.cornel.movie.service.MovieService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Movie getMovie(@PathVariable String id) {
        return service.getMovie(id);
    }
}