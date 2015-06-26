package com.fdilke.topple;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 07-Jan-2008
 * Time: 22:00:39
 * To change this template use File | Settings | File Templates.
 */
public interface ToposDot<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public ARROW getIdentity();
}
