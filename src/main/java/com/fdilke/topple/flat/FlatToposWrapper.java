package com.fdilke.topple.flat;

import static com.fdilke.topple.BaseTopos.pair;

import java.util.ArrayList;
import java.util.List;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.flat.impl.HashedExponentialDiagram;
import com.fdilke.topple.flat.impl.HashedProductDiagram;
import com.fdilke.topple.flat.impl.ToposHash;
import com.fdilke.topple.sugar.Topos;

/**
 * Expose a topos-like API in a "flat" form suitable for use in a wire protocol.
 * Also lets us manipulate the dots and arrows of a topos in "reference" form without caring what types they are.
 */
public class FlatToposWrapper<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>>
 implements FlatTopos {
    private final Topos<DOT, ARROW> _topos;
    private final ToposHash<DOT, ARROW> _hash;
    private final DotRef _unitRef;   // standard reference for the terminator

    public FlatToposWrapper(Topos<DOT, ARROW> topos) {
        _topos = topos;
        _hash = new ToposHash<DOT, ARROW>(topos);
        _unitRef = _hash.makeDotRef(_topos.getTerminatorDiagram().getTerminator());
    }

    public DotRef wrapDot(DOT dot) {
        return _hash.makeDotRef(dot);
    }

    public ArrowRef wrapArrow(ARROW arrow) {
        return _hash.makeArrowRef(arrow);
    }

    public DOT unwrapDot(DotRef dotRef) {
        return _hash.getDot(dotRef);
    }

    public ARROW unwrapArrow(ArrowRef arrowRef) {
        return _hash.getArrow(arrowRef);
    }

    public boolean equalArrows(ArrowRef arrowRef1, ArrowRef arrowRef2) {
        ARROW arrow1 = _hash.getArrow(arrowRef1);
        ARROW arrow2 = _hash.getArrow(arrowRef2);
        return arrow1.equals(arrow2);
    }

    public ArrowRef getIdentity(DotRef dotRef) {
        DOT dot = _hash.getDot(dotRef);
        return wrapArrow(dot.getIdentity());
    }

    public ArrowRef compose(ArrowRef preArrowRef, ArrowRef postArrowRef) {
        ARROW preArrow = _hash.getArrow(preArrowRef);
        ARROW postArrow = _hash.getArrow(postArrowRef);
        ARROW product = preArrow.compose(postArrow);
        return wrapArrow(product);
    }

    public ProductRef productGetDiagram(List<DotRef> components) {
        return _hash.makeProduct(components);
    }

    public DotRef productGetDot(ProductRef productRef) {
        HashedProductDiagram<DOT, ARROW> hashedProductDiagram = _hash.getProductDiagram(productRef);
        return hashedProductDiagram.getProductDotRef();
    }

    // A convention here: It's OK to pass a null source ref provided it can be inferred (i.e. there are >0 arrows).
    public ArrowRef productMultiplyArrows(ProductRef productRef, DotRef sourceRef, List<ArrowRef> arrowRefs) {
        HashedProductDiagram<DOT, ARROW> hashedProductDiagram = _hash.getProductDiagram(productRef);
        ProductDiagram<DOT, ARROW> productDiagram = hashedProductDiagram.getBaseProduct();
        List<ARROW> arrows = new ArrayList<ARROW>();
        for (ArrowRef arrowRef : arrowRefs) {
            arrows.add(_hash.getArrow(arrowRef));
        }
        DOT source; // use inference rule above if the source isn't specified
        if (sourceRef == null && !arrowRefs.isEmpty()) {
            source = arrows.get(0).getSource();
        } else {
            source = _hash.getDot(sourceRef);
        }
        ARROW productArrow = productDiagram.multiplyArrows(source, arrows);
        return wrapArrow(productArrow);
    }

    public List<ArrowRef> productGetProjections(ProductRef productRef) {
        HashedProductDiagram<DOT, ARROW> hashedProductDiagram = _hash.getProductDiagram(productRef);
        return hashedProductDiagram.getProjectionsRef(); 
    }

    public ArrowRef unitGetConstantArrow(DotRef sourceRef) {
        DOT source = _hash.getDot(sourceRef);
        ARROW constantArrow = _topos.getTerminatorDiagram().getConstantArrow(source);
        return wrapArrow(constantArrow);
    }

    // A flattened version of the "exponential diagram" API
    public ExponentialRef exponentialGetDiagram(DotRef refIndex, DotRef refExponent) {
        return _hash.makeExponential(refIndex, refExponent);
    }

    public DotRef exponentialGetDot(ExponentialRef refExponential) {
        HashedExponentialDiagram<DOT, ARROW> hashedExponentialDiagram = _hash.getExponentialDiagram(refExponential);
        return hashedExponentialDiagram.getExponentialDotRef();
    }

    public ArrowRef exponentialEvaluate(ExponentialRef refExponential, ArrowRef refFunction, ArrowRef refArgument) {
        HashedExponentialDiagram<DOT, ARROW> hashedExponentialDiagram = _hash.getExponentialDiagram(refExponential);
        ARROW arrowFunction = _hash.getArrow(refFunction);
        ARROW arrowArgument = _hash.getArrow(refArgument);
        ExponentialDiagram<DOT, ARROW> baseExponential = hashedExponentialDiagram.getBaseExponential();
        ProductDiagram<DOT, ARROW> productDiagram = baseExponential.getEvaluation().getProductDiagram();
		List<ARROW> components = pair(arrowFunction, arrowArgument);
        ARROW productArrow = productDiagram.multiplyArrows(arrowFunction.getSource(), components);
        ARROW valueArrow = baseExponential.getEvaluation().getArrow().compose(productArrow);
        return wrapArrow(valueArrow);
    }

    public ArrowRef exponentialTranspose(ExponentialRef refExponential, ProductRef refProduct, ArrowRef refMultiArrow) {
        HashedExponentialDiagram<DOT, ARROW> hashedExponentialDiagram = _hash.getExponentialDiagram(refExponential);
        ARROW multiArrow = _hash.getArrow(refMultiArrow);
        ExponentialDiagram<DOT, ARROW> baseExponential = hashedExponentialDiagram.getBaseExponential();
        HashedProductDiagram<DOT, ARROW> hashedProductDiagram = _hash.getProductDiagram(refProduct);
        ProductDiagram<DOT, ARROW> xSource = hashedProductDiagram.getBaseProduct();
        MultiArrow<DOT, ARROW> multiArrow2 = new MultiArrow<DOT, ARROW>(xSource, multiArrow);
        ARROW transposeArrow = baseExponential.getTranspose(multiArrow2);
        return wrapArrow(transposeArrow);
    }
    
    public DotRef unitGetRef() {
        return _unitRef;
    }

	public static <DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> 
		FlatTopos wrap(Topos<DOT, ARROW> topos) {
		return new FlatToposWrapper<DOT, ARROW>(topos);
	}
}
