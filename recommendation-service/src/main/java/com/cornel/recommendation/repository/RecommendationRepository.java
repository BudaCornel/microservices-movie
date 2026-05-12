package com.cornel.recommendation.repository;

import com.cornel.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByMovieIdOrderByScoreDesc(String movieId);
}