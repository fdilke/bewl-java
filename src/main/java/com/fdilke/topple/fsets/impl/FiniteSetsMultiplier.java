package com.fdilke.topple.fsets.impl;

import com.fdilke.topple.fsets.FiniteSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 13-Jan-2008
 * Time: 13:07:50
 * <p/>
 * Creates a productGetDiagram of a bunch of finite sets. The elements are just Object[] arrays with elements in the same order
 * as the corresponding components.
 */
public class FiniteSetsMultiplier {

    public static FiniteSet multiply(List<FiniteSet> components) {
        return new FiniteSetsMultiplier(components).getProduct();
    }

    private final List<FiniteSet> _components;
    private final Set<Object> _productSet;
    private final Object[] _array;

    private FiniteSetsMultiplier(List<FiniteSet> components) {
        _components = components;
        _productSet = new HashSet<Object>();
        _array = new Object[_components.size()];
        listRecursively(0);
    }

    private void listRecursively(int numComponent) {
        if (numComponent == _components.size()) {
            _productSet.add(_array.clone());
        } else {
            FiniteSet component = _components.get(numComponent);
            for (Object key : component.getUnderlyingSet()) {
                _array[numComponent] = key;
                listRecursively(numComponent + 1);
            }
        }
    }

    private FiniteSet getProduct() {
        return new FiniteSet(_productSet);
    }

    public static Object[] findEquivalentArray(FiniteSet product, Object[] array) {
        for (Object possible : product.getUnderlyingSet()) {
            Object[] possibleArray = (Object[]) possible;
            if (Arrays.deepEquals(possibleArray, array)) {
                return possibleArray;
            }
        }
        throw new IllegalArgumentException("Can't find equivalent array in productGetDiagram set");
    }
}

