package com.axxes.reactive.example1.model;

import com.google.common.collect.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PartialResult implements Comparable<PartialResult> {

    private int lowerBound;
    private int upperBound;
    private int value;
    private boolean partial;

    public PartialResult(int lowerBound, int upperBound, int value) {
        this(lowerBound, upperBound, value, false);
    }

    public PartialResult(int lowerBound, int upperBound, int value, boolean partial) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.value = value;
        this.partial = partial;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getValue() {
        return value;
    }

    public Range<Integer> getRange() {
        return Range.closed(lowerBound, upperBound);
    }

    public boolean isPartial() {
        return partial;
    }

    public static PartialResult stitchPartialResults(PartialResult previous, PartialResult next) {
        Range<Integer> range = previous.getRange().span(next.getRange());
        Integer upperValue = range.upperEndpoint();
        Integer lowerValue = range.lowerEndpoint();
        Integer newValue = previous.getValue() + next.getValue();
        return new PartialResult(lowerValue, upperValue, newValue
        );
    }

    @Override
    public int compareTo(PartialResult o) {
        return lowerBound - o.lowerBound;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("lowerBound", lowerBound)
                .append("upperBound", upperBound)
                .append("value", value)
                .toString();
    }
}
