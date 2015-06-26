package com.fdilke.topple.fsets.impl;

import com.fdilke.topple.diagrams.PullbackDiagram;
import com.fdilke.topple.diagrams.SubobjectClassifier;
import com.fdilke.topple.diagrams.TerminatorDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 03-Feb-2008
 * Time: 21:46:16
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSetsSubobjectClassifier implements SubobjectClassifier<FiniteSet, FiniteSetArrow> {
    private static final Boolean TRUE = true;
    private static final Boolean FALSE = false;
    private static final FiniteSet _omega = new FiniteSet(new HashSet<Object>(Arrays.asList(TRUE, FALSE)));
    private final FiniteSetArrow _truth;
    private final TerminatorDiagram<FiniteSet, FiniteSetArrow> _terminatorDiagram;

    public FiniteSetsSubobjectClassifier(TerminatorDiagram<FiniteSet, FiniteSetArrow> terminatorDiagram) {
        _terminatorDiagram = terminatorDiagram;
        FiniteSet terminator = terminatorDiagram.getTerminator();
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object source : terminator.getUnderlyingSet()) {
            map.put(source, TRUE);
        }
        _truth = new FiniteSetArrow(terminator, _omega, map);
    }

    public FiniteSetArrow getTruth() {
        return _truth;
    }

    public PullbackDiagram<FiniteSet, FiniteSetArrow> pullbackMonic(FiniteSetArrow monic) {
        return new CharacteristicMapPullbackDiagram(monic);
    }

    private class CharacteristicMapPullbackDiagram implements PullbackDiagram<FiniteSet, FiniteSetArrow> {
        private final FiniteSetArrow _monic;
        private final FiniteSetArrow _characteristicMap;
        private final FiniteSetArrow _constantMap;

        public CharacteristicMapPullbackDiagram(FiniteSetArrow monic) {
            _monic = monic;
            _characteristicMap = buildCharacteristicMap();
            _constantMap = _terminatorDiagram.getConstantArrow(_monic.getSource());
        }

        private FiniteSetArrow buildCharacteristicMap() {
            FiniteSet target = _monic.getTarget();
            Map<Object, Object> chiMap = new HashMap<Object, Object>();
            for (Object targetObject : target.getUnderlyingSet()) {
                chiMap.put(targetObject, FALSE);
            }
            // Then overwrite that for the targets that are images of something in the source
            Map<Object, Object> monicMap = _monic.getUnderlyingMap();
            for (Object sourceObject : _monic.getSource().getUnderlyingSet()) {
                Object targetObject = monicMap.get(sourceObject);
                chiMap.put(targetObject, TRUE);
            }
            return new FiniteSetArrow(target, _omega, chiMap);
        }

        public FiniteSetArrow getNorth() {
            return _monic;
        }

        public FiniteSetArrow getSouth() {
            return _truth;
        }

        public FiniteSetArrow getWest() {
            return _constantMap;
        }

        public FiniteSetArrow getEast() {
            return _characteristicMap;
        }

        public FiniteSetArrow factorize(List<FiniteSetArrow> commutingArrows) {
            // There should be 2 arrows, mapping the same object to (monicTarget) and 1, respectively
            if (commutingArrows.size() != 2) {
                throw new IllegalArgumentException("wrong number of arrows");
            }
            FiniteSet source = commutingArrows.get(0).getSource();
            if (source != commutingArrows.get(1).getSource()) {
                throw new IllegalArgumentException("arrows should have common source");
            }
            if (commutingArrows.get(0).getTarget() != _monic.getTarget()) {
                throw new IllegalArgumentException("wrong target for first commuting arrow");
            }
            if (commutingArrows.get(1).getTarget() != _terminatorDiagram.getTerminator()) {
                throw new IllegalArgumentException("wrong target for second commuting arrow");
            }
            Map<Object, Object> arrowMap = commutingArrows.get(0).getUnderlyingMap();
            Map<Object, Object> map = new HashMap<Object, Object>();
            FiniteSet monicSource = _monic.getSource();
            for (Object sourceObject : source.getUnderlyingSet()) {
                Object image = arrowMap.get(sourceObject);
                Object preimage = findMonicPreimage(image);
                if (preimage == null) {
                    throw new IllegalArgumentException("cannot find preimage: commuting conditions violated");
                }
                map.put(sourceObject, preimage);
            }
            return new FiniteSetArrow(source, monicSource, map);
        }

        private Object findMonicPreimage(Object image) {
            Map<Object, Object> monicMap = _monic.getUnderlyingMap();
            for (Object testObject : _monic.getSource().getUnderlyingSet()) {
                if (monicMap.get(testObject) == image) {
                    return testObject;
                }
            }
            return null; // not found
        }
    }
}
