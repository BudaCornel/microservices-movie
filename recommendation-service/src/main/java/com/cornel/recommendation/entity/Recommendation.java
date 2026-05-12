package com.cornel.recommendation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private String movieId;

    @Column(name = "recommended_id", nullable = false)
    private String recommendedId;

    @Column(nullable = false)
    private double score;

    protected Recommendation() {}

    public Recommendation(String movieId, String recommendedId, double score) {
        this.movieId = movieId;
        this.recommendedId = recommendedId;
        this.score = score;
    }

    public Long getId() { return id; }
    public String getMovieId() { return movieId; }
    public String getRecommendedId() { return recommendedId; }
    public double getScore() { return score; }
}