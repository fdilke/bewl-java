package com.fdilke.topple.sugar.dots;

import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.schema.Truth;

public interface DotTruth extends Dot<Truth>{
	
	// take the characteristic map of a monic
	<S extends Element<S>, T extends Element<T>>
	Arrow<T, Truth> characteristic(Arrow<S, T> monic);
}
