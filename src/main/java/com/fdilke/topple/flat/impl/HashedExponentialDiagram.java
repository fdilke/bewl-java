package com.fdilke.topple.flat.impl;

import com.fdilke.topple.ToposDot;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.flat.DotRef;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 16-Apr-2008
 * Time: 01:17:01
 * To change this template use File | Settings | File Templates.
 */
public class HashedExponentialDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    private final ExponentialDiagram<DOT, ARROW> _baseExponential;
    private final DotRef _exponentialDotRef;

    public HashedExponentialDiagram(ExponentialDiagram<DOT, ARROW> baseExponential, ToposHash<DOT, ARROW> hash) {
        _baseExponential = baseExponential;
        DOT dotExponential = baseExponential.getEvaluation().getProductDiagram().getProjections().get(0).getTarget();
        _exponentialDotRef = hash.makeDotRef(dotExponential);
    }

    public ExponentialDiagram<DOT, ARROW> getBaseExponential() {
        return _baseExponential;
    }

    public DotRef getExponentialDotRef() {
        return _exponentialDotRef;
    }
}

