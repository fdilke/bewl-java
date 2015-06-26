package com.fdilke.topple.diagrams;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * A triple of arrows r, s, t with r.s = r.t 
 */

public class EqualizerSituation<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {

	private final ARROW r, s, t;
	
	public EqualizerSituation(ARROW r, ARROW s, ARROW t) {
	    ARROW rs = s.compose(r);
	    ARROW rt = t.compose(r);
	    if (!rs.equals(rt)) {
	        throw new IllegalArgumentException("arrows don't equalize");
	    }
	    this.r = r;
	    this.s = s;
	    this.t = t;
	}

	public ARROW getR() {
		return r;
	}

	public ARROW getS() {
		return s;
	}

	public ARROW getT() {
		return t;
	}
}
