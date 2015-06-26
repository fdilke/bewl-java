package com.fdilke.topple.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposFixtures;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;
import com.fdilke.topple.fsets.FiniteSetsFixtures;
import com.fdilke.topple.fsets.FiniteSetsUtilities;
import com.fdilke.topple.sugar.Topos;

public class StandardLogicalOperatorsTradTest {
	private ToposFixtures<FiniteSet, FiniteSetArrow> fixtures = new FiniteSetsFixtures();
    private Topos<FiniteSet, FiniteSetArrow> topos = fixtures.getTopos();
    private FiniteSetsUtilities.Dissection dissection = new FiniteSetsUtilities.Dissection(topos);
    private final StandardLogicalOperatorsTrad<FiniteSet, FiniteSetArrow> operators 
    	= new StandardLogicalOperatorsTrad<FiniteSet, FiniteSetArrow>(topos);
	 
    // Make sure the "abstractly built" logical operations on Set are what they should be
    @Test
    public void logicalOperatorAnd() {
        MultiArrow<FiniteSet, FiniteSetArrow> and = operators.getAnd();
        List<FiniteSetArrow> projections = and.getProductDiagram().getProjections();
        assertEquals(2, projections.size());
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(and, dissection.TRUE, dissection.TRUE));
        assertTrue(dissection.FALSE == FiniteSetsUtilities.applyMultiArrow(and, dissection.TRUE, dissection.FALSE));
        assertTrue(dissection.FALSE == FiniteSetsUtilities.applyMultiArrow(and, dissection.FALSE, dissection.TRUE));
        assertTrue(dissection.FALSE == FiniteSetsUtilities.applyMultiArrow(and, dissection.FALSE, dissection.FALSE));
    }

    @Test
    public void logicalOperatorOr() {
        MultiArrow<FiniteSet, FiniteSetArrow> or = operators.getOr();
        List<FiniteSetArrow> projections = or.getProductDiagram().getProjections();
        assertEquals(2, projections.size());
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(or, dissection.TRUE,  dissection.TRUE));
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(or, dissection.TRUE,  dissection.FALSE));
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(or, dissection.FALSE, dissection.TRUE));
        assertTrue(dissection.FALSE== FiniteSetsUtilities.applyMultiArrow(or, dissection.FALSE, dissection.FALSE));
    }
    
    @Test
    public void logicalOperatorImplies() {
        MultiArrow<FiniteSet, FiniteSetArrow> implies = operators.getImplies();
        List<FiniteSetArrow> projections = implies.getProductDiagram().getProjections();
        assertEquals(2, projections.size());
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(implies, dissection.TRUE, dissection.TRUE));
        assertTrue(dissection.FALSE == FiniteSetsUtilities.applyMultiArrow(implies, dissection.TRUE, dissection.FALSE));
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(implies, dissection.FALSE, dissection.TRUE));
        assertTrue(dissection.TRUE == FiniteSetsUtilities.applyMultiArrow(implies, dissection.FALSE, dissection.FALSE));
    }
 
    @Test
    public void logicalOperatorTrue() {
        FiniteSetArrow TRUE = operators.getTrue();
        assertTrue(topos.getTerminator() == TRUE.getSource());
        List<Object> target = new ArrayList<Object>(TRUE.getUnderlyingMap().values());
        assertEquals(dissection.TRUE, target.get(0));
    }

    @Test
    public void logicalOperatorFalse() {
        FiniteSetArrow FALSE = operators.getFalse();
        assertTrue(topos.getTerminator() == FALSE.getSource());
        List<Object> target = new ArrayList<Object>(FALSE.getUnderlyingMap().values());
        assertEquals(dissection.FALSE, target.get(0));
    }
}
