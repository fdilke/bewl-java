package com.fdilke.topple.fsets.impl;

import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 10-Jan-2008
 * Time: 00:18:47
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSetProductDiagram implements ProductDiagram<FiniteSet, FiniteSetArrow> {
    private List<FiniteSet> _components;
    private FiniteSet _product;
    private List<FiniteSetArrow> _projections;

    public FiniteSetProductDiagram(List<FiniteSet> components) {
        _components = components;
        _product = FiniteSetsMultiplier.multiply(components);
        _projections = buildProjections();
    }

    public FiniteSet getProduct() {
        return _product;
    }

    public FiniteSetArrow multiplyArrows(FiniteSet source, List<FiniteSetArrow> finiteSetArrows) {
        // check the components are right
        if (finiteSetArrows.size() != _components.size()) {
            throw new IllegalArgumentException("Wrong number of arrows in productGetDiagram");
        }
        for (int i = 0; i < finiteSetArrows.size(); i++) {
            FiniteSetArrow arrow = finiteSetArrows.get(i);
            if (arrow.getSource() != source) {
                throw new IllegalArgumentException("Wrong source for component arrow #" + (i + 1));
            }
            if (arrow.getTarget() != _components.get(i)) {
                throw new IllegalArgumentException("Wrong target for component arrow #" + (i + 1));
            }
        }
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object key : source.getUnderlyingSet()) {
            Object[] array = new Object[finiteSetArrows.size()];
            for (int i = 0; i < finiteSetArrows.size(); i++) {
                FiniteSetArrow arrow = finiteSetArrows.get(i);
                array[i] = arrow.getUnderlyingMap().get(key);
            }
            // we can't add this array - need to find the equivalent one in the productGetDiagram
            Object[] equivalentArray = FiniteSetsMultiplier.findEquivalentArray(_product, array);
            map.put(key, equivalentArray);
        }
        return new FiniteSetArrow(source, _product, map);
    }

    public List<FiniteSetArrow> getProjections() {
        return _projections;
    }

    private List<FiniteSetArrow> buildProjections() {
        List<FiniteSetArrow> projections = new ArrayList<FiniteSetArrow>();
        for (int i = 0; i < _components.size(); i++) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            for (Object productObject : _product.getUnderlyingSet()) {
                Object[] productArray = (Object[]) productObject;
                map.put(productObject, productArray[i]);
            }
            FiniteSetArrow projection = new FiniteSetArrow(_product, _components.get(i), map);
            projections.add(projection);
        }
        return projections;
    }
}
