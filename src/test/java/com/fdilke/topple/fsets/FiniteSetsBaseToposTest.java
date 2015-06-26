package com.fdilke.topple.fsets;

import com.fdilke.topple.AbstractBaseToposTest;

/**
 * Test the topos of finite sets at the base topos level: we use an embedded set of fixtures specific to this topos
 */

public class FiniteSetsBaseToposTest extends AbstractBaseToposTest<FiniteSet, FiniteSetArrow> {
    public FiniteSetsBaseToposTest() {
    	super(new FiniteSetsFixtures());
    }
}
