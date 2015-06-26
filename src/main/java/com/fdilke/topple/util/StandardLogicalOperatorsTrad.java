package com.fdilke.topple.util;

import static com.fdilke.topple.BaseTopos.multiply;

import com.fdilke.topple.BaseTopos;
import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.diagrams.EqualizerDiagram;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.PullbackDiagram;
import com.fdilke.topple.diagrams.SubobjectClassifier;
import com.fdilke.topple.sugar.Topos;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 24-Feb-2008
 * Time: 20:40:51
 * To change this template use File | Settings | File Templates.
 */
public class StandardLogicalOperatorsTrad<
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>
> {
    private final ProductDiagram<DOT, ARROW> omegaSquared;
    private final MultiArrow<DOT, ARROW> and;
    private final MultiArrow<DOT, ARROW> or;
    private final MultiArrow<DOT, ARROW> implies;
    private final ARROW trueArrow;
    private final ARROW falseArrow;

    public StandardLogicalOperatorsTrad(Topos<DOT, ARROW> topos) {
        SubobjectClassifier<DOT, ARROW> classifier = topos.getSubobjectClassifier();
        ARROW truth = classifier.getTruth();
        DOT terminator = truth.getSource();
        DOT omega = truth.getTarget();
        omegaSquared = topos.product(omega, omega);

        ARROW truthSquared = multiply(omegaSquared, terminator, truth, truth);
        PullbackDiagram<DOT, ARROW> pullbackT2 = classifier.pullbackMonic(truthSquared);
        ARROW andArrow = pullbackT2.getEast(); // characteristic map
        and = new MultiArrow<DOT, ARROW>(omegaSquared, andArrow);

        EqualizerDiagram<DOT, ARROW> lessThanEqualizer = topos.equalizer(andArrow, omegaSquared.getProjections().get(0));
        ARROW lessThanMonic = lessThanEqualizer.getEqualizer();
        PullbackDiagram<DOT, ARROW> pullbackLessEq = classifier.pullbackMonic(lessThanMonic);
        ARROW impliesArrow = pullbackLessEq.getEast(); // characteristic map
        implies = new MultiArrow<DOT, ARROW>(omegaSquared, impliesArrow);
        
        trueArrow = classifier.getTruth();
        
        // build the quantifier "forAllOmega", which maps omega^omega to omega
        ARROW omegaTrue = trueArrow.compose(topos.getTerminatorDiagram().getConstantArrow(omega));
        ExponentialDiagram<DOT, ARROW> omegaOmegaDiagram = topos.getExponentialDiagram(omega, omega);
        ARROW forAllOmega = classifier.pullbackMonic(topos.getName(omegaTrue, omegaOmegaDiagram)).getEast();
        
        falseArrow = forAllOmega.compose(topos.getName(omega.getIdentity(), omegaOmegaDiagram));
        
        // Now a v b = for all w: ((a=>w) ^ (b=>w)) => w
        // Construct this by transposing the map ((a, b), w) => above formula, then composing with the quantifier
        ProductDiagram<DOT, ARROW> omegaCubed = topos.product(omegaSquared.getProduct(), omega);
        // Map this to (a, w) and (b, w) 
        ARROW map_w = omegaCubed.getProjections().get(1);
		ARROW map_a_w = omegaSquared.multiplyArrows(omegaCubed.getProduct(), BaseTopos.pair(
        		omegaSquared.getProjections().get(0).compose(omegaCubed.getProjections().get(0)), 
        		map_w
        ));
        ARROW map_b_w = omegaSquared.multiplyArrows(omegaCubed.getProduct(), BaseTopos.pair(
        		omegaSquared.getProjections().get(1).compose(omegaCubed.getProjections().get(0)), 
        		map_w
        ));
        ARROW map_subformula = andArrow.compose(omegaSquared.multiplyArrows(omegaCubed.getProduct(), BaseTopos.pair(
        		impliesArrow.compose(map_a_w),
        		impliesArrow.compose(map_b_w)
        )));
        ARROW map_formula = impliesArrow.compose(omegaSquared.multiplyArrows(omegaCubed.getProduct(), BaseTopos.pair(
        		map_subformula,
        		map_w
        )));
        MultiArrow<DOT, ARROW> formulaArrow = new MultiArrow<DOT, ARROW>(omegaCubed, map_formula);
        ARROW orArrow = forAllOmega.compose(omegaOmegaDiagram.getTranspose(formulaArrow));
        or = new MultiArrow<DOT, ARROW>(omegaSquared, orArrow);
    }
    
    public MultiArrow<DOT, ARROW> getAnd() {
        return and;
    }
    
    public MultiArrow<DOT, ARROW> getOr() {
        return or;
    }

    public MultiArrow<DOT, ARROW> getImplies() {
        return implies;
    }

    public ARROW getTrue() {
        return trueArrow; 
    }

    public ARROW getFalse() {
        return falseArrow; 
    }
    
    public ProductDiagram<DOT, ARROW> getOmegaSquared() {
    	return omegaSquared;
    }
}
