package com.fdilke.topple;

import java.util.List;

import com.fdilke.topple.diagrams.EqualizerDiagram;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.SubobjectClassifier;
import com.fdilke.topple.diagrams.TerminatorDiagram;

public interface ToposAPI<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public ProductDiagram<DOT, ARROW> getProductDiagram(List<DOT> components);
    public EqualizerDiagram<DOT, ARROW> equalizer(ARROW f, ARROW g);
    public ExponentialDiagram<DOT, ARROW> getExponentialDiagram(DOT target, DOT source);
    public SubobjectClassifier<DOT, ARROW> getSubobjectClassifier();
    public TerminatorDiagram<DOT, ARROW> getTerminatorDiagram();
}
