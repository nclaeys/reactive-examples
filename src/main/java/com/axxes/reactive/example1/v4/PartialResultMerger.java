package com.axxes.reactive.example1.v4;

public interface PartialResultMerger<T> {
    T merge(T previous, T next);

}
