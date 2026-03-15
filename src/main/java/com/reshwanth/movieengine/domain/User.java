package com.reshwanth.movieengine.domain;

import java.util.List;

public record User(long userId, List<String> preferredGenres, List<String> dislikedGenres, List<String> favoriteActors,
                   List<MovieRating> watchHistory)  {
}
