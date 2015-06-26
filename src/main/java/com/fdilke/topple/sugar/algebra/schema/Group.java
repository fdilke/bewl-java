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
import com.fdilke.topple.sugar.algebra.universal.AlgebraBuilder;
import com.fdilke.topple.sugar.algebra.universal.Axioms;
import com.fdilke.topple.sugar.algebra.universal.Law;
import com.fdilke.topple.sugar.algebra.universal.Operator;
import com.fdilke.topple.sugar.schema.Product;

// A monoid with some extra axioms to make multiplication invertible
@Axioms
public abstract class Group<T extends Element<T>> extends Monoid<T> {
	@Operator public abstract T inverse(T x);
		
	@Law public boolean leftInverse(T x) {
		return equality(
			multiply(inverse(x), x),			
			unit()
		);
	}

	// Alias the word 'commutative' for monoids that happen to be groups
	public boolean isAbelian() {
		return isCommutative();
	}
	
	public static <
	DOT extends ToposDot<DOT, ARROW>,
	ARROW extends ToposArrow<DOT, ARROW>,
	ELEMENT extends Element<ELEMENT>
> Group<ELEMENT> build(Topos<DOT, ARROW> topos,
		Dot<ELEMENT> dotElement,
		ARROW unitArrow, 
		ARROW inverseArrow, 
		MultiArrow<DOT, ARROW> multiplyArrow) {

		Dot<Product> dotSquare = topos.product(BaseTopos.<Dot<?>>pair(dotElement, dotElement));
		Arrow<Product, ELEMENT> unit = topos.nullaryOp(unitArrow, dotElement);
		Arrow<Product, ELEMENT> inverse = topos.unaryOp(inverseArrow, dotElement);
		Arrow<Product, ELEMENT> multiply = topos.multiArrow(multiplyArrow, dotSquare, dotElement);
	
	    Map<String, Arrow<Product, ELEMENT>> opMap = new HashMap<String, Arrow<Product, ELEMENT>>();
		opMap.put("unit", unit);
		opMap.put("inverse", inverse);
		opMap.put("multiply", multiply);
		
		@SuppressWarnings("unchecked") Class<? extends Group<ELEMENT>>
		axiomsClass = (Class<? extends Group<ELEMENT>>) (Object) Group.class;
	
		Group<ELEMENT> algebra = AlgebraBuilder.build(topos, dotElement, axiomsClass, opMap);
		return algebra;
	}
}
