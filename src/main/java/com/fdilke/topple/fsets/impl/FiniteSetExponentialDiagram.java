package com.fdilke.topple.fsets.impl;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 20-Jan-2008
 * Time: 23:34:33
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSetExponentialDiagram implements ExponentialDiagram<FiniteSet, FiniteSetArrow> {
    private final MultiArrow<FiniteSet, FiniteSetArrow> _evaluation;
    private final Map<Object, Integer> _indexLookup; // lookup mechanism that assigns indexes to source objects

    public FiniteSetExponentialDiagram(FiniteSet target, FiniteSet source) {
    	List<FiniteSet> componentsRepeat = Collections.nCopies(source.getUnderlyingSet().size(), target);
        FiniteSet exp = FiniteSetsMultiplier.multiply(componentsRepeat);
        _indexLookup = new HashMap<Object, Integer>();
        int nextIndex = 0;
        for (Object sourceObject : source.getUnderlyingSet()) {
            _indexLookup.put(sourceObject, nextIndex++);
        }
        List<FiniteSet> componentsExpDiagram = Arrays.asList(exp, source);
        FiniteSetProductDiagram productExpDiagram = new FiniteSetProductDiagram(componentsExpDiagram);
        FiniteSet productObject = productExpDiagram.getProduct();
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object tupleSource : productObject.getUnderlyingSet()) {
            Object[] tupleSourceArray = (Object[]) tupleSource;
            Object[] tuple = (Object[]) tupleSourceArray[0];
            Object sourceObject = tupleSourceArray[1];
            int index = _indexLookup.get(sourceObject);
            Object image = tuple[index];
            map.put(tupleSource, image);
        }
        FiniteSetArrow eval = new FiniteSetArrow(productObject, target, map);
        _evaluation = new MultiArrow<FiniteSet, FiniteSetArrow>(productExpDiagram, eval);
    }

    public MultiArrow<FiniteSet, FiniteSetArrow> getEvaluation() {
        return _evaluation;
    }

    public FiniteSetArrow getTranspose(MultiArrow<FiniteSet, FiniteSetArrow> f2) {
        FiniteSet originalSource = _evaluation.getProductDiagram().getProjections().get(1).getTarget();
        FiniteSet originalTarget = _evaluation.getArrow().getTarget();
        FiniteSet exponentialObject = _evaluation.getProductDiagram().getProjections().get(0).getTarget();
        // should these be members? It's relatively quick to extract them.

        // make sure it's a multi-arrow of the right type, something x source -> target
        ProductDiagram<FiniteSet, FiniteSetArrow> productDiagram = f2.getProductDiagram();
        if (productDiagram.getProjections().size() != 2) {
            throw new IllegalArgumentException("multi-arrow has wrong arity");
        } else if (productDiagram.getProjections().get(1).getTarget() != originalSource) {
            throw new IllegalArgumentException("multi-arrow has wrong type in 2nd argument");
        } else if (f2.getArrow().getTarget() != originalTarget) {
            throw new IllegalArgumentException("multi-arrow has wrong return type");
        }
        FiniteSet transposeSource = productDiagram.getProjections().get(0).getTarget();
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object transposeSourceObject : transposeSource.getUnderlyingSet()) {
            Object[] functionArray = new Object[originalSource.getUnderlyingSet().size()];
            for (Object originalSourceObject : originalSource.getUnderlyingSet()) {
                Object[] pair = new Object[]{transposeSourceObject, originalSourceObject};
                Object equivalentPair = FiniteSetsMultiplier.findEquivalentArray(f2.getArrow().getSource(), pair);
                Object image = f2.getArrow().getUnderlyingMap().get(equivalentPair);
                int index = _indexLookup.get(originalSourceObject);
                functionArray[index] = image;
            }
            map.put(transposeSourceObject, FiniteSetsMultiplier.findEquivalentArray(exponentialObject, functionArray));
        }
        return new FiniteSetArrow(transposeSource, exponentialObject, map);
    }
}
