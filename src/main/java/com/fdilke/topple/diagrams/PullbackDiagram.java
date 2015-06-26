package com.fdilke.topple.diagrams;

import java.util.List;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 29-Jan-2008
 * Time: 23:58:13
 * To change this template use File | Settings | File Templates.
 */
public interface PullbackDiagram<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
    public ARROW getNorth();

    public ARROW getSouth();

    public ARROW getWest();

    public ARROW getEast();

    public ARROW factorize(List<ARROW> commutingArrows);
}
