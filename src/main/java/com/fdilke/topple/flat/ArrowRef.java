package com.fdilke.topple.flat;

/**
 * Boilerplate "identifier" class wrapping a long value.
 **/

public class ArrowRef {
    private static long s_sequenceNumber = 0;
    private long _id;

    public ArrowRef() {
        _id = s_sequenceNumber++;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrowRef arrowRef = (ArrowRef) o;

        if (_id != arrowRef._id) return false;

        return true;
    }

    @Override public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }
}
