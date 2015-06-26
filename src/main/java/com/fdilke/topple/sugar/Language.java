package com.fdilke.topple.sugar;

/*
 * Allows us to select a language - either the 'traditional' (old-style topos machine code), or the new Bile layer.
 * Some algorithms are implemented twice, once in each. Using this enum facilitates testing.
 */
public enum Language {
	TRAD, NEW
}
