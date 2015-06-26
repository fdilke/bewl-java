package com.fdilke.topple.sugar.algebra.schema;

import com.fdilke.topple.sugar.algebra.universal.Axioms;
import com.fdilke.topple.sugar.algebra.universal.Law;
import com.fdilke.topple.sugar.algebra.universal.Operator;

@Axioms public abstract class HeytingAlgebra<T> extends Lattice<T> {
	@Operator public abstract T implies(T x, T y);
	
	// The 4 algebraic laws for implication. Hope the names are right.
	
	@Law public boolean x_implies_x(T x) {
		// x=>x = TRUE
		return equality(
			implies(x, x), 
			TRUE()
		);
	}

	@Law public boolean modusPonens(T x, T y) {
		return equality(
			and(x, implies(x, y)), 
			and(x, y)
		);
	}

	@Law public boolean xSupersedesAnythingImplying_x(T x, T y) {
		return equality(
			and(x, implies(y, x)), 
			x
		);
	}

	@Law public boolean impliesDistributesOverAnd(T x, T y, T z) {
		return equality(
			implies(x, and(y, z)), 
			and(implies(x, y), implies(x, z))
		);
	}
	
	// and a derived operator
	public T not(T x) {
		return implies(x, FALSE());
	}
}
