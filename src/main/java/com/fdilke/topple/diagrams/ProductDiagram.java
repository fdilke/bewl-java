package com.fdilke.topple.diagrams;

import java.util.List;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 10-Jan-2008
 * Time: 00:06:50
 * To change this template use File | Settings | File Templates.
 */
public interface ProductDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public DOT getProduct();

    public ARROW multiplyArrows(DOT source, List<ARROW> arrows);

    public List<ARROW> getProjections();
}
