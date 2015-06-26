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
import com.fdilke.topple.sugar.algebra.universal.Parameter;
import com.fdilke.topple.sugar.algebra.universal.ParameterSpace;
import com.fdilke.topple.sugar.schema.Product;

// Axioms for a "left monod action", to test the concept of parameter spaces
@Axioms
public abstract class LeftMonoidAction<
	SCALAR extends Element<SCALAR>, 
	VECTOR extends Element<VECTOR>
> implements Algebra<LeftMonoidAction<SCALAR, VECTOR>> {
	@ParameterSpace public abstract Monoid<SCALAR> monoid();

	@Operator public abstract VECTOR scalarMultiply(@Parameter("monoid") SCALAR s, VECTOR v);		

	@Law public boolean scalarAssociative(@Parameter("monoid") SCALAR s, @Parameter("monoid") SCALAR t, VECTOR v) {
		return 
			scalarMultiply(s, scalarMultiply(t, v))       .equals( 
			scalarMultiply(monoid().multiply(s, t), v)
		);
	}
	
	public static <
	DOT extends ToposDot<DOT, ARROW>,
	ARROW extends ToposArrow<DOT, ARROW>,
	SCALAR extends Element<SCALAR>,
	VECTOR extends Element<VECTOR>	
    > LeftMonoidAction<SCALAR, VECTOR> build(Topos<DOT, ARROW> topos,
	Dot<VECTOR> dotVector,
	Monoid<SCALAR> monoid, 
	MultiArrow<DOT, ARROW> scalarMultiply) {	
		Dot<Product> dotScalarVector = topos.product(BaseTopos.<Dot<?>>pair(monoid.dotCarrier(), dotVector));
	    Map<String, Arrow<Product, VECTOR>> opMap = new HashMap<String, Arrow<Product, VECTOR>>();
		opMap.put("scalarMultiply", topos.multiArrow(scalarMultiply, dotScalarVector, dotVector));
	    Map<String, Algebra<?>> paramSpaces = new HashMap<String, Algebra<?>>();
	    paramSpaces.put("monoid", monoid);
		
		@SuppressWarnings("unchecked") Class<? extends LeftMonoidAction<SCALAR, VECTOR>>
		axiomsClass = (Class<? extends LeftMonoidAction<SCALAR, VECTOR>>) (Object) LeftMonoidAction.class;
	
		return AlgebraBuilder.build(topos, dotVector, axiomsClass, opMap, paramSpaces);
	}
}
