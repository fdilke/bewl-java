package com.fdilke.topple.util;

public class Pair<A, B> {
	public final A a;
	public final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public String toString() {
		return "Pair(" + a + "," + b + ")";
	}

	private static boolean equals(Object x, Object y) {
		return (x == null && y == null) || (x != null && x.equals(y));
	}

	public boolean equals(Object other) {
		return other instanceof Pair<?, ?> && equals(a, ((Pair<?, ?>) other).a)
				&& equals(b, ((Pair<?, ?>) other).b);
	}

	public int hashCode() {
		if (a == null)
			return (b == null) ? 0 : b.hashCode() + 1;
		else if (b == null)
			return a.hashCode() + 2;
		else
			return a.hashCode() * 17 + b.hashCode();
	}
}