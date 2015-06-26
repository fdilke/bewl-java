package com.fdilke.topple.fsets;

import java.util.List;

import com.fdilke.topple.diagrams.EqualizerDiagram;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.StandardTerminatorDiagram;
import com.fdilke.topple.diagrams.SubobjectClassifier;
import com.fdilke.topple.diagrams.TerminatorDiagram;
import com.fdilke.topple.fsets.impl.FiniteSetEqualizerDiagram;
import com.fdilke.topple.fsets.impl.FiniteSetExponentialDiagram;
import com.fdilke.topple.fsets.impl.FiniteSetProductDiagram;
import com.fdilke.topple.fsets.impl.FiniteSetsSubobjectClassifier;
import com.fdilke.topple.sugar.Topos;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 07-Jan-2008
 * Time: 21:58:20
 * To change this template use File | Settings | File Templates.
 */

public class FiniteSets extends Topos<FiniteSet, FiniteSetArrow> {

    private final TerminatorDiagram<FiniteSet, FiniteSetArrow> _terminatorDiagram = new StandardTerminatorDiagram<FiniteSet, FiniteSetArrow>(this);
    private final SubobjectClassifier<FiniteSet, FiniteSetArrow> _subobjectClassifier = new FiniteSetsSubobjectClassifier(_terminatorDiagram);

    public ExponentialDiagram<FiniteSet, FiniteSetArrow> getExponentialDiagram(FiniteSet target, FiniteSet source) {
        return new FiniteSetExponentialDiagram(target, source);
    }

    public SubobjectClassifier<FiniteSet, FiniteSetArrow> getSubobjectClassifier() {
        return _subobjectClassifier;
    }

    public TerminatorDiagram<FiniteSet, FiniteSetArrow> getTerminatorDiagram() {
        return _terminatorDiagram;
    }

    public ProductDiagram<FiniteSet, FiniteSetArrow> getProductDiagram(List<FiniteSet> components) {
        return new FiniteSetProductDiagram(components);
    }

    public EqualizerDiagram<FiniteSet, FiniteSetArrow> equalizer(FiniteSetArrow f, FiniteSetArrow g) {
        return new FiniteSetEqualizerDiagram(f, g);
    }
}
