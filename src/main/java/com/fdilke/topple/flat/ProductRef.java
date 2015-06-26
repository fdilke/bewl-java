package com.fdilke.topple.flat;

/**
 * A reference to a product diagram
 */
public class ProductRef implements StructuredDotRef {
    private static long s_sequenceNumber = 0;
    private long _id;

    public ProductRef() {
        _id = s_sequenceNumber++;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductRef productRef = (ProductRef) o;

        return _id == productRef._id;
    }

    @Override public int hashCode() {
        return (int) (_id ^ (_id >>> 32));
    }
}
