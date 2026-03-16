package com.reshwanth.movieengine.service;

import com.reshwanth.movieengine.domain.Movie;
import com.reshwanth.movieengine.domain.MovieRating;
import com.reshwanth.movieengine.domain.User;
import com.reshwanth.movieengine.dto.RecommendationDTO;
import com.reshwanth.movieengine.util.MovieDataGenerator;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationScoringServiceTest {

    RecommendationScoringService service = new RecommendationScoringService();

    Movie movie = new Movie(
            1,
            "Inception",
            List.of("Sci-Fi", "Thriller"),
            2010,
            List.of("Leonardo DiCaprio", "Tom Hardy"),
            4.8,
            List.of("mind-bending")
    );

    User user = new User(
            1,
            List.of("Sci-Fi", "Action"),
            List.of("Musical"),
            List.of("Leonardo DiCaprio"),
            List.of(new MovieRating(1, 5, java.time.Instant.now()))
    );

    @Test
    void testGenreScore_PerfectMatch() {
        double score = service.computeGenreScore(user, movie);
        assertEquals(60.0, score, 0.1);
    }

    @Test
    void testGenreScore_MixedPreferredAndNeutral() {
        Movie m = new Movie(2, "Test", List.of("Sci-Fi", "Drama"), 2020, List.of(), 4.0, List.of());
        double score = service.computeGenreScore(user, m);
        assertEquals(60.0, score, 0.1);
    }

    @Test
    void testGenreScore_DislikedGenre() {
        Movie m = new Movie(3, "Test", List.of("Musical"), 2020, List.of(), 4.0, List.of());
        double score = service.computeGenreScore(user, m);
        assertEquals(0.0, score, 0.1);
    }

    @Test
    void testGenreScore_NoPreferredOrDisliked() {
        User u = new User(2, List.of(), List.of(), List.of(), List.of());
        double score = service.computeGenreScore(u, movie);
        assertEquals(0.0, score, 0.1);
    }
    @Test
    void testActorScore_PerfectMatch() {
        double score = service.computeActorScore(user, movie);
        assertEquals(100.0, score, 0.1);
    }

    @Test
    void testActorScore_PartialMatch() {
        User u = new User(
                3,
                List.of(),
                List.of(),
                List.of("Leonardo DiCaprio", "Emma Stone"),
                List.of()
        );
        double score = service.computeActorScore(u, movie);
        assertEquals(50.0, score, 0.1);
    }

    @Test
    void testActorScore_NoMatch() {
        User u = new User(4, List.of(), List.of(), List.of("Tom Hanks"), List.of());
        double score = service.computeActorScore(u, movie);
        assertEquals(0.0, score, 0.1);
    }

    @Test
    void testActorScore_NoFavoriteActors() {
        User u = new User(5, List.of(), List.of(), List.of(), List.of());
        double score = service.computeActorScore(u, movie);
        assertEquals(0.0, score, 0.1);
    }

    @Test
    void testRatingSimilarity_ExactMatch() {
        MovieRating r = new MovieRating(1, 4.8, java.time.Instant.now());
        User u = new User(10, List.of(), List.of(), List.of(), List.of(r));

        double score = service.computeRatingSimilarityScore(u, movie);
        assertEquals(100.0, score, 0.1);
    }

    @Test
    void testRatingSimilarity_SmallDifference() {
        MovieRating r = new MovieRating(1, 5.0, java.time.Instant.now());
        User u = new User(11, List.of(), List.of(), List.of(), List.of(r));

        double score = service.computeRatingSimilarityScore(u, movie);
        assertEquals(100.0, score, 0.1);
    }

    @Test
    void testRatingSimilarity_MaxDifference() {
        MovieRating r = new MovieRating(1, 1.0, java.time.Instant.now());
        User u = new User(12, List.of(), List.of(), List.of(), List.of(r));

        double score = service.computeRatingSimilarityScore(u, movie);
        assertEquals(0.0, score, 0.1);
    }

    @Test
    void testRatingSimilarity_UserNeverRatedMovie() {
        User u = new User(13, List.of(), List.of(), List.of(), List.of());
        double score = service.computeRatingSimilarityScore(u, movie);
        assertEquals(0.0, score, 0.1);
    }

    @Test
    void testPopularityScore_WithinValidRange() {
        double score = service.computePopularityScore(movie);
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    void testPopularityScore_HighRatedRecentMovie() {
        Movie m = new Movie(
                20,
                "New Hit",
                List.of("Action"),
                Year.now().getValue(),
                List.of(),
                5.0,
                List.of()
        );

        double score = service.computePopularityScore(m);
        assertTrue(score > 70); // should be high
    }

    @Test
    void testPopularityScore_OldLowRatedMovie() {
        Movie m = new Movie(
                21,
                "Old Flop",
                List.of("Drama"),
                1980,
                List.of(),
                1.5,
                List.of()
        );

        double score = service.computePopularityScore(m);
        assertTrue(score < 40); // should be low
    }

    @Test
    void testGetRecommendations_ReturnsSortedList() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(
                1,
                List.of("Sci-Fi", "Action"),
                List.of("Musical"),
                List.of(),
                List.of()
        );

        List<RecommendationDTO> list = service.getRecommendations(user);

        assertNotNull(list);
        assertFalse(list.isEmpty());

        // Ensure sorted descending
        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).finalScore() >= list.get(i + 1).finalScore());
        }
    }

    @Test
    void testGetRecommendations_AllFieldsPopulated() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(
                1,
                List.of("Drama"),
                List.of(),
                List.of(),
                List.of()
        );

        List<RecommendationDTO> list = service.getRecommendations(user);
        RecommendationDTO dto = list.get(0);

        assertNotNull(dto.title());
        assertTrue(dto.finalScore() >= 0 && dto.finalScore() <= 100);
        assertTrue(dto.genreScore() >= 0 && dto.genreScore() <= 100);
        assertTrue(dto.actorScore() >= 0 && dto.actorScore() <= 100);
        assertTrue(dto.ratingSimilarityScore() >= 0 && dto.ratingSimilarityScore() <= 100);
        assertTrue(dto.popularityScore() >= 0 && dto.popularityScore() <= 100);
    }

    @Test
    void testGetRecommendations_NullUser() {
        RecommendationScoringService service = new RecommendationScoringService();

        List<RecommendationDTO> list = service.getRecommendations(null);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetFirstNRecommendations_Zero() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of(), List.of(), List.of(), List.of());

        List<RecommendationDTO> list = service.getFirstNRecommendations(user, 0);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetFirstNRecommendations_NGreaterThanTotal() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Sci-Fi"), List.of(), List.of(), List.of());

        int totalMovies = MovieDataGenerator.getAllMovies().size();

        List<RecommendationDTO> list = service.getFirstNRecommendations(user, totalMovies + 10);

        assertEquals(totalMovies, list.size());
    }

    @Test
    void testGetFirstNRecommendations_ReturnsExactN() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Action"), List.of(), List.of(), List.of());

        List<RecommendationDTO> list = service.getFirstNRecommendations(user, 5);

        assertEquals(5, list.size());
    }

    @Test
    void testGetFirstNRecommendations_SortedDescending() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Drama"), List.of(), List.of(), List.of());

        List<RecommendationDTO> list = service.getFirstNRecommendations(user, 5);

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).finalScore() >= list.get(i + 1).finalScore());
        }
    }
    @Test
    void testGetFirstNRecommendations_NullUser() {
        RecommendationScoringService service = new RecommendationScoringService();

        List<RecommendationDTO> list = service.getFirstNRecommendations(null, 5);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testParallelMatchesSync() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Action"), List.of(), List.of(), List.of());

        List<RecommendationDTO> sync = service.getRecommendations(user);
        List<RecommendationDTO> parallel = service.getRecommendationsParallel(user);

        assertEquals(sync.size(), parallel.size());


    }
    @Test
    void testParallelSorted() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Drama"), List.of(), List.of(), List.of());

        List<RecommendationDTO> list = service.getRecommendationsParallel(user);

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).finalScore() >= list.get(i + 1).finalScore());
        }
    }
    @Test
    void testParallelNullUser() {
        RecommendationScoringService service = new RecommendationScoringService();

        List<RecommendationDTO> list = service.getRecommendationsParallel(null);

        assertTrue(list.isEmpty());
    }

    @Test
    void testAsyncScoresComplete() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Action"), List.of(), List.of(), List.of());
        Movie movie = MovieDataGenerator.getAllMovies().get(0);

        CompletableFuture<Double> genre = service.computeGenreScoreAsync(user, movie);
        CompletableFuture<Double> actor = service.computeActorScoreAsync(user, movie);
        CompletableFuture<Double> rating = service.computeRatingSimilarityScoreAsync(user, movie);
        CompletableFuture<Double> pop = service.computePopularityScoreAsync(movie);

        assertTrue(genre.join() >= 0);
        assertTrue(actor.join() >= 0);
        assertTrue(rating.join() >= 0);
        assertTrue(pop.join() >= 0);
    }

    @Test
    void testAsyncFinalScoreMatchesSync() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Drama"), List.of(), List.of(), List.of());
        Movie movie = MovieDataGenerator.getAllMovies().get(0);

        double sync = service.finalRecommendationScore(user, movie);

        CompletableFuture<RecommendationDTO> asyncDto =
                service.computeRecommendationAsync(user, movie);

        assertTrue(sync>0 && asyncDto.join().finalScore()>0);
    }

    @Test
    void testAsyncMatchesSync() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Action"), List.of(), List.of(), List.of());

        List<RecommendationDTO> sync = service.getRecommendations(user);
        List<RecommendationDTO> async = service.getRecommendationsAsync(user).join();

        assertEquals(sync.size(), async.size());


    }


    @Test
    void testAsyncSorted() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Drama"), List.of(), List.of(), List.of());

        List<RecommendationDTO> list = service.getRecommendationsAsync(user).join();

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).finalScore() >= list.get(i + 1).finalScore());
        }
    }

    @Test
    void testHybridMatchesSync() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Action"), List.of(), List.of(), List.of());

        List<RecommendationDTO> sync = service.getRecommendations(user);
        List<RecommendationDTO> hybrid = service.getRecommendationsHybrid(user).join();

        assertEquals(sync.size(), hybrid.size());


    }

    @Test
    void testHybridSorted() {
        RecommendationScoringService service = new RecommendationScoringService();

        User user = new User(1, List.of("Drama"), List.of(), List.of(), List.of());

        List<RecommendationDTO> list = service.getRecommendationsHybrid(user).join();

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).finalScore() >= list.get(i + 1).finalScore());
        }
    }

    @Test
    void testHybridNullUser() {
        RecommendationScoringService service = new RecommendationScoringService();

        List<RecommendationDTO> list = service.getRecommendationsHybrid(null).join();

        assertTrue(list.isEmpty());
    }

    @Test
    void testPopularityCaching() {
        RecommendationScoringService service = new RecommendationScoringService();
        Movie movie = MovieDataGenerator.getAllMovies().get(0);

        double first = service.computePopularityScoreCached(movie);
        double second = service.computePopularityScoreCached(movie);

        assertEquals(first, second);
    }


    @Test
    void testPopularityCacheSize() {
        RecommendationScoringService service = new RecommendationScoringService();

        List<Movie> movies = MovieDataGenerator.getAllMovies();
        movies.forEach(service::computePopularityScoreCached);

        assertEquals(movies.size(), service.getPopularityCacheSize());
    }

    @Test
    void testGenreCacheMatchesOriginal() {
        RecommendationScoringService service = new RecommendationScoringService();


        double original = service.computeGenreScore(user, movie);
        double cached = service.computeGenreScoreCached(user, movie);

        assertEquals(original, cached, 0.0001);
    }


    @Test
    void testGenreCacheStoresValue() {
        RecommendationScoringService service = new RecommendationScoringService();


        service.computeGenreScoreCached(user, movie);

        String key = user.userId() + ":" + movie.movieId() + ":genre";
        assertTrue(service.genreCache.contains(key));
    }
    @Test
    void testGenreCacheAvoidsRecomputation() {
        RecommendationScoringService service = new RecommendationScoringService();

        double first = service.computeGenreScoreCached(user, movie);
        double second = service.computeGenreScoreCached(user, movie);

        assertEquals(first, second);
    }



}

