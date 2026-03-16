package com.reshwanth.movieengine.service;

public interface Cache<K, V> {
    V get(K key);
    void put(K key, V value);
    boolean contains(K key);
}
