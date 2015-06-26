package com.fdilke.topple.sugar.schema;

import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;

public abstract class Product 
	extends Element<Product>{

	public abstract <E extends Element<E>> E component(int index, Dot<E> dot);
}
