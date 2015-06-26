package com.fdilke.topple.sugar.dots;

import java.util.List;

import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.schema.Product;

public interface DotProduct extends Dot<Product>{
	
	// multiply up a bunch of arrows in the context of a product
	<SRC extends Element<SRC>>
	Arrow<SRC, Product> multiply(Dot<SRC> dotSrc, List<Arrow<SRC, ?>> arrows);
	// same, with elements
	Product tuple(List<Element<?>> elements);
	
	// get the projections into the components
	<COMPONENT extends Element<COMPONENT>>
	Arrow<Product, COMPONENT> projection(int index, Dot<COMPONENT> component);
}
