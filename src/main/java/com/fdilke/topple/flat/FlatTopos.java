package com.fdilke.topple.flat;

import java.util.List;

/**
 * An API for a topos that doesn't include embedded types (generic type parameters)
 * and could use RPC, i.e. work with a "topos service" on a remote machine. Maybe written in Erlang?
 */

public interface FlatTopos {
    // Basic categorical operations
    public boolean equalArrows(ArrowRef arrowRef1, ArrowRef arrowRef2);
    public ArrowRef getIdentity(DotRef dotRef);
    public ArrowRef compose(ArrowRef preArrowRef, ArrowRef postArrowRef);

    // A flattened version of the "product diagram" API
    public ProductRef productGetDiagram(List<DotRef> components);
    public DotRef productGetDot(ProductRef productRef);
    public ArrowRef productMultiplyArrows(ProductRef refProduct, DotRef refSource, List<ArrowRef> arrows);
    public List<ArrowRef> productGetProjections(ProductRef productRef);

    // A flattened version of the terminator API
    public DotRef unitGetRef();
    public ArrowRef unitGetConstantArrow(DotRef refSource);

    // A flattened version of the "getExponentialDiagram diagram" API
    public ExponentialRef exponentialGetDiagram(DotRef refExponent, DotRef refIndex);
    public DotRef exponentialGetDot(ExponentialRef refExponential);
    public ArrowRef exponentialEvaluate(ExponentialRef refExponential, ArrowRef refFunction, ArrowRef refArgument);
    public ArrowRef exponentialTranspose(ExponentialRef refExponential, ProductRef refProduct, ArrowRef refMultiArrow);
 }
