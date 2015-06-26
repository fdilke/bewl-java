package com.fdilke.topple.diagrams;

import java.util.Collections;
import java.util.List;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.sugar.Topos;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 03-Feb-2008
 * Time: 21:50:56
 * To change this template use File | Settings | File Templates.
 */
public class StandardTerminatorDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>>
        implements TerminatorDiagram<DOT, ARROW> {
    private final ProductDiagram<DOT, ARROW> _emptyProductDiagram;

    public StandardTerminatorDiagram(Topos<DOT, ARROW> topos) {
        _emptyProductDiagram = topos.getProductDiagram(Collections.<DOT>emptyList());
    }

    public DOT getTerminator() {
        return _emptyProductDiagram.getProduct();
    }

    public ARROW getConstantArrow(DOT source) {
        return _emptyProductDiagram.multiplyArrows(source, Collections.<ARROW>emptyList());
    }

    // remaining methods also give the terminator diagram a 'product diagram' interface
    
	public DOT getProduct() {
		return getTerminator();
	}

	public ARROW multiplyArrows(DOT source, List<ARROW> arrows) {
		return _emptyProductDiagram.multiplyArrows(source, Collections.<ARROW>emptyList());
	}

	public List<ARROW> getProjections() {
		return Collections.<ARROW>emptyList();
	}
}
