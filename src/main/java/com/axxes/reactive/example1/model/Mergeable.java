package com.axxes.reactive.example1.model;

public interface Mergeable<T> extends Comparable<T>{
    boolean isSubsequent(T next);

    boolean isPartial();
}
