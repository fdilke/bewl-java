package com.fdilke.topple.diagrams;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 14-Jan-2008
 * Time: 21:00:36
 * To change this template use File | Settings | File Templates.
 */
public interface EqualizerDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public ARROW getEqualizer();

    public ARROW factorize(EqualizerSituation<DOT, ARROW> situation);
}

