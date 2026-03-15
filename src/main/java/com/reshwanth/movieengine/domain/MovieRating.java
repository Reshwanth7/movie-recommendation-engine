package com.reshwanth.movieengine.domain;

import java.time.Instant;

public record MovieRating(long movieId, double rating, Instant addedTime) {
    public MovieRating{
        if(rating<1.0 || rating > 5.0)
        {
            throw new IllegalArgumentException("Invalid Rating Details Provided value should be from 1 to 6");
        }
    }
}
