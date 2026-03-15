package com.reshwanth.movieengine.domain;

import java.util.List;

public record Movie(long movieId, String title, List<String> genres, int releaseYear, List<String> cast, double rating, List<String> tags) {
}

