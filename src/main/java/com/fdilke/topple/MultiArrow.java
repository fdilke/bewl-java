package com.fdilke.topple;

import com.fdilke.topple.diagrams.ProductDiagram;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 17-Jan-2008
 * Time: 21:22:21
 * To change this template use File | Settings | File Templates.
 */

// a MultiArrow is just a bean of a productGetDiagram diagram together with a map from the productGetDiagram object to somewhere else.

public class MultiArrow<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    private ProductDiagram<DOT, ARROW> _diagram;
    private ARROW _arrow;

    public MultiArrow(ProductDiagram<DOT, ARROW> diagram, ARROW arrow) {
        if (arrow.getSource() != diagram.getProduct()) {
            throw new IllegalArgumentException("Arrow does not have product object as source");
        }
        _diagram = diagram;
        _arrow = arrow;
    }


    public ProductDiagram<DOT, ARROW> getProductDiagram() {
        return _diagram;
    }

    public ARROW getArrow() {
        return _arrow;
    }
}
