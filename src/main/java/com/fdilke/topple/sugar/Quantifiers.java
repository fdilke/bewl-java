package com.fdilke.topple.sugar;

import com.fdilke.topple.sugar.dots.DotExponential;
import com.fdilke.topple.sugar.schema.Exponential;
import com.fdilke.topple.sugar.schema.Truth;

public interface Quantifiers<X extends Element<X>> {

	public Arrow<Exponential<X, Truth>, Truth> forAll();

	public DotExponential<X, Truth> power();
}
