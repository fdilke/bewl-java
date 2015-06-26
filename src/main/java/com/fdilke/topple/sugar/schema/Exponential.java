package com.fdilke.topple.sugar.schema;

import com.fdilke.topple.sugar.Element;

public abstract class Exponential<SRC extends Element<SRC>, TGT extends Element<TGT>> 
extends Element<Exponential<SRC, TGT>>{

	public abstract TGT evaluate(SRC src); 
}
