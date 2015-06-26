package com.fdilke.topple.sugar.dots;

import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.schema.BiProduct;

public interface DotBiProduct<LEFT extends Element<LEFT>, RIGHT extends Element<RIGHT>>
	extends Dot<BiProduct<LEFT, RIGHT>>{

	// Multiply a pair of arrows of the right type
	BiProduct<LEFT, RIGHT> pair(LEFT left, RIGHT right);
	
	// The left and right projections
	Arrow<BiProduct<LEFT, RIGHT>, LEFT> leftProjection();
	Arrow<BiProduct<LEFT, RIGHT>, RIGHT> rightProjection();
}
