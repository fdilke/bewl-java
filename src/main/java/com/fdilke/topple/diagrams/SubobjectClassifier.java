package com.fdilke.topple.diagrams;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 29-Jan-2008
 * Time: 23:47:19
 * To change this template use File | Settings | File Templates.
 */
public interface SubobjectClassifier<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public ARROW getTruth();

    public PullbackDiagram<DOT, ARROW> pullbackMonic(ARROW monic);
}
