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

// Axioms for a "left monoid action", to test the concept of parameter spaces
@Axioms
public abstract class RightMonoidAction<
	SCALAR extends Element<SCALAR>, 
	VECTOR extends Element<VECTOR>
> implements Algebra<RightMonoidAction<SCALAR, VECTOR>> {
	@ParameterSpace public abstract Monoid<SCALAR> monoid();

	@Operator public abstract VECTOR scalarMultiply(VECTOR v, @Parameter("monoid") SCALAR s);		

	@Law public boolean scalarAssociative(@Parameter("monoid") SCALAR s, @Parameter("monoid") SCALAR t, VECTOR v) {
		return 
			scalarMultiply(scalarMultiply(v, s), t)       .equals( 
			scalarMultiply(v, monoid().multiply(s, t))
		);
	}
	
	public static <
	DOT extends ToposDot<DOT, ARROW>,
	ARROW extends ToposArrow<DOT, ARROW>,
	SCALAR extends Element<SCALAR>,
	VECTOR extends Element<VECTOR>	
    > RightMonoidAction<SCALAR, VECTOR> build(Topos<DOT, ARROW> topos,
		Dot<VECTOR> dotVector,
		Monoid<SCALAR> monoid, 
		MultiArrow<DOT, ARROW> scalarMultiply) {
		Dot<Product> dotVectorScalar = topos.product(BaseTopos.<Dot<?>>pair(dotVector, monoid.dotCarrier()));
	
	    Map<String, Arrow<Product, VECTOR>> opMap = new HashMap<String, Arrow<Product, VECTOR>>();
		opMap.put("scalarMultiply", topos.multiArrow(scalarMultiply, dotVectorScalar, dotVector));
	    Map<String, Algebra<?>> paramSpaces = new HashMap<String, Algebra<?>>();
	    paramSpaces.put("monoid", monoid);
		
		@SuppressWarnings("unchecked") Class<? extends RightMonoidAction<SCALAR, VECTOR>>
		axiomsClass = (Class<? extends RightMonoidAction<SCALAR, VECTOR>>) (Object) RightMonoidAction.class;
	
		return AlgebraBuilder.build(topos, dotVector, axiomsClass, opMap, paramSpaces);
	}
}
