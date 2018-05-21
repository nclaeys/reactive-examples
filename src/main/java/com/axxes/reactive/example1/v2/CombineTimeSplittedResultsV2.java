package com.axxes.reactive.example1.v2;

import com.axxes.reactive.example1.model.PartialResult;
import com.axxes.reactive.example1.model.Result;
import com.axxes.reactive.example1.CombineTimeSplittedResults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CombineTimeSplittedResultsV2 implements CombineTimeSplittedResults {

    @Override
    public Flux<Result> transform(List<CompletableFuture<List<PartialResult>>> futureResults) {
        Flux<PartialResult> inputFlux = adaptToReactive(futureResults);
        return stitchPartials(inputFlux)
                .map(Result::fromPartialResult)
                .filter(r -> r.getIntervalSize() > 2);
    }

    private Flux<PartialResult> adaptToReactive(List<CompletableFuture<List<PartialResult>>> futures) {
        return Flux.from(new FuturePublisher<>(futures))
                .flatMap(Flux::fromIterable);
    }

    private Flux<PartialResult> stitchPartials(Flux<PartialResult> inputFlux) {
        return Flux.create(outputSink -> {
            fillStitchablePartialsStream(inputFlux, outputSink)
                    .buffer()
                    .subscribe(
                            Stitcher.stitcher(outputSink),
                            outputSink::error,
                            outputSink::complete
                    );
        });
    }

    private Flux<PartialResult> fillStitchablePartialsStream(Flux<PartialResult> inputFlux,
                                                             FluxSink<PartialResult> outputSink) {
        return Flux.create(stitchableSink ->
                inputFlux.subscribe(
                        routeToStitchableOrOutput(stitchableSink, outputSink),
                        outputSink::error,
                        stitchableSink::complete)
        );
    }

    private Consumer<PartialResult> routeToStitchableOrOutput(FluxSink<PartialResult> partialResults,
                                                              FluxSink<PartialResult> output) {
        return p -> {
            if (p.isPartial()) {
                partialResults.next(p);
            } else {
                output.next(p);
            }
        };
    }
}
