package com.fdilke.topple.diagrams;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 20-Jan-2008
 * Time: 22:38:10
 * To change this template use File | Settings | File Templates.
 */
public interface ExponentialDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public MultiArrow<DOT, ARROW> getEvaluation();
    public ARROW getTranspose(MultiArrow<DOT, ARROW> multiArrow);
}
