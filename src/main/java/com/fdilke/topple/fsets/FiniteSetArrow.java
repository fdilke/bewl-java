package com.fdilke.topple.fsets;

import java.util.HashMap;
import java.util.Map;

import com.fdilke.topple.ToposArrow;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 07-Jan-2008
 * Time: 22:05:36
 * Encapsulate an arrow between two finite sets.
 */

public class FiniteSetArrow implements ToposArrow<FiniteSet, FiniteSetArrow> {
    private FiniteSet _source;
    private FiniteSet _target;
    private Map<Object, Object> _map;

    public FiniteSetArrow(FiniteSet source, FiniteSet target, Map<Object, Object> map) {
        _source = source;
        _target = target;
        _map = map;
    }

    public FiniteSet getSource() {
        return _source;
    }

    public FiniteSet getTarget() {
        return _target;
    }

    public FiniteSetArrow compose(FiniteSetArrow other) {
        if (other.getTarget() != getSource()) {
            throw new IllegalArgumentException("Illegal composition: source and target don't match");
        }
        Map<Object, Object> map = new HashMap<Object, Object>();
        FiniteSet source = other.getSource();
        for (Object key : source.getUnderlyingSet()) {
            Object image1 = other.getUnderlyingMap().get(key);
            Object image2 = getUnderlyingMap().get(image1);
            map.put(key, image2);
        }
        return new FiniteSetArrow(source, getTarget(), map);
    }

    public Map<Object, Object> getUnderlyingMap() {
        return _map;
    }

    @Override
    public String toString() {
        String text = "{";
        for (Object key : _map.keySet()) {
            Object value = _map.get(key);
            text += FiniteSet.represent(key) + "=>" + FiniteSet.represent(value) + " ";
        }
        text = text.trim();
        text += "}";
        return text;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FiniteSetArrow that = (FiniteSetArrow) o;

        return (_source == that._source && _target == that._target &&
                _map.equals(that._map));
    }

    @Override public int hashCode() {
        int result;
        result = (_source != null ? _source.hashCode() : 0);
        result = 31 * result + (_target != null ? _target.hashCode() : 0);
        result = 31 * result + (_map != null ? _map.hashCode() : 0);
        return result;
    }
}
