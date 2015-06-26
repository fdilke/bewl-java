package com.fdilke.topple.diagrams;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 29-Jan-2008
 * Time: 23:05:45
 * To change this template use File | Settings | File Templates.
 */
public interface TerminatorDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> 
	extends ProductDiagram<DOT, ARROW> {
    public DOT getTerminator();

    public ARROW getConstantArrow(DOT source);
}
