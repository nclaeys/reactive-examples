package com.axxes.reactive.example1.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Result {

    private Integer bottom;
    private Integer top;
    private Integer value;

    public static Result fromPartialResult(PartialResult partialResult) {
        return new Result(
                partialResult.getLowerBound(),
                partialResult.getUpperBound(),
                partialResult.getValue()
        );
    }

    public Result(Integer lowest, Integer top, Integer value) {
        this.bottom = lowest;
        this.top = top;
        this.value = value;
    }

    public int getIntervalSize() {
        return top - bottom;
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
                .append("bottom", bottom)
                .append("top", top)
                .append("value", value)
                .toString();
    }
}
