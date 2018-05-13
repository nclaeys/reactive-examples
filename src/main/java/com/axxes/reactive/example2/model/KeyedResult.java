package com.axxes.reactive.example2.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class KeyedResult {

    private String key;
    private float result;

    public KeyedResult(String key, float result) {
        this.key = key;
        this.result = result;
    }

    public String getKey() {
        return key;
    }

    public float getResult() {
        return result;
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
                .append("key", key)
                .append("result", result)
                .toString();
    }
}
