package com.fdilke.topple;

import static com.fdilke.topple.BaseTopos.exponentialDot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fdilke.topple.diagrams.EqualizerDiagram;
import com.fdilke.topple.diagrams.EqualizerSituation;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.PullbackDiagram;
import com.fdilke.topple.diagrams.SubobjectClassifier;
import com.fdilke.topple.diagrams.TerminatorDiagram;

/**
 * Abstract tests for any topos at the base layer , given a set of 'test fixtures' (stock dots and arrows) for it
 */

public abstract class AbstractBaseToposTest<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
	private final ToposFixtures<DOT, ARROW> fixtures;
    private final BaseTopos<DOT, ARROW> _topos;
    
    public AbstractBaseToposTest(ToposFixtures<DOT, ARROW> fixtures) {
    	this.fixtures = fixtures;
        _topos = fixtures.getTopos();
    }

    @Test public void sourceAndTarget() {
    	ARROW f = fixtures.arrowFooToBar();
    	assertSame(fixtures.dotFoo(), f.getSource());
    	assertSame(fixtures.dotBar(), f.getTarget());
    }
    
    // Check the effect of pre- and post-multiplication by identities on the given arrow
    @Test public void testIdentity() {
    	ARROW f = fixtures.arrowFooToBar();
        DOT source = f.getSource();
        DOT target = f.getTarget();
        ARROW id_left = source.getIdentity();
        assertSends(source, id_left, source);
        ARROW id_right = target.getIdentity();
        assertSends(target, id_right, target);
        ARROW If = f.compose(id_left);
        assertTrue(f.equals(If));
        assertTrue(If.equals(f));
        ARROW fI = id_right.compose(f);
        assertTrue(f.equals(fI));
        assertTrue(fI.equals(f));
        // and for good measure:
        assertTrue(If.equals(fI));
        assertTrue(fI.equals(If));
    }

    // Given 2 composable arrows, check their product is what we think it is, 
    // and that they can't be composed the other way
    @Test public void testComposition() {
    	ARROW f = fixtures.arrowFooToBar();
        ARROW g = fixtures.arrowBarToBaz();
        ARROW expected_fg = fixtures.arrowFooToBaz();
 
        ARROW fg = g.compose(f);
        assertSends(f.getSource(), fg, g.getTarget());
        assertSends(f.getSource(), expected_fg, g.getTarget());
        assertEquals(expected_fg, fg);
        assertEquals(fg, expected_fg);
        try {
            f.compose(g);
            fail("Should not be able to compose arrows backwards");
        } catch (Exception ex) {
            // as expected
        }
    }

    // Given a chain of 3 composable arrows, check composition is associative
    @Test public void testAssociativity() {
        ARROW f = fixtures.arrowFooToBar();
        ARROW g = fixtures.arrowBarToBaz();
        ARROW h = fixtures.arrowBazToBub();
        ARROW gf = g.compose(f);
        ARROW hg = h.compose(g);
        ARROW h_gf = h.compose(gf);
        ARROW hg_f = hg.compose(f);
        assertEquals(hg_f, h_gf);
        assertEquals(h_gf, hg_f);
    }

    // Given two arrows with the same source, check we can multiply them 
    // and get an arrow to the product of their targets
    @Test public void testProductOf2() {
        ARROW p = fixtures.arrowFooToBar();
        ARROW q = fixtures.arrowFooToBaz();
    	
        DOT a = p.getSource();
        DOT b = p.getTarget();
        DOT c = q.getTarget();
        assertTrue(a == q.getSource());

        ProductDiagram<DOT, ARROW> diagram = _topos.product(b, c);
        List<ARROW> arrowComponents = new ArrayList<ARROW>();
        arrowComponents.add(p);
        arrowComponents.add(q);
        ARROW pxq = diagram.multiplyArrows(a, arrowComponents);
        List<ARROW> projections = diagram.getProjections();
        assertTrue(p.equals(projections.get(0).compose(pxq)));
        assertTrue(q.equals(projections.get(1).compose(pxq)));
    }
    
    @Test public void testCanonicalProductIso() {
    	DOT dot1 = fixtures.dotBar();
    	DOT dot2 = fixtures.dotFoo();
    	ProductDiagram<DOT, ARROW> productA = _topos.product(dot1, dot2);
    	ProductDiagram<DOT, ARROW> productB = _topos.product(dot1, dot2);
    	
    	ARROW isoAB = _topos.canonicalIso(productA, productB);
    	ARROW isoBA = _topos.canonicalIso(productB, productA);
    	
    	DOT dotA = productA.getProduct();
    	DOT dotB = productB.getProduct();

    	assertSame(isoAB.getSource(), dotA);
		assertSame(isoAB.getTarget(), dotB);
    	assertSame(isoBA.getSource(), dotB);
    	assertSame(isoBA.getTarget(), dotA);
    	
    	assertEquals(dotA.getIdentity(), isoBA.compose(isoAB));
    	assertEquals(dotB.getIdentity(), isoAB.compose(isoBA));
    }

    @Test public void testTerminator() {
        ARROW p = fixtures.arrowFooToBar();
        
        DOT a = p.getSource();
        DOT b = p.getTarget();
        TerminatorDiagram<DOT, ARROW> tDgm = _topos.getTerminatorDiagram();
        DOT terminator = tDgm.getTerminator();
        ARROW constantArrow_a = tDgm.getConstantArrow(a);
        assertTrue(a == constantArrow_a.getSource());
        assertTrue(terminator == constantArrow_a.getTarget());
        ARROW constantArrow_b = tDgm.getConstantArrow(b);
        assertEquals(constantArrow_a, constantArrow_b.compose(p));
    }

    // given a "biarrow" AxB --> C, check we can transpose it via the getExponentialDiagram
    @Test public void testExponential() {
    	MultiArrow<DOT, ARROW> biArrow = fixtures.arrowFooBarToBaz();
        List<ARROW> projections = biArrow.getProductDiagram().getProjections();
        assertTrue(projections.size() == 2);
        DOT a = projections.get(0).getTarget();
        DOT b = projections.get(1).getTarget();
        DOT c = biArrow.getArrow().getTarget();
        ExponentialDiagram<DOT, ARROW> exponential = _topos.getExponentialDiagram(c, b);
        // Check evaluation maps C^B x B -> C
        MultiArrow<DOT, ARROW> ev = exponential.getEvaluation();
        List<ARROW> expProjections = ev.getProductDiagram().getProjections();
        assertTrue(expProjections.size() == 2);
        assertTrue(expProjections.get(1).getTarget() == b);
        assertTrue(ev.getArrow().getTarget() == c);
        // and the universal property of evaluation
        ARROW transpose = exponential.getTranspose(biArrow); // check this maps A -> B^C
        assertTrue(transpose.getSource() == a);
        assertTrue(transpose.getTarget() == expProjections.get(0).getTarget()); // the exponent object itself
        // Next, construct the arrow: transpose x 1 : A x B -> C^B x B as the productGetDiagram of A x B -> A -> C^B and A x B -> B -> B
        ARROW x1 = transpose.compose(projections.get(0));
        ARROW x2 = projections.get(1);
        List<ARROW> multiplicands = new ArrayList<ARROW>();
        multiplicands.add(x1);
        multiplicands.add(x2);
        ARROW t_x_1 = ev.getProductDiagram().multiplyArrows(biArrow.getProductDiagram().getProduct(), multiplicands);
        assertTrue(biArrow.getArrow().equals(ev.getArrow().compose(t_x_1)));
    }
    
    @Test public void testCanonicalExponentialIso() {
    	DOT dot1 = fixtures.dotFoo();
    	DOT dot2 = fixtures.dotBar();
    
		ExponentialDiagram<DOT, ARROW> expA = _topos.getExponentialDiagram(dot1, dot2);
		ExponentialDiagram<DOT, ARROW> expB = _topos.getExponentialDiagram(dot1, dot2);
		
		ARROW isoAB = _topos.canonicalIso(expA, expB);
		ARROW isoBA = _topos.canonicalIso(expB, expA);
		
		DOT dotA = exponentialDot(expA);
		DOT dotB = exponentialDot(expB);
		
		assertSame(isoAB.getSource(), dotA);
		assertSame(isoAB.getTarget(), dotB);
		assertSame(isoBA.getSource(), dotB);
		assertSame(isoBA.getTarget(), dotA);
		
		assertEquals(dotA.getIdentity(), isoBA.compose(isoAB));
		assertEquals(dotB.getIdentity(), isoAB.compose(isoBA));
	}
   
    // Given a monic and an arrow to premultiply it, check we can factorize this out again via the subobject classifier and its induced pullback
    @Test public void testSubobjectClassifier() {
    	ARROW monicPrefix = fixtures.arrowBubToFoo();
    	ARROW monic = fixtures.arrowMonicFooToBar();
    	
        SubobjectClassifier<DOT, ARROW> classifier = _topos.getSubobjectClassifier();
        ARROW truth = classifier.getTruth();
        TerminatorDiagram<DOT, ARROW> tDgm = _topos.getTerminatorDiagram();
        DOT terminator = tDgm.getTerminator();
        assertTrue(terminator == truth.getSource());

        assertTrue(monicPrefix.getTarget() == monic.getSource());
        PullbackDiagram<DOT, ARROW> pullback = classifier.pullbackMonic(monic);

        // Verify that this really is a pullback diagram and can factor out the monic.
        ARROW north = pullback.getNorth();
        ARROW south = pullback.getSouth();
        ARROW west = pullback.getWest();
        ARROW east = pullback.getEast();
        assertTrue(north.getSource() == west.getSource());
        assertTrue(north.getTarget() == east.getSource());
        assertTrue(south.getSource() == west.getTarget());
        assertTrue(south.getTarget() == east.getTarget());
        assertTrue(east.compose(north).equals(south.compose(west)));
        // it's a commutative square: now, are the right bits in place?
        assertTrue(south == truth); // seems reasonable to insist on strict equality here
        assertTrue(north == monic);

        // pull back the composition...
        ARROW composition = monic.compose(monicPrefix);
        ARROW constant = tDgm.getConstantArrow(composition.getSource());
        // sanity check: just make sure it can be pulled back...
        assertTrue(east.compose(composition).equals(south.compose(constant)));
        List<ARROW> commutingArrows = new ArrayList<ARROW>();
        commutingArrows.add(composition);
        commutingArrows.add(constant);
        ARROW factor = pullback.factorize(commutingArrows);       // should have extract the original prefix arrow
        assertTrue(monicPrefix.equals(factor));
    }

    // Given an 'equalizer situation', check we can factorize through the equalizer 
    @Test public void testEqualizer() {
    	EqualizerSituation<DOT, ARROW> situation = fixtures.equalizerSituation();
    	ARROW r = situation.getR();
    	ARROW s = situation.getS();
    	ARROW t = situation.getT();
    	
        EqualizerDiagram<DOT, ARROW> diagram = _topos.equalizer(s, t);
        ARROW e = diagram.getEqualizer();
        assertEquals(s.compose(e), t.compose(e));
        ARROW q = diagram.factorize(situation);
        assertTrue(q.getSource() == r.getSource());
        assertTrue(q.getTarget() == e.getSource());
        assertTrue(r.equals(e.compose(q)));
    }

    private void assertSends(DOT a, ARROW f, DOT b) {
        assertTrue(f.getSource() == a);
        assertTrue(f.getTarget() == b);
    }
}
