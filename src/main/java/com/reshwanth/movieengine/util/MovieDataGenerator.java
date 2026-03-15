package com.reshwanth.movieengine.util;

import com.reshwanth.movieengine.domain.Movie;

import java.util.List;

public class MovieDataGenerator {

    public static List<Movie> getAllMovies() {
        return List.of(
                new Movie(1, "The Matrix", List.of("Action", "Sci-Fi"), 1999,
                        List.of("Keanu Reeves", "Laurence Fishburne"), 4.7,
                        List.of("cyberpunk", "classic")),

                new Movie(2, "Inception", List.of("Sci-Fi", "Thriller"), 2010,
                        List.of("Leonardo DiCaprio", "Tom Hardy"), 4.8,
                        List.of("mind-bending", "dream")),

                new Movie(3, "Interstellar", List.of("Sci-Fi", "Drama"), 2014,
                        List.of("Matthew McConaughey", "Anne Hathaway"), 4.6,
                        List.of("space", "emotional")),

                new Movie(4, "The Dark Knight", List.of("Action", "Crime"), 2008,
                        List.of("Christian Bale", "Heath Ledger"), 4.9,
                        List.of("superhero", "gritty")),

                new Movie(5, "Avatar", List.of("Sci-Fi", "Adventure"), 2009,
                        List.of("Sam Worthington", "Zoe Saldana"), 4.2,
                        List.of("fantasy", "alien")),

                new Movie(6, "Titanic", List.of("Romance", "Drama"), 1997,
                        List.of("Leonardo DiCaprio", "Kate Winslet"), 4.5,
                        List.of("classic", "emotional")),

                new Movie(7, "Gladiator", List.of("Action", "Drama"), 2000,
                        List.of("Russell Crowe", "Joaquin Phoenix"), 4.7,
                        List.of("historical", "epic")),

                new Movie(8, "The Avengers", List.of("Action", "Sci-Fi"), 2012,
                        List.of("Robert Downey Jr.", "Chris Evans"), 4.4,
                        List.of("superhero", "team")),

                new Movie(9, "Iron Man", List.of("Action", "Sci-Fi"), 2008,
                        List.of("Robert Downey Jr.", "Gwyneth Paltrow"), 4.6,
                        List.of("superhero", "tech")),

                new Movie(10, "Joker", List.of("Crime", "Drama"), 2019,
                        List.of("Joaquin Phoenix"), 4.3,
                        List.of("psychological", "dark")),

                new Movie(11, "La La Land", List.of("Romance", "Musical"), 2016,
                        List.of("Ryan Gosling", "Emma Stone"), 4.1,
                        List.of("music", "dance")),

                new Movie(12, "Whiplash", List.of("Drama", "Music"), 2014,
                        List.of("Miles Teller", "J.K. Simmons"), 4.8,
                        List.of("intense", "music")),

                new Movie(13, "The Social Network", List.of("Drama", "Biography"), 2010,
                        List.of("Jesse Eisenberg", "Andrew Garfield"), 4.0,
                        List.of("tech", "startup")),

                new Movie(14, "Parasite", List.of("Thriller", "Drama"), 2019,
                        List.of("Song Kang-ho"), 4.6,
                        List.of("social", "twist")),

                new Movie(15, "The Shawshank Redemption", List.of("Drama"), 1994,
                        List.of("Tim Robbins", "Morgan Freeman"), 4.9,
                        List.of("classic", "prison")),

                new Movie(16, "The Godfather", List.of("Crime", "Drama"), 1972,
                        List.of("Marlon Brando", "Al Pacino"), 4.9,
                        List.of("mafia", "classic")),

                new Movie(17, "The Lion King", List.of("Animation", "Family"), 1994,
                        List.of("Matthew Broderick"), 4.3,
                        List.of("family", "musical")),

                new Movie(18, "Frozen", List.of("Animation", "Family"), 2013,
                        List.of("Idina Menzel"), 4.0,
                        List.of("kids", "musical")),

                new Movie(19, "Toy Story", List.of("Animation", "Comedy"), 1995,
                        List.of("Tom Hanks"), 4.2,
                        List.of("family", "classic")),

                new Movie(20, "Mad Max: Fury Road", List.of("Action", "Adventure"), 2015,
                        List.of("Tom Hardy", "Charlize Theron"), 4.7,
                        List.of("post-apocalyptic", "chase"))
        );
    }
}
