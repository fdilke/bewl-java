package com.fdilke.topple.sugar;

public interface Arrow<SRC extends Element<SRC>, TGT extends Element<TGT>> {
	TGT apply(SRC element);
	TGT asElement();
	
	<PRESRC extends Element<PRESRC>> Arrow<PRESRC, TGT> compose(Arrow<PRESRC, SRC> prefix);
	Dot<SRC> source();
	Dot<TGT> target();
}
