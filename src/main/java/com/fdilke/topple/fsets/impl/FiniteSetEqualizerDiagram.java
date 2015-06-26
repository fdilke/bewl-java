package com.fdilke.topple.fsets.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fdilke.topple.BaseTopos;
import com.fdilke.topple.diagrams.EqualizerDiagram;
import com.fdilke.topple.diagrams.EqualizerSituation;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 14-Jan-2008
 * Time: 22:39:22
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSetEqualizerDiagram implements EqualizerDiagram<FiniteSet, FiniteSetArrow> {
    private final FiniteSetArrow _firstArrow;
    private final FiniteSetArrow _secondArrow;
    private final FiniteSetArrow _equalizer;
    private final FiniteSet _equalizerSource;

    public FiniteSetEqualizerDiagram(FiniteSetArrow f, FiniteSetArrow g) {
        _firstArrow = f;
        _secondArrow = g;
        BaseTopos.checkParallelArrows(f, g);
        Set<Object> equalizerSourceSet = new HashSet<Object>();
        Map<Object, Object> equalizerMap = new HashMap<Object, Object>();
        Map<Object, Object> fMap = f.getUnderlyingMap();
        Map<Object, Object> gMap = g.getUnderlyingMap();
        FiniteSet source = f.getSource();
        for (Object key : source.getUnderlyingSet()) {
            if (fMap.get(key) == gMap.get(key)) {
                equalizerSourceSet.add(key);
                equalizerMap.put(key, key);
            }
        }
        _equalizerSource = new FiniteSet(equalizerSourceSet);
        _equalizer = new FiniteSetArrow(_equalizerSource, source, equalizerMap);
    }

    public FiniteSetArrow getEqualizer() {
        return _equalizer;
    }

    public FiniteSetArrow factorize(EqualizerSituation<FiniteSet, FiniteSetArrow> situation) {
    	FiniteSetArrow r = situation.getR();
        FiniteSet source = r.getSource();
        if (situation.getS() != _firstArrow) {
        	throw new IllegalArgumentException("equalizer situation: 's' does not match");
        }
        if (situation.getT() != _secondArrow) {
        	throw new IllegalArgumentException("equalizer situation: 't' does not match");
        }
        Map<Object, Object> rMap = r.getUnderlyingMap();
        Map<Object, Object> factorMap = new HashMap<Object, Object>();
        for (Object key : source.getUnderlyingSet()) {
            Object image = rMap.get(key);
            factorMap.put(key, image);
        }
        return new FiniteSetArrow(source, _equalizerSource, factorMap);
    }
}
