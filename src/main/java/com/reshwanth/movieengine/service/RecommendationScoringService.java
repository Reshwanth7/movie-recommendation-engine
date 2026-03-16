package com.reshwanth.movieengine.service;

import com.reshwanth.movieengine.config.CustomThreadPool;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;


public class RecommendationScoringService {

    private final ConcurrentHashMap<Long, Double> popularityCache = new ConcurrentHashMap<>();

    public double computePopularityScoreCached(Movie movie) {

        if (movie == null) return 0.0;

        return popularityCache.computeIfAbsent(movie.movieId(), id ->
                computePopularityScore(movie)  // your existing scoring logic
        );
    }

    public int getPopularityCacheSize() {
        return popularityCache.size();
    }

    public final Cache<String, Double> genreCache = new InMemoryCache<>();
    private final Cache<String, Double> actorCache = new InMemoryCache<>();
    private final Cache<String, Double> ratingCache = new InMemoryCache<>();

    public double computeGenreScoreCached(User user, Movie movie) {
        String key = user.userId() + ":" + movie.movieId() + ":genre";

        if (genreCache.contains(key)) {
            return genreCache.get(key);
        }

        double score = computeGenreScore(user, movie);
        genreCache.put(key, score);
        return score;
    }

    public double computeActorScoreCached(User user, Movie movie) {
        String key = user.userId() + ":" + movie.movieId() + ":actor";

        if (actorCache.contains(key)) {
            return actorCache.get(key);
        }

        double score = computeActorScore(user, movie);
        actorCache.put(key, score);
        return score;
    }

    public double computeRatingSimilarityScoreCached(User user, Movie movie) {
        String key = user.userId() + ":" + movie.movieId() + ":rating";

        if (ratingCache.contains(key)) {
            return ratingCache.get(key);
        }

        double score = computeRatingSimilarityScore(user, movie);
        ratingCache.put(key, score);
        return score;
    }




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
        double genreScore = computeGenreScoreCached(user,movie);
        double actorScore = computeActorScoreCached(user,movie);
        double ratingSimilarity = computeRatingSimilarityScoreCached(user,movie);
        double popularityScore = computePopularityScoreCached(movie);

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
                    double genreScore = computeGenreScoreCached(user,movie);
                    double actorScore = computeActorScoreCached(user,movie);
                    double ratingSimilarity = computeRatingSimilarityScoreCached(user,movie);
                    double popularityScore = computePopularityScoreCached(movie);
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

    public List<RecommendationDTO> getRecommendationsParallel(User user) {

        if (user == null) {
            return Collections.emptyList();
        }

            return CustomThreadPool.PARALLEL_POOL.submit(() ->
                    MovieDataGenerator.getAllMovies()
                            .parallelStream()
                            .map(movie -> {
                                MovieSanitizer ms = new MovieSanitizer();
                                return ms.sanitize(movie);
                            })
                            .map(movie -> {
                                double genre = computeGenreScoreCached(user, movie);
                                double actor = computeActorScoreCached(user, movie);
                                double ratingSim = computeRatingSimilarityScoreCached(user, movie);
                                double popularity = computePopularityScoreCached(movie);
                                double finalScore = finalRecommendationScore(user, movie);

                                return new RecommendationDTO(
                                        movie.movieId(),
                                        movie.title(),
                                        finalScore,
                                        genre,
                                        actor,
                                        ratingSim,
                                        popularity
                                );
                            })
                            .sorted(Comparator.comparingDouble(RecommendationDTO::finalScore).reversed())
                            .toList()
            ).join();

    }

    public CompletableFuture<Double> computeGenreScoreAsync(User user, Movie movie) {
        return CompletableFuture.supplyAsync(() -> computeGenreScoreCached(user, movie), CustomThreadPool.EXECUTOR_POOL);
    }

    public CompletableFuture<Double> computeActorScoreAsync(User user, Movie movie) {
        return CompletableFuture.supplyAsync(() -> computeActorScoreCached(user, movie), CustomThreadPool.EXECUTOR_POOL);
    }

    public CompletableFuture<Double> computeRatingSimilarityScoreAsync(User user, Movie movie) {
        return CompletableFuture.supplyAsync(() -> computeRatingSimilarityScoreCached(user, movie), CustomThreadPool.EXECUTOR_POOL);
    }

    public CompletableFuture<Double> computePopularityScoreAsync(Movie movie) {
        return CompletableFuture.supplyAsync(() -> computePopularityScoreCached(movie), CustomThreadPool.EXECUTOR_POOL);
    }

    public CompletableFuture<Double> computeFinalScoreAsync(
            CompletableFuture<Double> genreFuture,
            CompletableFuture<Double> actorFuture,
            CompletableFuture<Double> ratingFuture,
            CompletableFuture<Double> popularityFuture
    ) {
        return genreFuture
                .thenCombine(actorFuture, (genre, actor) -> genre * 0.35 + actor * 0.25)
                .thenCombine(ratingFuture, (partial, rating) -> partial + rating * 0.20)
                .thenCombine(popularityFuture, (partial, pop) -> partial + pop * 0.20);
    }

    public CompletableFuture<RecommendationDTO> computeRecommendationAsync(User user, Movie movie) {

        CompletableFuture<Double> genreFuture = computeGenreScoreAsync(user, movie);
        CompletableFuture<Double> actorFuture = computeActorScoreAsync(user, movie);
        CompletableFuture<Double> ratingFuture = computeRatingSimilarityScoreAsync(user, movie);
        CompletableFuture<Double> popularityFuture = computePopularityScoreAsync(movie);

        CompletableFuture<Double> finalFuture =
                computeFinalScoreAsync(genreFuture, actorFuture, ratingFuture, popularityFuture);

        return finalFuture.thenApply(finalScore ->
                new RecommendationDTO(
                        movie.movieId(),
                        movie.title(),
                        finalScore,
                        genreFuture.join(),
                        actorFuture.join(),
                        ratingFuture.join(),
                        popularityFuture.join()
                )
        );
    }

    public CompletableFuture<List<RecommendationDTO>> getRecommendationsAsync(User user) {

        if (user == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<Movie> movies = MovieDataGenerator.getAllMovies();
        if (movies == null || movies.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // Step 1: Create async tasks for each movie
        List<CompletableFuture<RecommendationDTO>> futures = movies.stream()
                .map(movie -> computeRecommendationAsync(user, movie))
                .toList();

        // Step 2: Combine all futures
        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Step 3: Convert to CompletableFuture<List<DTO>>
        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .sorted(Comparator.comparingDouble(RecommendationDTO::finalScore).reversed())
                        .toList()
        );
    }

    public CompletableFuture<List<RecommendationDTO>> getRecommendationsHybrid(User user) {

        if (user == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<Movie> movies = MovieDataGenerator.getAllMovies();
        if (movies == null || movies.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

            // Step 1: Submit parallel work to custom pool
            return CustomThreadPool.PARALLEL_POOL.submit(() -> {

                // Step 2: For each movie, start async scoring
                List<CompletableFuture<RecommendationDTO>> futures = movies
                        .parallelStream()
                        .map(movie -> computeRecommendationAsync(user, movie))
                        .toList();

                // Step 3: Combine all async tasks
                CompletableFuture<Void> allDone =
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

                // Step 4: Build final sorted list
                return allDone.thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .sorted(Comparator.comparingDouble(RecommendationDTO::finalScore).reversed())
                                .toList()
                );

            }).join(); // join the outer ForkJoinPool task


    }


}
