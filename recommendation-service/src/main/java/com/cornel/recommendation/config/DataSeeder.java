package com.cornel.recommendation.config;

import com.cornel.recommendation.entity.Recommendation;
import com.cornel.recommendation.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seed(RecommendationRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            log.info("Seeding recommendations…");
            repo.saveAll(List.of(
                    new Recommendation("m1", "m2", 0.95),
                    new Recommendation("m1", "m3", 0.82),
                    new Recommendation("m1", "m4", 0.78),
                    new Recommendation("m2", "m1", 0.91),
                    new Recommendation("m2", "m5", 0.74),
                    new Recommendation("m3", "m1", 0.88),
                    new Recommendation("m3", "m2", 0.71),
                    new Recommendation("m4", "m1", 0.66),
                    new Recommendation("m5", "m2", 0.59)
            ));
        };
    }
}
