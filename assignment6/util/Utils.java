package util;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	private Utils() {
	}

	public static final java.util.Random RNG = new java.util.Random();

	public static <T> T randomlySelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		return list.get(RNG.nextInt(list.size()));
	}

	public static <A, B> F<A, B> constant(final B constant) {
		return new F<A, B>() {
			@Override
			public B f(A a) {
				return constant;
			}
		};
	}

	public static <T> F<T, Boolean> isA(final Class<? extends T> type) {
		return new F<T, Boolean>() {
			@Override
			public Boolean f(T a) {
				return type.isAssignableFrom(a.getClass());
			}
		};
	}

	public static <T> F<List<T>, List<T>> filter(final F<T, Boolean> p) {
		return new F<List<T>, List<T>>() {
			@Override
			public List<T> f(List<T> a) {
				List<T> ret = new ArrayList<T>();
				for (T t : a) {
					if (p.f(t)) {
						ret.add(t);
					}
				}
				return ret;
			}
		};
	}

	// this is really inefficient in general, but it's fine for our usage
	// and its correctness is easy to verify
	public static List<Integer> distinctRandoms(int bound, int n) {
		List<Integer> candidates = new ArrayList<Integer>();
		for (int i = 0; i < bound; i++) {
			candidates.add(i);
		}
		List<Integer> ret = new ArrayList<Integer>(n);
		for (int i = 0; i < n && !candidates.isEmpty(); i++) {
			Integer chosen = randomlySelect(candidates);
			candidates.remove(chosen);
			ret.add(chosen);
		}
		return ret;
	}

}
