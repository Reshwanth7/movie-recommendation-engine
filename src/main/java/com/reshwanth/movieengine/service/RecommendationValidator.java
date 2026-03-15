package com.reshwanth.movieengine.service;

import com.reshwanth.movieengine.domain.Movie;
import com.reshwanth.movieengine.domain.User;
import com.reshwanth.movieengine.dto.RecommendationDTO;
import com.reshwanth.movieengine.util.MovieDataGenerator;

import java.util.Collections;
import java.util.List;

public class RecommendationValidator {

    private final RecommendationScoringService scoringService = new RecommendationScoringService();

    public boolean validateScoringConsistency(User user) {

        if (user == null) {
            return false;
        }

        List<Movie> movies = MovieDataGenerator.getAllMovies();
        if (movies.isEmpty()) {
            return true; // nothing to validate
        }

        for (Movie movie : movies) {

            double genre = scoringService.computeGenreScore(user, movie);
            double actor = scoringService.computeActorScore(user, movie);
            double ratingSim = scoringService.computeRatingSimilarityScore(user, movie);
            double popularity = scoringService.computePopularityScore(movie);
            double finalScore = scoringService.finalRecommendationScore(user, movie);

            if (isValidScore(genre) ||
                    isValidScore(actor) ||
                    isValidScore(ratingSim) ||
                    isValidScore(popularity) ||
                    isValidScore(finalScore)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidScore(double score) {
        return Double.isNaN(score) ||
                Double.isInfinite(score) ||
                !(score >= 0.0) ||
                !(score <= 100.0);
    }

    public List<RecommendationDTO> getRecommendationsForUserWithNoPreferences(User user) {

        if (user == null) {
            return Collections.emptyList();
        }

        // User has empty preference lists → normal scoring still works
        return scoringService.getRecommendations(user);
    }
}

