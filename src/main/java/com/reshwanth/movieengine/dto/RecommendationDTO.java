package com.reshwanth.movieengine.dto;

public record RecommendationDTO(
        long movieId,
        String title,
        double finalScore,
        double genreScore,
        double actorScore,
        double ratingSimilarityScore,
        double popularityScore
) {}

