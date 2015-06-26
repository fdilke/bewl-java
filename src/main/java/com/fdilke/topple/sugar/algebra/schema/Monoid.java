package com.fdilke.topple.sugar.algebra.schema;

import java.util.HashMap;
import java.util.Map;

import com.fdilke.topple.BaseTopos;
import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.Topos;
import com.fdilke.topple.sugar.algebra.universal.Algebra;
import com.fdilke.topple.sugar.algebra.universal.AlgebraBuilder;
import com.fdilke.topple.sugar.algebra.universal.Axioms;
import com.fdilke.topple.sugar.algebra.universal.Law;
import com.fdilke.topple.sugar.algebra.universal.Operator;
import com.fdilke.topple.sugar.algebra.universal.OptionalLaw;
import com.fdilke.topple.sugar.schema.Product;

// Axioms for a set with a associative binary operation
@Axioms
public abstract class Monoid<T extends Element<T>> implements Algebra<Monoid<T>> {
	@Operator public abstract T unit();
	@Operator public abstract T multiply(T x, T y);		
	
	@Law public boolean leftUnit(T x) {
		return equality(
			multiply(unit(), x),
			x
		);
	}

	@Law public boolean rightUnit(T x) {
		return equality(
			multiply(x, unit()),
			x
		);
	}
	
	@Law public boolean associative(T x, T y, T z) {
		return equality(
			multiply(x, multiply(y, z)), 
			multiply(multiply(x, y), z)
		);
	}

	@OptionalLaw public boolean commutative(T x, T y) {
		return equality(
			multiply(x, y),
			multiply(y, x)
		);
	}
	
	@OptionalLaw public boolean idem(T x) {
		return equality(
			multiply(x, x),
			unit()
		);
	}
	
	public static <
		DOT extends ToposDot<DOT, ARROW>,
		ARROW extends ToposArrow<DOT, ARROW>,
		ELEMENT extends Element<ELEMENT>
	> Monoid<ELEMENT> build(Topos<DOT, ARROW> topos,
			Dot<ELEMENT> dotElement,
			ARROW unitArrow, 
			MultiArrow<DOT, ARROW> multiply) {
		Dot<Product> dotSquare = topos.product(BaseTopos.<Dot<?>>pair(dotElement, dotElement));
	    Map<String, Arrow<Product, ELEMENT>> opMap = new HashMap<String, Arrow<Product, ELEMENT>>();
		opMap.put("unit", topos.nullaryOp(unitArrow, dotElement));
		opMap.put("multiply", topos.multiArrow(multiply, dotSquare, dotElement));
		
		@SuppressWarnings("unchecked") Class<? extends Monoid<ELEMENT>>
		axiomsClass = (Class<? extends Monoid<ELEMENT>>) (Object) Monoid.class;

		Monoid<ELEMENT> algebra = AlgebraBuilder.build(topos, dotElement, axiomsClass, opMap);
		return algebra;
	}
	
	public <
		DOT extends ToposDot<DOT, ARROW>,
		ARROW extends ToposArrow<DOT, ARROW>,
		VECTOR extends Element<VECTOR>
	> RightMonoidAction<T, VECTOR> action(Topos<DOT, ARROW> topos,
			Dot<VECTOR> dotVector,
			MultiArrow<DOT, ARROW> scalarMultiply) {
		return RightMonoidAction.<DOT, ARROW, T, VECTOR>build(topos, dotVector, this, scalarMultiply);
	}

	public boolean isCommutative() {
		return checkOptionalLaw("commutative");
	}
	
	public boolean isIdem() {
		return checkOptionalLaw("idem");
	}
}

