package com.reshwanth.movieengine.util;

import com.reshwanth.movieengine.domain.User;

import java.util.List;

public class UserDataGenerator {

    public static List<User> getAllUsers() {

        User user1 = new User(
                1,
                List.of("Sci-Fi", "Action", "Thriller"),
                List.of("Musical"),
                List.of("Leonardo DiCaprio", "Keanu Reeves"),
                RatingDataGenerator.ratingsFor(
                        2, 5,   // Inception
                        1, 5,   // Matrix
                        3, 4,   // Interstellar
                        4, 5,   // Dark Knight
                        5, 3,   // Avatar
                        10, 4   // Joker
                )
        );

        User user2 = new User(
                2,
                List.of("Romance", "Drama"),
                List.of("Action"),
                List.of("Emma Stone", "Ryan Gosling"),
                RatingDataGenerator.ratingsFor(
                        11, 5,  // La La Land
                        6, 4,   // Titanic
                        12, 4,  // Whiplash
                        14, 3,  // Parasite
                        13, 2   // Social Network
                )
        );

        User user3 = new User(
                3,
                List.of("Animation", "Family", "Comedy"),
                List.of("Crime"),
                List.of("Tom Hanks"),
                RatingDataGenerator.ratingsFor(
                        19, 5,  // Toy Story
                        18, 4,  // Frozen
                        17, 5,  // Lion King
                        8, 3,   // Avengers
                        9, 3    // Iron Man
                )
        );

        return List.of(user1, user2, user3);
    }
}
