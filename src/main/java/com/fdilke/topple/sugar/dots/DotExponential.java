package com.fdilke.topple.sugar.dots;

import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.schema.BiProduct;
import com.fdilke.topple.sugar.schema.Exponential;
import com.fdilke.topple.sugar.schema.Unit;

public interface DotExponential<SRC extends Element<SRC>, TGT extends Element<TGT>> 
	extends Dot<Exponential<SRC, TGT>> {
 
	<E extends Element<E>> 
	Arrow<E, Exponential<SRC, TGT>> transpose(
			Arrow<BiProduct<E, SRC>, TGT> wrappedMultiArrow);
	
	Arrow<Unit, Exponential<SRC, TGT>> name(Arrow<SRC, TGT> arrow);
}
