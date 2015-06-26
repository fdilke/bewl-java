package com.fdilke.topple.fsets;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.SubobjectClassifier;
import com.fdilke.topple.fsets.impl.FiniteSetsMultiplier;
import com.fdilke.topple.fsets.impl.FiniteSetProductDiagram;
import com.fdilke.topple.sugar.Topos;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 24-Feb-2008
 * Time: 20:01:42
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSetsUtilities {
    public static FiniteSet compileDot(String dotCode) {
        String[] tokens = dotCode.split(" ", -1);
        Set<Object> set = new HashSet<Object>();
        for (String token : tokens) {
            if (set.contains(token)) {
                throw new IllegalArgumentException("Duplicate token \"" + token + "\"");
            }
            set.add(token.intern());
        }
        return new FiniteSet(set);
    }

    public static FiniteSetArrow compileArrow(FiniteSet source, FiniteSet target, String arrowCode) {
        String[] tokens = arrowCode.split(" ", -1);
        Map<Object, Object> map = new HashMap<Object, Object>();
        if (tokens.length < source.getUnderlyingSet().size()) {
            throw new IllegalArgumentException("Arrow map token does not define images for all operands");
        }
        for (String mapItem : tokens) {
            String[] mapTokens = mapItem.split(":", -1);
            if (mapTokens.length != 2) {
                throw new IllegalArgumentException("Arrow map token has too many components");
            }
            String mapSource = mapTokens[0].intern();
            String mapTarget = mapTokens[1].intern();
            if (!source.getUnderlyingSet().contains(mapSource)) {
                throw new IllegalArgumentException("Arrow map token has undefined source \"" + mapSource + "\"");
            }
            if (!target.getUnderlyingSet().contains(mapTarget)) {
                throw new IllegalArgumentException("Arrow map token has undefined target \"" + mapTarget + "\"");
            }
            map.put(mapSource, mapTarget);
        }
        return new FiniteSetArrow(source, target, map);
    }

    public static MultiArrow<FiniteSet, FiniteSetArrow> compileBiArrow(FiniteSet source1, FiniteSet source2, FiniteSet target, String biArrowCode) {
        List<FiniteSet> sources = Arrays.asList(source1, source2);
        FiniteSetProductDiagram productDiagram = new FiniteSetProductDiagram(sources);
        return compileBiArrow(productDiagram, source1, source2, target, biArrowCode);
    }
    
    public static MultiArrow<FiniteSet, FiniteSetArrow> compileBiArrow(
    		ProductDiagram<FiniteSet, FiniteSetArrow> productDiagram,
    		FiniteSet source1, FiniteSet source2, FiniteSet target, String biArrowCode) {
        FiniteSet productSet = productDiagram.getProduct();
        Map<Object, Object> projectionMap1 = productDiagram.getProjections().get(0).getUnderlyingMap();
        Map<Object, Object> projectionMap2 = productDiagram.getProjections().get(1).getUnderlyingMap();
        Map<Object, Map<Object, Object>> map2D = new HashMap<Object, Map<Object, Object>>();
        for (Object mapSource1 : source1.getUnderlyingSet()) {
            map2D.put(mapSource1, new HashMap<Object, Object>());
        }
        String[] tokens = biArrowCode.split(" ", -1);
        if (tokens.length < source1.getUnderlyingSet().size() * source2.getUnderlyingSet().size()) {
            throw new IllegalArgumentException("Arrow map token does not define images for all operands");
        }
        for (String mapItem : tokens) {
            String[] mapTokens = mapItem.split(":", -1);
            if (mapTokens.length != 3) {
                throw new IllegalArgumentException("Arrow map token has wrong number of components");
            }
            String mapSource1 = mapTokens[0].intern();
            String mapSource2 = mapTokens[1].intern();
            String mapTarget = mapTokens[2].intern();
            if (!source1.getUnderlyingSet().contains(mapSource1)) {
                throw new IllegalArgumentException("Arrow map token has undefined source1 \"" + mapSource1 + "\"");
            }
            if (!source2.getUnderlyingSet().contains(mapSource2)) {
                throw new IllegalArgumentException("Arrow map token has undefined source2 \"" + mapSource2 + "\"");
            }
            if (!target.getUnderlyingSet().contains(mapTarget)) {
                throw new IllegalArgumentException("Arrow map token has undefined target \"" + mapTarget + "\"");
            }
            map2D.get(mapSource1).put(mapSource2, mapTarget);
        }
        Map<Object, Object> productMap = new HashMap<Object, Object>();
        for (Object productObject : productSet.getUnderlyingSet()) {
            Object mapSource1 = projectionMap1.get(productObject);
            Object mapSource2 = projectionMap2.get(productObject);
            Object image = map2D.get(mapSource1).get(mapSource2);
            productMap.put(productObject, image);
        }
        FiniteSetArrow arrow = new FiniteSetArrow(productSet, target, productMap);
        return new MultiArrow<FiniteSet, FiniteSetArrow>(productDiagram, arrow);
    }

    public static Object applyArrow(FiniteSetArrow arrow, Object object) {
        return arrow.getUnderlyingMap().get(object);
    }

    public static Object applyMultiArrow(MultiArrow<FiniteSet, FiniteSetArrow> multiArrow, Object... objects) {
        FiniteSet product = multiArrow.getArrow().getSource();
        Object[] equivalentArray = FiniteSetsMultiplier.findEquivalentArray(product, objects);
        return applyArrow(multiArrow.getArrow(), equivalentArray);
    }
    
    public static FiniteSetArrow constantArrow(Topos<FiniteSet, FiniteSetArrow> topos, FiniteSet set, Object element) {
       	FiniteSet terminator = topos.getTerminator();
    	Object terminatorElement = new ArrayList<Object>(terminator.getUnderlyingSet()).get(0);
    	Map<Object, Object> unitMap = Collections.<Object, Object>singletonMap(terminatorElement, element);
    	return new FiniteSetArrow(terminator, set, unitMap);
     }

    // dissect the FiniteSet implementation of omega, truth, etc
    public static class Dissection {
        public final Object TRUE;
        public final Object FALSE;
        public final FiniteSet TERMINATOR;
        public final Object TERMINATOR_ELEMENT;

        public Dissection(Topos<FiniteSet, FiniteSetArrow> topos) {
            SubobjectClassifier<FiniteSet, FiniteSetArrow> classifier = topos.getSubobjectClassifier();
            FiniteSetArrow truth = classifier.getTruth();
            TERMINATOR = truth.getSource();
            FiniteSet omega = truth.getTarget();
            TERMINATOR_ELEMENT = TERMINATOR.getUnderlyingSet().toArray()[0];
            // pick out the "true" and "false" elements of the two-element set "omega"
            TRUE = truth.getUnderlyingMap().get(TERMINATOR_ELEMENT);
            Object theFalse = null;
            for (Object object : omega.getUnderlyingSet()) {
                if (object != TRUE) {
                    theFalse = object;
                }
            }
            FALSE = theFalse;
        }
    }
}
