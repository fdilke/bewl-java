package com.fdilke.topple.sugar;

public interface Lambda<SRC extends Element<SRC>, DEST extends Element<DEST>> {
    public DEST lambda(SRC element);
}
