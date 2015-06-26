package com.fdilke.topple.flat.impl;

import com.fdilke.topple.*;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.flat.ArrowRef;
import com.fdilke.topple.flat.DotRef;
import com.fdilke.topple.flat.ProductRef;
import com.fdilke.topple.flat.ExponentialRef;
import com.fdilke.topple.sugar.Topos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Mini-database enabling translation of "reference keys" for dots and arrows.
 * Masks the type as well - above this layer, we don't have to think about what types the dots and arrows are.
 */

public class ToposHash<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    private final Topos<DOT, ARROW> _topos;
    private final HashMap<DotRef, DOT> _dotMap = new HashMap<DotRef, DOT>();
    private final HashMap<ArrowRef, ARROW> _arrowMap = new HashMap<ArrowRef,ARROW>();
    private final HashMap<ProductRef, HashedProductDiagram<DOT, ARROW>> _productMap = new HashMap<ProductRef, HashedProductDiagram<DOT,ARROW>>();
    private final HashMap<ExponentialRef, HashedExponentialDiagram<DOT, ARROW>> _exponentialMap = new HashMap<ExponentialRef, HashedExponentialDiagram<DOT,ARROW>>();

    public ToposHash(Topos<DOT, ARROW> topos) {
        _topos = topos;
    }

    public DotRef makeDotRef(DOT dot) {
        DotRef ref = new DotRef();
        _dotMap.put(ref, dot);
        return ref;
    }

    public ArrowRef makeArrowRef(ARROW arrow) {
        ArrowRef ref = new ArrowRef();
        _arrowMap.put(ref, arrow);
        return ref;
    }

    public ARROW getArrow(ArrowRef arrowRef) {
        return _arrowMap.get(arrowRef);
    }

    public DOT getDot(DotRef dotRef) {
        return _dotMap.get(dotRef);
    }

    public ProductRef makeProduct(List<DotRef> componentsRef) {
        // Build the list of components from their references
        List<DOT> components = new ArrayList<DOT>();
        for (DotRef dotRef : componentsRef) {
            components.add(_dotMap.get(dotRef));
        }
        ProductDiagram<DOT, ARROW> productDiagram = _topos.getProductDiagram(components);
        // Build the appropriate "hashed product diagram"
        HashedProductDiagram<DOT, ARROW> hashedProductDiagram = new HashedProductDiagram<DOT, ARROW>(productDiagram, this);

        // Create a reference to this object and return it
        ProductRef productRef = new ProductRef();
        _productMap.put(productRef, hashedProductDiagram);
        return productRef;
    }
    
    public HashedProductDiagram<DOT, ARROW> getProductDiagram(ProductRef productRef) {
        return _productMap.get(productRef);
    }

    public ExponentialRef makeExponential(DotRef refIndex, DotRef refExponent) {
        DOT dotIndex = _dotMap.get(refIndex);
        DOT dotExponent = _dotMap.get(refExponent);

        // Build the appropriate "hashed exponential diagram"
        ExponentialDiagram<DOT, ARROW> exponentialDiagram = _topos.getExponentialDiagram(dotIndex, dotExponent);
        HashedExponentialDiagram<DOT, ARROW> hashedExponentialDiagram = new HashedExponentialDiagram<DOT, ARROW>(exponentialDiagram, this);

        // Create a reference to this object and return it
        ExponentialRef exponentialRef = new ExponentialRef();
        _exponentialMap.put(exponentialRef, hashedExponentialDiagram);
        return exponentialRef;
    }

    public HashedExponentialDiagram<DOT, ARROW> getExponentialDiagram(ExponentialRef refExponential) {
        return _exponentialMap.get(refExponential);
    }
}
