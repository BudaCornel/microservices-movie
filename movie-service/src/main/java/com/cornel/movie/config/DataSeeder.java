package com.cornel.movie.config;

import com.cornel.movie.entity.Movie;
import com.cornel.movie.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedMovies(MovieRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.saveAll(List.of(
                    new Movie("m1", "The Fellowship of the Ring",
                            "A hobbit and a fellowship set out to destroy a powerful ring."),
                    new Movie("m2", "The Two Towers",
                            "The fellowship is broken; war comes to Middle-earth."),
                    new Movie("m3", "The Return of the King",
                            "The final stand against Sauron and the return of the rightful king."),
                    new Movie("m4", "The Hobbit: An Unexpected Journey",
                            "Bilbo Baggins joins thirteen dwarves to reclaim Erebor."),
                    new Movie("m5", "Dune",
                            "A noble family fights for control of the desert planet Arrakis.")
            ));
        };
    }
}