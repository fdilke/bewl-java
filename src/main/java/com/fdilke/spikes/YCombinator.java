package com.fdilke.spikes;

public class YCombinator {

	public static interface Function<A, B> {
		public B apply(A a);
	}

	public static interface MetaFunction<A, B>  extends Function<Function<A, B>, Function<A, B>> {
	}
	
	public static void main(String[] args) {
		MetaFunction<Integer, Integer> factorialBase = new MetaFunction<Integer, Integer>() {
			public Function<Integer, Integer> apply(final Function<Integer, Integer> f) {
				return new Function<Integer, Integer>() {
					public Integer apply(Integer n) {
						return (n <= 2) ? n : (n * f.apply(n-1));
					}
				};
			}
		};
		Function<Integer, Integer> factorial = fix(factorialBase);
		System.out.println("factorial..." + factorial.apply(6));
	}

	public static class Quine<A, B> implements Function<Quine<A, B>, Function<A, B>> {
		private final MetaFunction<A, B> base;
		
		public Quine(MetaFunction<A, B> base) {
			this.base = base;
		}

		public Function<A, B> apply(final Quine<A, B> q) {
			return base.apply(new Function<A, B>() {
				public B apply(A a) {
					return q.recurse().apply(a);
				}
			});
		}
		
		public Function<A, B> recurse() {
			return apply(this);
		}
	}
	
	public static <A, B> Function<A, B> fix(MetaFunction<A, B> base) {
		return new Quine<A, B>(base).recurse();
	}
}
