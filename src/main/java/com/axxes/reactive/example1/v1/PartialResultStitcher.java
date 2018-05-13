package com.axxes.reactive.example1.v1;

import com.axxes.reactive.example1.model.PartialResult;
import org.slf4j.Logger;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.FluxSink;

import java.util.Iterator;
import java.util.TreeSet;

import static org.slf4j.LoggerFactory.getLogger;

public class PartialResultStitcher extends BaseSubscriber<PartialResult> {
    private static final Logger LOGGER = getLogger(PartialResultStitcher.class);
    private TreeSet<PartialResult> stitchables;
    private FluxSink<PartialResult> sink;

    PartialResultStitcher(FluxSink<PartialResult> sink) {
        this.sink = sink;
        stitchables = new TreeSet<>();
    }

    @Override
    protected void hookOnNext(PartialResult value) {
        handleResult(value);
    }

    @Override
    protected void hookOnComplete() {
        processStitchables();
        sink.complete();
    }

    private void handleResult(PartialResult result) {
        LOGGER.info("new result {}", result);
        if (result.isPartial()) {
            stitchables.add(result);
        } else {
            sink.next(result);
        }
    }

    private void processStitchables() {
        if (stitchables.isEmpty()) {
            return;
        }

        Iterator<PartialResult> it = stitchables.iterator();
        PartialResult current = it.next();
        while (it.hasNext()) {
            PartialResult previous = current;
            current = it.next();

            if (isStitchable(previous, current)) {
                current = PartialResult.mergeResult(previous, current);
            } else {
                // emit previous; continue stitching with new current
                sink.next(previous);
            }
        }
        sink.next(current);
    }

    private boolean isStitchable(PartialResult previous, PartialResult current) {
        return previous.getUpperBound() == current.getLowerBound();
    }
}
