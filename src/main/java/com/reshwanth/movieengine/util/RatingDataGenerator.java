package com.reshwanth.movieengine.util;

import com.reshwanth.movieengine.domain.MovieRating;

import java.time.Instant;
import java.util.List;

public class RatingDataGenerator {

    public static MovieRating rating(int movieId, int score) {
        return new MovieRating(movieId, score, Instant.now().minusSeconds((long) (Math.random() * 1_000_000)));
    }

    public static List<MovieRating> ratingsFor(int... movieIdAndScorePairs) {
        // Example usage: ratingsFor(1,5, 2,4, 3,3)
        return java.util.stream.IntStream.range(0, movieIdAndScorePairs.length / 2)
                .mapToObj(i -> {
                    int movieId = movieIdAndScorePairs[i * 2];
                    int score = movieIdAndScorePairs[i * 2 + 1];
                    return rating(movieId, score);
                })
                .toList();
    }
}
