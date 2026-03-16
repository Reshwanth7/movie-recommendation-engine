package com.reshwanth.movieengine.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class CustomThreadPool {
    public static final ExecutorService EXECUTOR_POOL =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Parallelism for CPU-bound tasks
    public static final ForkJoinPool PARALLEL_POOL =
            new ForkJoinPool(Runtime.getRuntime().availableProcessors());

}
