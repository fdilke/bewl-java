package com.fdilke.topple.sugar.algebra.universal;

import java.util.List;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.sugar.Element;

public abstract class ParsedLaw <
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>,
	AXIOMS extends Algebra<? super AXIOMS>
> {
	private final String name;
	private final boolean isStrict;
	private final int arity;
	private final List<Algebra<?>> paramSpaces;

	public ParsedLaw(String name, boolean isStrict, int arity, List<Algebra<?>> paramSpaces) {
		this.name = name;
		this.isStrict = isStrict;
		this.arity = arity;
		this.paramSpaces = paramSpaces;
	}

	public String getName() {
		return name;
	}
	
	public int getArity() {
		return arity;
	}
	
	public List<Algebra<?>> getParamSpaces() {
		return paramSpaces;
	}

	public abstract boolean verify(AXIOMS axioms, List<Element<?>> arrowArgs);

	public boolean isStrict() {
		return isStrict;
	}
}
