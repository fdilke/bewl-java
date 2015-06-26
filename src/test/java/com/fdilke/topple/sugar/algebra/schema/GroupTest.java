package com.fdilke.topple.sugar.algebra.schema;

import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileBiArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.constantArrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;
import com.fdilke.topple.fsets.FiniteSets;
import com.fdilke.topple.fsets.FiniteSetsUtilities;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;

public class GroupTest {
	private FiniteSets topos = new FiniteSets();
	private FiniteSet trivialSet = FiniteSetsUtilities.compileDot("1");
	private FiniteSet symbolSet = FiniteSetsUtilities.compileDot("* & %");
	private Dot<Trivial> dotTrivial = topos.dot(trivialSet, Trivial.class);
	private Dot<Symbol> dotSymbol = topos.dot(symbolSet, Symbol.class);
		
    @Test public void trivialGroups() {
	    FiniteSetArrow unit = constantArrow(topos, trivialSet, "1");
		FiniteSetArrow inverse = compileArrow(trivialSet, trivialSet, "1:1");
    	ProductDiagram<FiniteSet, FiniteSetArrow> square = topos.square(trivialSet);
	    MultiArrow<FiniteSet, FiniteSetArrow> multiply = 
	    	compileBiArrow(square, trivialSet, trivialSet, trivialSet, "1:1:1");
	    	    
    	Group<Trivial> group = Group.build(topos, dotTrivial, unit, inverse, multiply);
    	assertTrue(group.isCommutative());
    	assertTrue(group.isIdem());
    }

    // Construct the symmetry group on 3 symbols
	@Ignore @Test public void lessTrivialGroup() {
		RightGroupAction<Scalar, Symbol> action = topos.automorphisms(Scalar.class, dotSymbol);
		Group<Scalar> group = action.group();
		@SuppressWarnings("unchecked")
		Dot<Scalar> dotCarrier = (Dot<Scalar>)group.dotCarrier();
    	assertEquals(6, topos.unwrap(dotCarrier).getUnderlyingSet().size());
    	assertFalse(group.isAbelian());
    	assertFalse(group.isIdem());
    }
    
	// Placeholder element class for left actions over a monoid
    public static class Trivial extends Element<Trivial> {	
    }
    public static class Symbol extends Element<Symbol> {	
    }
    public static class Scalar extends Element<Scalar> {	
    }
 }
