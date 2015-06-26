package com.fdilke.topple.fsets;

import com.fdilke.topple.ToposDot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 07-Jan-2008
 * Time: 22:05:14
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSet implements ToposDot<FiniteSet, FiniteSetArrow> {
    private Set<Object> _set;

    public FiniteSet(Set<Object> set) {
        _set = set;
    }

    public Set<Object> getUnderlyingSet() {
        return _set;
    }

    public FiniteSetArrow getIdentity() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object key : _set) {
            map.put(key, key);
        }
        return new FiniteSetArrow(this, this, map);
    }

    // Used for our "strict checking" function
    public boolean strictlyContains(Object value) {
        for (Object member : _set) {
            if (member == value) {
                return true; // found
            }
        }
        return false;  // not found
    }
    
    @Override public String toString() {
        String text = "{";
        for (Object value : _set) {
            text += FiniteSet.represent(value) + " ";
        }
        text = text.trim();
        text += "}";
        return text;
    	// return "{ " + _set + "}";
    }
    
    public static String represent(Object o) {
        if (o instanceof Object[]) {
            return Arrays.deepToString((Object[]) o);
        }
        return o.toString();
    }
}
