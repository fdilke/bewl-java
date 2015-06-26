package com.fdilke.topple.fsets;

import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileBiArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileDot;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposFixtures;
import com.fdilke.topple.diagrams.EqualizerSituation;
import com.fdilke.topple.sugar.Topos;

public class FiniteSetsFixtures implements ToposFixtures<FiniteSet, FiniteSetArrow> {
	private final FiniteSets topos = new FiniteSets();
    private final FiniteSet fruitSet = compileDot("apple orange banana");
    private final FiniteSet fourSet = compileDot("1 2 3 4");
    private final FiniteSet wordSet = compileDot("foo bar baz");
    private final FiniteSet symbolSet = compileDot("* & @ %");
    private final FiniteSetArrow arrowFruit2Four = compileArrow(fruitSet, fourSet, "apple:1 orange:2 banana:3");
    private final FiniteSetArrow arrowFruit2Fruit = 
    	compileArrow(fruitSet, fruitSet, "apple:orange orange:banana banana:apple");
    private final FiniteSetArrow arrowFour2Words = compileArrow(fourSet, wordSet, "1:foo 2:foo 3:bar 4:baz");
    private final FiniteSetArrow arrowWords2Four = compileArrow(wordSet, fourSet, "foo:1 bar:3 baz:4");
    private final FiniteSetArrow arrowFruit2Words = compileArrow(fruitSet, wordSet, "apple:foo orange:foo banana:bar");
    private final FiniteSetArrow arrowWords2Symbols = compileArrow(wordSet, symbolSet, "foo:@ bar:* baz:%");
    private final MultiArrow<FiniteSet, FiniteSetArrow> arrowFooBar2Baz = compileBiArrow(fruitSet, fourSet, wordSet, 
    		"apple:1:foo apple:2:baz apple:3:foo apple:4:bar " +
            "orange:1:baz orange:2:baz orange:3:foo orange:4:bar " +
            "banana:1:foo banana:2:bar banana:3:bar banana:4:baz");
    private final FiniteSetArrow arrowSymbolToFruit = compileArrow(symbolSet, fruitSet, "*:apple &:orange @:banana %:banana");
    private final FiniteSetArrow arrowMonicFruit2Four = compileArrow(fruitSet, fourSet, "apple:1 orange:3 banana:4");
	private final EqualizerSituation<FiniteSet, FiniteSetArrow> equalizerSituation = 
		new EqualizerSituation<FiniteSet, FiniteSetArrow>( 
        	FiniteSetsUtilities.compileArrow(fruitSet, fourSet, "apple:1 orange:2 banana:2"),
        	FiniteSetsUtilities.compileArrow(fourSet, wordSet,  "1:foo 2:baz 3:baz 4:foo"),
        	FiniteSetsUtilities.compileArrow(fourSet, wordSet,  "1:foo 2:baz 3:foo 4:bar")
        );
 
   	public Topos<FiniteSet, FiniteSetArrow> getTopos() {
		return topos;
	}
	
	public FiniteSet dotFoo() {
		return fruitSet;
	}

	public FiniteSet dotBar() {
		return fourSet;
	}
	
	public FiniteSet dotBaz() {
		return wordSet;
	}
	
	public FiniteSetArrow arrowFooToBar() {
		return arrowFruit2Four;
	}
	
	public FiniteSetArrow arrowFooToFoo() {
		return arrowFruit2Fruit;
	}
		
	public FiniteSetArrow arrowBarToBaz() {
		return arrowFour2Words;
	}

	public FiniteSetArrow arrowBazToBar() {
		return arrowWords2Four;
	}

	public FiniteSetArrow arrowFooToBaz() {
		return arrowFruit2Words;
	}

	public FiniteSetArrow arrowBazToBub() {
		return arrowWords2Symbols;
	}
	
	public MultiArrow<FiniteSet, FiniteSetArrow> arrowFooBarToBaz() {
	       return arrowFooBar2Baz; 
	}

	public FiniteSetArrow arrowMonicFooToBar() {
		return arrowMonicFruit2Four;
	}
	
	public FiniteSetArrow arrowBubToFoo() {
		return arrowSymbolToFruit;
	}
	
	public EqualizerSituation<FiniteSet, FiniteSetArrow> equalizerSituation() {
		return equalizerSituation;
	}
}

