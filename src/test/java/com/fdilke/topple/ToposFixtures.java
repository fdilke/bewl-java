package com.fdilke.topple;

import com.fdilke.topple.diagrams.EqualizerSituation;
import com.fdilke.topple.sugar.Topos;

public interface ToposFixtures<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
	Topos<DOT, ARROW> getTopos();

	DOT dotFoo();
	DOT dotBar();
	DOT dotBaz();
	ARROW arrowFooToBar();
	ARROW arrowBarToBaz();
	ARROW arrowBazToBar();
	ARROW arrowFooToBaz();
	ARROW arrowBazToBub();
	MultiArrow<DOT, ARROW> arrowFooBarToBaz();
	ARROW arrowMonicFooToBar();	
	ARROW arrowBubToFoo();
	EqualizerSituation<DOT, ARROW> equalizerSituation();

	ARROW arrowFooToFoo();
}
