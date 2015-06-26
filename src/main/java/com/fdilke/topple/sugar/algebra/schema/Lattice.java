package com.fdilke.topple.sugar.algebra.schema;

import com.fdilke.topple.sugar.algebra.universal.Algebra;
import com.fdilke.topple.sugar.algebra.universal.Axioms;
import com.fdilke.topple.sugar.algebra.universal.Law;
import com.fdilke.topple.sugar.algebra.universal.Operator;
import com.fdilke.topple.sugar.algebra.universal.OptionalLaw;

// Axioms for lattices
@Axioms public abstract class Lattice<T> implements Algebra<Lattice<T>> {
	@Operator public abstract T FALSE();
	@Operator public abstract T TRUE();
	@Operator public abstract T and(T x, T y);		
	@Operator public abstract T or(T x, T y);		

	// <X, 0=false, v> is a commutative monoid
	
	@Law public boolean falseIsUnitForOr(T x) {
		return equality(
			or(FALSE(), x),
			x
		);
	}
	
	@Law public boolean orIsAssociative(T x, T y, T z) {
		return equality(
			or(x, or(y, z)), 
			or(or(x, y), z)
		);
	}

	@Law public boolean orIsCommutative(T x, T y) {
		return equality(
			or(x, y),
			or(y, x)
		);
	}

	// <X, 1=TRUE, ^> is a commutative monoid
	
	@Law public boolean oneIsUnitForAnd(T x) {
		return equality(
			and(TRUE(), x),
			x
		);
	}
	
	@Law public boolean andIsAssociative(T x, T y, T z) {
		return equality(
			and(x, and(y, z)), 
			and(and(x, y), z)
		);
	}

	@Law public boolean andIsCommutative(T x, T y) {
		return equality(
			and(x, y),
			and(y, x)
		);
	}
	
	// The absorptive laws, which imply v,^ are idempotent
	@Law public boolean andAbsorbsOr(T x, T y) {
		return equality(
			and(x, or(x, y)),
			x
		);
	}

	@Law public boolean orAbsorbsAnd(T x, T y) {
		return equality(
			or(x, and(x, y)),
			x
		);
	}
	
	// Some optional laws expressing other lattice properties - modularity, distributivity
	
	@OptionalLaw public boolean modular(T x, T y, T z) {
		// (x ^ z) v (y ^ z) = [(x ^ z) v y] ^ z
		return equality(
			or(and(x, z), and(y, z)),
			and(or(and(x, z), y), z)
		);
	}

	@OptionalLaw public boolean distributive(T x, T y, T z) {
		// x v (y ^ z) = (x v y) ^ (x v z)
		return equality(
			or(x, and(y, z)),
			and(or(x, y), or(x, z))
		);
	}
}

