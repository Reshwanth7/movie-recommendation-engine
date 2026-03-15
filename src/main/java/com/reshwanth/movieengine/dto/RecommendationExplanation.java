package com.reshwanth.movieengine.dto;

public record RecommendationExplanation(
        long movieId,
        String title,
        double finalScore,
        double genreScore,
        double actorScore,
        double ratingSimilarityScore,
        double popularityScore,
        String explanationText
) {}
