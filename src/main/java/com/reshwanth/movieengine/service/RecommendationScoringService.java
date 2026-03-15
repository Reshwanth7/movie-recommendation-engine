package com.reshwanth.movieengine.service;

import com.reshwanth.movieengine.domain.Movie;
import com.reshwanth.movieengine.domain.MovieRating;
import com.reshwanth.movieengine.domain.User;
import com.reshwanth.movieengine.dto.RecommendationDTO;
import com.reshwanth.movieengine.dto.RecommendationExplanation;
import com.reshwanth.movieengine.util.MovieDataGenerator;

import java.time.Year;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RecommendationScoringService {
    public double computeGenreScore(User user, Movie movie){
            List<String> movieGenres = movie.genres();
            List<String> userPreferredGenres = user.preferredGenres();
            List<String> userUnpreferredGenres = user.dislikedGenres();
           double rawScore =  Optional.ofNullable(movieGenres)
                    .orElseGet(Collections::emptyList)
                    .stream()
                   .mapToDouble(mg-> {
                       if(userPreferredGenres != null && userPreferredGenres.contains(mg))
                           return 10.0;
                       if(userUnpreferredGenres != null && userUnpreferredGenres.contains(mg))
                           return -5.0;
                       return 0.0;
                   })
                    .sum();

           //Normalizing
        double maxScore = userPreferredGenres != null ? userPreferredGenres.size() * 10 : 0.0;
        double minScore = userUnpreferredGenres != null ? userUnpreferredGenres.size() * -5.0 : 0.0;

        if (maxScore == 0 && minScore == 0) return 0;
        return normalize(rawScore,minScore,maxScore);

    }

    public double  computeActorScore(User user, Movie movie)  {
        List<String> movieCast= movie.cast();
        List<String> userFavActors = user.favoriteActors();
       double rawScore =  Optional.ofNullable(movieCast)
                .orElseGet(Collections::emptyList)
                .stream()
                .mapToDouble(mg-> {
                    if(userFavActors != null && userFavActors.contains(mg))
                        return 15.0;
                    return 0.0;
                })
                .sum();

        double maxScore = userFavActors != null ? userFavActors.size() * 15 : 0.0;
        if(maxScore == 0 ) return 0;
        return normalize(rawScore,0,maxScore);
    }

    public double computeRatingSimilarityScore(User user, Movie movie){
        List<MovieRating> userRatedMovies = user.watchHistory();
       double weightedDifference =  Optional.ofNullable(userRatedMovies)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(urm -> urm != null && urm.movieId() == movie.movieId())
                .mapToDouble(urm -> Math.abs(urm.rating() - movie.rating()))
                .findFirst()
                .orElse(4.0);
        double similarity = Math.round(((1.0 - (weightedDifference/4.0)) * 100)/100);
        return similarity * 100;
    }

    public double computePopularityScore(Movie movie){
        double ratingScore = normalize(movie.rating(), 1.0, 5.0);

        int ratingCount = 100 + (int)(Math.random() * 900);
        double ratingCountScore = normalize(ratingCount, 100, 1000);

        int currentYear = Year.now().getValue();
        int age = currentYear - movie.releaseYear();
        double recencyScore = normalize(100 - age, 0, 100); // newer = higher

        return
                ratingScore * 0.5 +
                        ratingCountScore * 0.3 +
                        recencyScore * 0.2;


    }

    private double normalize(double value, double min, double max) {
        if (max == min) return 0;
        return ((value - min) / (max - min)) * 100.0;
    }

    public double finalRecommendationScore(User user, Movie movie) {
        if(user == null || movie == null)
        {
            return 0.0;
        }
        double genreScore = computeGenreScore(user,movie);
        double actorScore = computeActorScore(user,movie);
        double ratingSimilarity = computeRatingSimilarityScore(user,movie);
        double popularityScore = computePopularityScore(movie);

        return (genreScore * 0.35) + (actorScore * 0.25) + (ratingSimilarity * 0.20) + (popularityScore * 0.20);
    }

    public  List<RecommendationDTO> getRecommendations(User user)
    {
        if (user == null)
            return  Collections.emptyList();
        return Optional.of(MovieDataGenerator.getAllMovies())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(movie -> {
                    MovieSanitizer ms = new MovieSanitizer();
                    return ms.sanitize(movie);
                })
                .map(movie -> {
                    double genreScore = computeGenreScore(user,movie);
                    double actorScore = computeActorScore(user,movie);
                    double ratingSimilarity = computeRatingSimilarityScore(user,movie);
                    double popularityScore = computePopularityScore(movie);
                    double finalScore = (genreScore * 0.35) + (actorScore * 0.25) + (ratingSimilarity * 0.20) + (popularityScore * 0.20);
                    return new RecommendationDTO(movie.movieId(),movie.title(),finalScore,genreScore,actorScore,ratingSimilarity,popularityScore);

                })
                .sorted(Comparator.comparingDouble(RecommendationDTO::finalScore).reversed())
                .toList();

    }

    public  List<RecommendationDTO> getFirstNRecommendations(User user,int noOfRecommendations){
        int totalMovies = MovieDataGenerator.getAllMovies().size();
        if(noOfRecommendations == 0)
            return  Collections.emptyList();
        if(noOfRecommendations>totalMovies)
            noOfRecommendations = totalMovies;
        List<RecommendationDTO> recommendedList = getRecommendations(user);
        return Optional.ofNullable(recommendedList)
                .orElseGet(Collections::emptyList)
                .stream()
                .limit(noOfRecommendations)
                .toList();

    }

    public RecommendationExplanation explainRecommendation(RecommendationDTO dto) {

        if (dto == null) {
            return new RecommendationExplanation(
                    -1,
                    "Unknown",
                    0,
                    0,
                    0,
                    0,
                    0,
                    "No explanation available because the recommendation data was null."
            );
        }

        String explanation = String.format(
                """
                Recommendation Breakdown for '%s':
                
                • Final Score: %.2f
                
                • Genre Match Score: %.2f
                  Indicates how closely the movie's genres align with the user's preferred and disliked genres.
                
                • Actor Match Score: %.2f
                  Measures how many of the user's favorite actors appear in the movie.
                
                • Rating Similarity Score: %.2f
                  Shows how close the movie's global rating is to the user's personal rating history.
                
                • Popularity Score: %.2f
                  Reflects global popularity based on rating, rating count, and recency.
                """,
                dto.title(),
                dto.finalScore(),
                dto.genreScore(),
                dto.actorScore(),
                dto.ratingSimilarityScore(),
                dto.popularityScore()
        );

        return new RecommendationExplanation(
                dto.movieId(),
                dto.title(),
                dto.finalScore(),
                dto.genreScore(),
                dto.actorScore(),
                dto.ratingSimilarityScore(),
                dto.popularityScore(),
                explanation
        );
    }

}
