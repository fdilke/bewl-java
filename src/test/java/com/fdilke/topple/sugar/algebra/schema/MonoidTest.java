package com.fdilke.topple.sugar.algebra.schema;

import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileBiArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.constantArrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;
import com.fdilke.topple.fsets.FiniteSets;
import com.fdilke.topple.fsets.FiniteSetsUtilities;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.Language;

@RunWith(Theories.class)
public class MonoidTest {
	public static @DataPoints Language[] values = Language.values(); // run tests with each language
	private FiniteSets topos = new FiniteSets();
	private FiniteSet trivialSet = FiniteSetsUtilities.compileDot("1");
	private FiniteSet fruitSet = FiniteSetsUtilities.compileDot("apple orange");
	private FiniteSet threeSet = FiniteSetsUtilities.compileDot("0 1 2");
	private FiniteSet countrySet = FiniteSetsUtilities.compileDot("USA UK Japan");
	private Dot<Trivial> dotTrivial = topos.dot(trivialSet, Trivial.class);
	private Dot<Fruit> dotFruit = topos.dot(fruitSet, Fruit.class);
	private Dot<Three> dotThree = topos.dot(threeSet, Three.class); 
	private Dot<Country> dotCountry = topos.dot(countrySet, Country.class); 
	
    @Test
    public void trivialMonoid() {
	    FiniteSetArrow unit = constantArrow(topos, trivialSet, "1");
    	ProductDiagram<FiniteSet, FiniteSetArrow> square = topos.square(trivialSet);
	    MultiArrow<FiniteSet, FiniteSetArrow> multiply = 
	    	compileBiArrow(square, trivialSet, trivialSet, trivialSet, "1:1:1");
	    	    
    	Monoid<Trivial> monoid = Monoid.build(topos, dotTrivial, unit, multiply);
    	assertTrue(monoid.isCommutative());
    	assertTrue(monoid.isIdem());
    }
  
    @Theory
    public void trivialEndomorphismMonoid(Language language) {
    	RightMonoidAction<Scalar, Trivial> action = topos.endomorphisms(Scalar.class, dotTrivial, language);
    	Monoid<Scalar> monoid = action.monoid();
    	@SuppressWarnings("unchecked")
		Dot<Scalar> dotCarrier = (Dot<Scalar>)monoid.dotCarrier();
    	FiniteSet carrier = topos.unwrap(dotCarrier); 
    	assertEquals(1, carrier.getUnderlyingSet().size());
    	assertTrue(monoid.isCommutative());
    	assertTrue(monoid.isIdem());
    } 

    @Theory
    public void almostTrivialMonoid(Language language) {
    	RightMonoidAction<Scalar, Fruit> action = topos.endomorphisms(Scalar.class, dotFruit, language);
    	Monoid<Scalar> monoid = action.monoid();
		@SuppressWarnings("unchecked")
		Dot<Scalar> dotCarrier = (Dot<Scalar>)monoid.dotCarrier();
    	FiniteSet carrier = topos.unwrap(dotCarrier); 
    	assertEquals(4, carrier.getUnderlyingSet().size());
    	assertFalse(monoid.isCommutative());
    	assertFalse(monoid.isIdem());
    }

	@Theory
	public void lessTrivialMonoid(Language language) {
		RightMonoidAction<Scalar, Country> action = topos.endomorphisms(Scalar.class, dotCountry, language);
		Monoid<Scalar> monoid = action.monoid();
		@SuppressWarnings("unchecked")
		Dot<Scalar> dotCarrier = (Dot<Scalar>)monoid.dotCarrier();
    	assertEquals(27, topos.unwrap(dotCarrier).getUnderlyingSet().size());
    	assertFalse(monoid.isCommutative());
    	assertFalse(monoid.isIdem());
    }
    
    @Test
    public void handDrawnCommutativeMonoid() {
        ProductDiagram<FiniteSet, FiniteSetArrow> square = topos.square(threeSet);
        MultiArrow<FiniteSet, FiniteSetArrow> multiply = compileBiArrow(square, 
        		threeSet, threeSet, threeSet, 
        		"0:0:0 0:1:1 0:2:2 " +
                "1:0:1 1:1:2 1:2:0 " +
                "2:0:2 2:1:0 2:2:1");
    	
    	FiniteSetArrow unit = constantArrow(topos, threeSet, "0");
    	
    	Monoid<Three> monoid = Monoid.build(topos, dotThree, unit, multiply);
    	assertTrue(monoid.isCommutative());
    	assertFalse(monoid.isIdem());
    }

    // Now with 0 as the unit again, but 1+x===1, 2+x===2
    @Test
    public void handDrawnNoncommutativeMonoid() {
    	ProductDiagram<FiniteSet, FiniteSetArrow> square = topos.square(threeSet);
        MultiArrow<FiniteSet, FiniteSetArrow> multiply = compileBiArrow(square, threeSet, threeSet, threeSet, 
        		"0:0:0 0:1:1 0:2:2 " +
                "1:0:1 1:1:1 1:2:1 " +
                "2:0:2 2:1:2 2:2:2");
       	FiniteSetArrow unit = FiniteSetsUtilities.constantArrow(topos, threeSet, "0");
            	
    	Monoid<Three> monoid = Monoid.build(topos, dotThree, unit, multiply);
    	assertFalse(monoid.isCommutative());
    	assertFalse(monoid.isIdem());
    }
     
	// Placeholder element classes
    public static class Trivial extends Element<Trivial> {	
    }

    public static class Three extends Element<Three> {	
    }

    public static class Fruit extends Element<Fruit> {	
    }

    public static class Country extends Element<Country> {	
    }
    
    public static class Scalar extends Element<Scalar> {	
    }
}
