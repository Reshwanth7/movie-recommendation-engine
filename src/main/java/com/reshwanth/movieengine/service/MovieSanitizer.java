package com.reshwanth.movieengine.service;

import com.reshwanth.movieengine.domain.Movie;

import java.util.List;

public class MovieSanitizer {

    public Movie sanitize(Movie movie) {
        if (movie == null) return null;

        List<String> genres = movie.genres() == null ? List.of() : movie.genres();
        List<String> cast = movie.cast() == null ? List.of() : movie.cast();
        List<String> tags = movie.tags() == null ? List.of() : movie.tags();

        return new Movie(
                movie.movieId(),
                movie.title(),
                genres,
                movie.releaseYear(),
                cast,
                movie.rating(),
                tags
        );
    }
}

