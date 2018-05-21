package com.axxes.reactive.example1.v3;

import com.axxes.reactive.example1.CombineTimeSplittedResults;
import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import com.axxes.reactive.example1.v2.FuturePublisher;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CombineTimeSplittedResultsV3 implements CombineTimeSplittedResults {

    @Override
    public Flux<Result> transform(List<CompletableFuture<List<PartialResult>>> futureResults) {
        return Flux.from(new FuturePublisher<>(futureResults))
                .flatMap(Flux::fromIterable)
                .groupBy(PartialResult::isPartial)
                .flatMap(groupedFlux -> {
                    if (groupedFlux.key()) {
                        return new MergeableFlux(groupedFlux);
                    }
                    return groupedFlux;
                })
                .map(Result::fromPartialResult)
                .filter(r -> r.getIntervalSize() > 2);
    }

    private static final class MergeableFlux implements Publisher<PartialResult> {
        private final GroupedFlux<Boolean, PartialResult> partialsFlow;

        MergeableFlux(GroupedFlux<Boolean, PartialResult> partialsFlow) {
            this.partialsFlow = partialsFlow;
        }

        @Override
        public void subscribe(Subscriber<? super PartialResult> subscriber) {
            Flux.<PartialResult>create(outputSink ->
                    partialsFlow.buffer().subscribe(
                            Merger.merge(outputSink),
                            outputSink::error,
                            outputSink::complete)
            ).subscribe(subscriber);
        }
    }
}
