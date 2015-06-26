package com.fdilke.topple.flat;

/**
 * Boilerplate "identifier" class wrapping a long value.
 **/

public class DotRef {
    private static long s_sequenceNumber = 0;
    private long _id;

    public DotRef() {
        _id = s_sequenceNumber++;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DotRef dotRef = (DotRef) o;

        if (_id != dotRef._id) return false;

        return true;
    }

    @Override public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }
}
