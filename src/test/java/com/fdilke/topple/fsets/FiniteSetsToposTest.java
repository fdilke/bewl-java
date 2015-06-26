package com.fdilke.topple.fsets;

import com.fdilke.topple.AbstractToposTest;

/**
 * Test the topos of finite sets at the topos level: use the same embedded fixtures
 */

public class FiniteSetsToposTest extends AbstractToposTest<FiniteSet, FiniteSetArrow> {
    public FiniteSetsToposTest() {
    	super(new FiniteSetsFixtures());
    }
}
