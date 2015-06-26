package com.fdilke.topple;

import static com.fdilke.topple.BaseTopos.twist;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.constantArrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;
import com.fdilke.topple.fsets.FiniteSetsFixtures;
import com.fdilke.topple.fsets.FiniteSetsUtilities;
import com.fdilke.topple.sugar.Topos;

/**
 * This is (necessarily?) very specific to sets, so I haven't separated out the fixtures.
 */

public class BaseToposTest {
    private ToposFixtures<FiniteSet, FiniteSetArrow> fixtures = new FiniteSetsFixtures();
    private Topos<FiniteSet, FiniteSetArrow> topos = fixtures.getTopos();
    private FiniteSetsUtilities.Dissection dissection = new FiniteSetsUtilities.Dissection(topos);
    
    @Test public void arrowsParallel() {
        FiniteSetArrow arrow = fixtures.arrowFooToBar();
        FiniteSetArrow arrow2 = fixtures.arrowMonicFooToBar();
        BaseTopos.checkParallelArrows(arrow, arrow2);
        FiniteSetArrow arrow3 = fixtures.arrowFooToBaz();
        try {
            BaseTopos.checkParallelArrows(arrow, arrow3);
        } catch(IllegalArgumentException ex) {
            // as it should be
        }
        FiniteSetArrow arrow4 = fixtures.arrowBazToBar();
        try {
            BaseTopos.checkParallelArrows(arrow, arrow4);
        } catch(IllegalArgumentException ex) {
            // as it should be
        }
    }
 
    // Test the construction of the name of an arrow f: A -> B (in the context of an getExponentialDiagram diagram of B^A)
    @Test
    public void name() {
    	FiniteSet src = fixtures.dotFoo();
    	FiniteSet tgt = fixtures.dotBar();
        FiniteSetArrow arrow = fixtures.arrowFooToBar();
        ExponentialDiagram<FiniteSet, FiniteSetArrow> exponential = topos.getExponentialDiagram(tgt, src);
        FiniteSetArrow name = topos.getName(arrow, exponential);
        assertTrue(dissection.TERMINATOR == name.getSource());
        Object function = name.getUnderlyingMap().get(dissection.TERMINATOR_ELEMENT);
        MultiArrow<FiniteSet, FiniteSetArrow> evaluation = exponential.getEvaluation();
        for (Object fruit : src.getUnderlyingSet()) {
            Object functionImage = FiniteSetsUtilities.applyMultiArrow(evaluation, function, fruit);
            Object functionImage2 = FiniteSetsUtilities.applyArrow(arrow, fruit);
            assertTrue(functionImage == functionImage2);
        }
    }
    
    @Test
    public void nullaryOp() {
    	FiniteSet foo = fixtures.dotFoo();
    	Object anElement = new ArrayList<Object>(foo.getUnderlyingSet()).get(0);
		FiniteSetArrow arrow = constantArrow(topos, foo, anElement);
		MultiArrow<FiniteSet, FiniteSetArrow> op = topos.nullaryOp(arrow);
		FiniteSet terminator = topos.getTerminatorDiagram().getTerminator();
		Object the1 = new ArrayList<Object>(terminator.getUnderlyingSet()).get(0);
		
		assertSame(terminator, op.getArrow().getSource());
		assertSame(foo,op.getArrow().getTarget());
		assertEquals(anElement, op.getArrow().getUnderlyingMap().get(the1));
    }
    
    @Test
    public void unaryOp() {
    	FiniteSetArrow arrow = fixtures.arrowFooToFoo();
		MultiArrow<FiniteSet, FiniteSetArrow> op = topos.unaryOp(arrow);
		assertEquals(1, op.getProductDiagram().getProjections().size());
		assertSame(arrow.getSource(), op.getProductDiagram().getProjections().get(0).getTarget());
		assertEquals(arrow.compose(op.getProductDiagram().getProjections().get(0)), op.getArrow());
    }
    
    // Test the "twist" arrow AxB -> BxA
    @Test
    public void twistArrow() {
    	FiniteSet foo = fixtures.dotFoo();
    	FiniteSet bar = fixtures.dotBar();
    	
    	ProductDiagram<FiniteSet, FiniteSetArrow> fruitByFour = topos.product(foo, bar);
    	ProductDiagram<FiniteSet, FiniteSetArrow> fourByFruit = topos.product(bar,foo);
    	
    	FiniteSetArrow twist = twist(fruitByFour, fourByFruit);
    	FiniteSetArrow twistInverse = twist(fourByFruit, fruitByFour);
    	
    	// Check these are mutually inverse isomorphisms of these products
    	assertSame(twist.getSource(), fruitByFour.getProduct());
    	assertSame(twist.getTarget(), fourByFruit.getProduct());
    	assertEquals(fruitByFour.getProduct().getIdentity(), twistInverse.compose(twist));
    	assertEquals(fourByFruit.getProduct().getIdentity(), twist.compose(twistInverse));
    }
}
