package com.fdilke.topple.sugar;

import com.fdilke.topple.ToposArrow;

// base class that wraps an arrow to an object bound to class E

public class Element<E extends Element<E>> {
	public ToposArrow<?, ?> arrow;
	
	public void setArrow(ToposArrow<?, ?> arrow) {
		this.arrow = arrow;
	}

	public ToposArrow<?, ?> getArrow() {
		return arrow;
	}
	
	@Override public boolean equals(Object other) {
		if (other instanceof Element) {
			@SuppressWarnings("unchecked")
			Element<E> otherElement = (Element<E>) other;
			return arrow.equals(otherElement.arrow);
		} else {
			return false;
		}
	}
}





