package com.fdilke.topple.flat.impl;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.flat.ArrowRef;
import com.fdilke.topple.flat.DotRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference descriptor for a product diagram in an "OO topos".
 */

public class HashedProductDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    private final ProductDiagram<DOT, ARROW> _baseProduct;
    private final DotRef _productDotRef;
    private final List<ArrowRef> _projectionsRef;

    public HashedProductDiagram(ProductDiagram<DOT, ARROW> baseProduct, ToposHash<DOT, ARROW> hash) {
        _baseProduct = baseProduct;
        _productDotRef = hash.makeDotRef(baseProduct.getProduct());

        _projectionsRef = new ArrayList<ArrowRef>();
        for (ARROW projection : baseProduct.getProjections()) {
            _projectionsRef.add(hash.makeArrowRef(projection));
        }
    }

    public ProductDiagram<DOT, ARROW> getBaseProduct() {
        return _baseProduct;
    }

    public DotRef getProductDotRef() {
        return _productDotRef;
    }

    public List<ArrowRef> getProjectionsRef() {
        return _projectionsRef;
    }
}
