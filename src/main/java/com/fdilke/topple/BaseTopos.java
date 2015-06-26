package com.fdilke.topple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.TerminatorDiagram;

public abstract class BaseTopos<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> 
	implements ToposAPI<DOT, ARROW> {

    public static <DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>>
        void checkParallelArrows(ARROW f, ARROW g) {
        DOT source = f.getSource();
        DOT target = f.getTarget();
        if (source != g.getSource()) {
            throw new IllegalArgumentException("arrows not parallel: sources don't match");
        }
        if (target != g.getTarget()) {
            throw new IllegalArgumentException("arrows not parallel: targets don't match");
        }
    }

    // Express a constant arrow as an operator of arity 0
	public MultiArrow<DOT, ARROW> nullaryOp(ARROW arrow) {
		TerminatorDiagram<DOT, ARROW> tDgm = getTerminatorDiagram();
		if (arrow.getSource() != tDgm.getTerminator()) {
			throw new IllegalArgumentException("Cannot build nullary op from non-terminator-sourced arrow");
		}
		return new MultiArrow<DOT, ARROW>(tDgm, arrow);
	}

    // Express an arrow as an operator of arity 1
	public MultiArrow<DOT, ARROW> unaryOp(ARROW arrow) {
		List<DOT> listOfOne = Collections.singletonList(arrow.getSource());
		ProductDiagram<DOT, ARROW> productOfOne = getProductDiagram(listOfOne);
		return new MultiArrow<DOT, ARROW>(productOfOne, arrow.compose(productOfOne.getProjections().get(0)));
	}
	
    /*
     *  find the name of an arrow f:A -> B, constructed as the transpose of 1xA->A->B in the context of a
     *  specified getExponentialDiagram diagram B^A 
     */
    public ARROW getName(ARROW arrow, ExponentialDiagram<DOT, ARROW> exponential) {
        DOT source = arrow.getSource();
        DOT terminator = getTerminatorDiagram().getTerminator();
        ProductDiagram<DOT, ARROW> diagram1xA = getProductDiagram(pair(terminator, source));
        ARROW arrow1xAtoA = diagram1xA.getProjections().get(1);
        ARROW arrow1xAtoB = arrow.compose(arrow1xAtoA);
        MultiArrow<DOT, ARROW> multiarrow = new MultiArrow<DOT,ARROW>(diagram1xA, arrow1xAtoB);
        return exponential.getTranspose(multiarrow);
    }

    public static <T> List<T> pair(T a, T b) {
    	List<T> pair = new ArrayList<T>();
    	pair.add(a);
    	pair.add(b);
    	return pair;
    }
    
	public ProductDiagram<DOT, ARROW>
		product(DOT a,
				DOT b) {
		return getProductDiagram(pair(a, b));
	}
	
	/*
	 * Shortcut to get just the terminator object, without its structure
	 */
	public DOT getTerminator() {
		return getTerminatorDiagram().getTerminator();
	}
	
	/*
	 * Shortcut to get just the truth object, without its structure
	 */
	public DOT getOmega() {
		return getSubobjectClassifier().getTruth().getTarget();
	}

	public static <DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>>
	ARROW multiply(ProductDiagram<DOT, ARROW> product,
		DOT source,
		ARROW a,
		ARROW b) {
		return product.multiplyArrows(source, pair(a, b));
	}
	
	/*
	 * Construct the canonical "twist arrow" from AxB to BxA
	 */
	public static <DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> 
	ARROW twist(ProductDiagram<DOT, ARROW> aXb, ProductDiagram<DOT, ARROW> bXa) {
		return multiply(bXa, aXb.getProduct(), 
				aXb.getProjections().get(1), 
				aXb.getProjections().get(0)
			);
	}
	
    public ProductDiagram<DOT, ARROW> square(DOT dot) {
    	return product(dot, dot);
    }
    
    /*
     * Extract the "exponential dot" from a diagram, using the fact that evaluation maps C^B x B -> C
     */
    public static <DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> 
    	DOT exponentialDot(ExponentialDiagram<DOT, ARROW> exponential) {
    	return exponential.getEvaluation().getProductDiagram().getProjections().get(0).getTarget();
    }

    /*
     * Calculate the canonical isomorphism between two products with the same factors
     */
    public ARROW canonicalIso(
    		ProductDiagram<DOT, ARROW> productA, 
    		ProductDiagram<DOT, ARROW> productB) {
    	// Multiply the projections of one product over the other
    	List<ARROW> projections = productA.getProjections();
    	return productB.multiplyArrows(productA.getProduct(), projections);
    }
    
    /*
     * Calculate the canonical isomorphism between two exponentials with the same index & exponent
     */
    public ARROW canonicalIso(ExponentialDiagram<DOT, ARROW> expA,
    		ExponentialDiagram<DOT, ARROW> expB) {
    	// Transpose the evaluation of one exponential via the other
    	return expB.getTranspose(expA.getEvaluation());
    }
}