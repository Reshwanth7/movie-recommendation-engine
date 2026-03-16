# Modern Java Movie Recommendation Engine

## Overview
A high‑performance, production‑ready Movie Recommendation Engine built using Modern Java (17+), featuring multiple concurrency pipelines, caching, global executors, and a clean modular architecture. This project demonstrates mastery of Modern Java concurrency, functional programming, and performance optimization.

## Features

### Multi‑Pipeline Architecture
- Synchronous Pipeline — baseline, simple and readable  
- Parallel Pipeline — custom ForkJoinPool for CPU‑level parallelism  
- Async Pipeline — CompletableFuture‑based scoring  
- Hybrid Pipeline — parallel movie iteration + async scoring (fastest)

### Advanced Scoring System
Each movie is scored using four independent modules: genre similarity, actor preference, rating similarity, and popularity score. Final score uses configurable weights.

### Caching & Memoization
- Popularity score cache  
- User‑specific scoring cache  
- Final recommendation list cache  
- Pluggable Cache<K, V> interface with InMemoryCache implementation

### Global Executors
- One global ForkJoinPool for parallel pipelines  
- One global ExecutorService for async pipelines  
- Zero thread churn, stable performance

### Clean Modular Architecture
com.reshwanth.recommendation
├── model/
├── scoring/
├── pipeline/
├── cache/
├── executors/
├── config/
├── util/
└── RecommendationEngine.java


## How Scoring Works

### Scoring Modules
- computeGenreScore(user, movie)  
- computeActorScore(user, movie)  
- computeRatingSimilarityScore(user, movie)  
- computePopularityScore(movie)

### Final Score Formula
finalScore =
genreScore      * 0.35 +
actorScore      * 0.25 +
ratingScore     * 0.20 +
popularityScore * 0.20


## Caching Layer

### Popularity Cache
movieId → popularityScore

### User Score Cache
"userId:movieId:genre" → genreScore  
"userId:movieId:actor" → actorScore  
"userId:movieId:rating" → ratingSimilarityScore

### Final Result Cache
"userId:final" → List<RecommendationDTO>

All caches use the pluggable Cache<K, V> interface.

## Installation

### Requirements
- Java 17+  
- Maven or Gradle

### Clone the Repository
```
bash
git clone https://github.com/reshwanth/modern-java-recommendation-engine.git
cd modern-java-recommendation-engine
```

### Usage Example

```
RecommendationEngine engine = new RecommendationEngine();

User user = new User(
        1,
        List.of("Action", "Drama"),
        List.of(),
        List.of("Tom Cruise"),
        List.of()
);

List<RecommendationDTO> results = engine.recommendHybrid(user);
results.forEach(System.out::println);
```

### Lessons Learned
This project demonstrates Modern Java concurrency (CompletableFuture, ForkJoinPool), functional programming with streams, clean architecture and modular design, caching and memoization, performance tuning, and building production‑ready Java libraries.
