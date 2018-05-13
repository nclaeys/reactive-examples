package com.axxes.reactive.example1.model;

public interface Stitchable<T> extends Comparable<T>{
    boolean isSubsequent(T next);
}
