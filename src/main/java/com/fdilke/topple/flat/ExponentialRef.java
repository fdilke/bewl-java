package com.fdilke.topple.flat;

/**
  * Reference to an getExponentialDiagram diagram
 */

public class ExponentialRef implements StructuredDotRef {
    private static long s_sequenceNumber = 0;
    private long _id;

    public ExponentialRef() {
        _id = s_sequenceNumber++;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExponentialRef exponentialRef = (ExponentialRef) o;

        return _id == exponentialRef._id;
    }

    @Override public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }
}
