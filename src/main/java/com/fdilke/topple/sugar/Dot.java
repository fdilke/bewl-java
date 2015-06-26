package com.fdilke.topple.sugar;

public interface Dot<E extends Element<E>> {
	Class<? extends E> boundClass();
	Arrow<E, E> getIdentity();
}
