package com.fdilke.topple.sugar.dots;

import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.schema.Unit;

public interface DotUnit extends Dot<Unit>{
	
	// get the constant arrow from any dot
	<X extends Element<X>>
	Arrow<X, Unit> constant(Dot<X> dotX);
}
