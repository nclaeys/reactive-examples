package com.axxes.reactive.example1.v4;

import com.axxes.reactive.example1.model.Stitchable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class MergePartialResultPublisher<T extends Stitchable> implements Publisher<T>{
    private final Flux<T> partialResultFlux;
    private final PartialResultMerger<T> merger;

    public MergePartialResultPublisher(Flux<T> partialResultFlux, PartialResultMerger<T> merger) {
        this.partialResultFlux = partialResultFlux;
        this.merger = merger;
    }

    private Consumer<List<T>> mergeResults(FluxSink<T> outputSink) {
        return partialResults -> {
            if (partialResults.isEmpty()) {
                return;
            }

            TreeSet<T> results = new TreeSet<>(partialResults);
            Iterator<T> it = results.iterator();
            T current = it.next();
            while (it.hasNext()) {
                T previous = current;
                current = it.next();

                if (previous.isSubsequent(current)) {
                    current = merger.merge(previous, current);
                } else {
                    outputSink.next(previous);
                }
            }
            outputSink.next(current);
        };
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        Flux.<T>create(outputSink -> partialResultFlux.buffer().subscribe(
                mergeResults(outputSink),
                outputSink::error,
                outputSink::complete)).subscribe(subscriber);
    }
}
