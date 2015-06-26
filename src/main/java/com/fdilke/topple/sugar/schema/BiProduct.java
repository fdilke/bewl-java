package com.fdilke.topple.sugar.schema;

import com.fdilke.topple.sugar.Element;

public abstract class BiProduct<LEFT extends Element<LEFT>, RIGHT extends Element<RIGHT>> 
	extends Element<BiProduct<LEFT, RIGHT>>{

	public abstract LEFT left();
    public abstract RIGHT right();
}
