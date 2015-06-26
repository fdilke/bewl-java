package com.fdilke.topple.sugar.algebra.universal;

import com.fdilke.topple.sugar.Dot;

public interface Algebra<AXIOMS extends Algebra<? super AXIOMS>> {
	public Dot<?> dotCarrier();
	public boolean checkOptionalLaw(String lawName);
	public boolean equality(Object element1, Object element2);
}
