package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import util.F;
import util.Utils;

// TODO: mutations are probably better implemented as structural sharing on a persistent data structure
public class Mutation {

	private Mutation() {
	}

	public enum Type { //made it public (Kelly)
		REMOVE, SWAP, COPY_TREE, COPY, CREATE_PARENT, REPLICATE
	}

	public static boolean mutate(RichNode root) {
		List<Node> all = new ArrayList<Node>();
		root.query(Utils.<Node, Boolean> constant(true), all);
		while (!all.isEmpty()) {
			Node node = Utils.randomlySelect(all);
			for (Type type : randomize(Type.values())) {
				boolean success = node.mutate(type);
				if (success) {
					return true;
				}
			}
			all.remove(node);
		}
		return false;
	}

	private static <T> List<T> randomize(T[] elements) {
		List<T> randomized = new ArrayList<T>(Arrays.asList(elements));
		Collections.shuffle(randomized);
		return randomized;
	}

	private static RichNode root(RichNode current) {
		RichNode parent = current.getParent();
		return parent == null ? current : root(parent);
	}

	@SuppressWarnings("unchecked")
	static <T extends Node> List<T> alikes(RichNode node,
			Map<Class<? extends Node>, Class<? extends Node>> categories) {
		Class<? extends Node> category = categories.get(node.getClass());
		if (category == null) {
			throw new IllegalArgumentException();
		}
		List<Node> alikes = new ArrayList<Node>();
		root(node).query(Utils.isA(category), alikes);
		boolean inCategory = alikes.remove(node);
		if (!inCategory) {
			throw new AssertionError();
		}
		return (List<T>) alikes;
	}

	static <T extends Node> T alike(RichNode node,
			Map<Class<? extends Node>, Class<? extends Node>> categories) {
		return Utils.randomlySelect(Mutation.<T> alikes(node, categories));
	}

	static <Child extends RichNode> void swapArgs(Binary<Child, ?> binary) {
		Child tmp = binary.left;
		binary.left = binary.right;
		binary.right = tmp;
	}

	static <T> boolean swap2(List<T> xs) {
		int size = xs.size();
		if (size < 2) {
			return false;
		}
		List<Integer> indices = Utils.distinctRandoms(xs.size(), 2);
		int index1 = indices.get(0);
		int index2 = indices.get(1);
		Collections.swap(xs, index1, index2);
		return true;
	}

	static <Op, T extends Operable<Op> & RichNode> boolean copy(final T target) {
		List<T> nodes = alikes(target, Categories.ByStructure);
		nodes = Utils.filter(new F<T, Boolean>() {
			@Override
			public Boolean f(T x) {
				return !x.getOp().equals(target.getOp());
			}
		}).f(nodes);
		T donor = Utils.randomlySelect(nodes);
		if (donor == null) {
			return false;
		}
		target.setOp(donor.getOp());
		return true;
	}

	static <Child extends RichNode> Child randomArg(Binary<Child, ?> binary) {
		return Utils.RNG.nextBoolean() ? binary.left : binary.right;
	}

	static boolean createCondParent(Condition child) {
		Logical.Op op = randomize(Logical.Op.values()).get(0);
		Logical logical = new Logical(null, op, null);
		logical.left = child;
		Condition cond = alike(child, Categories.ByType);
		if (cond == null) {
			return false;
		}
		logical.right = cond.dup(logical);
		replaceCond(child, logical);
		child.setParent(logical);
		return true;
	}

	private enum NewExprParent {
		UNARY_EXPR, ARITHMETIC
	}

	static void createExprParent(Expression child) {
		for (NewExprParent exprParent : randomize(NewExprParent.values())) {
			switch (exprParent) {
			case UNARY_EXPR:
				UnaryExpr.Op op1 = randomize(UnaryExpr.Op.values()).get(0);
				UnaryExpr unaryExpr = new UnaryExpr(op1, child);
				replaceExpr(child, unaryExpr);
				child.setParent(unaryExpr);
				return;
			case ARITHMETIC:
				Arithmetic.Op op2 = randomize(Arithmetic.Op.values()).get(0);
				Arithmetic arithmetic = new Arithmetic(null, op2, null);
				Expression other = alike(child, Categories.ByType);
				if (other == null) {
					continue;
				}
				Expression copied = other.dup(arithmetic);
				if (Utils.RNG.nextBoolean()) {
					arithmetic.left = child;
					arithmetic.right = copied;
				} else {
					arithmetic.left = copied;
					arithmetic.right = child;
				}
				replaceExpr(child, arithmetic);
				child.setParent(arithmetic);
				return;
			default:
				throw new InternalError();
			}
		}
		// at least UNARY_EXPR should always succeed
		throw new AssertionError();
	}

	static void replaceCond(Condition oldCond, Condition newCond) {
		RichNode parent = oldCond.getParent();
		newCond.setParent(parent);
		if (parent instanceof Rule) {
			Rule rule = (Rule) parent;
			rule.condition = newCond;
		} else if (parent instanceof Logical) {
			Logical logical = (Logical) parent;
			if (oldCond == logical.left) {
				logical.left = newCond;
			} else if (oldCond == logical.right) {
				logical.right = newCond;
			} else {
				throw new AssertionError();
			}
		} else {
			throw new AssertionError();
		}
	}

	static void replaceExpr(Expression oldExpr, Expression newExpr) {
		RichNode parent = oldExpr.getParent();
		newExpr.setParent(parent);
		if (parent instanceof Unary) {
			Unary<?> unary = (Unary<?>) parent;
			unary.expr = newExpr;
		} else if (parent instanceof Comparison) {
			Comparison comparison = (Comparison) parent;
			if (oldExpr == comparison.left) {
				comparison.left = newExpr;
			} else if (oldExpr == comparison.right) {
				comparison.right = newExpr;
			} else {
				throw new AssertionError();
			}
		} else if (parent instanceof Update) {
			Update update = (Update) parent;
			if (oldExpr == update.idx) {
				update.idx = newExpr;
			} else if (oldExpr == update.val) {
				update.val = newExpr;
			} else {
				throw new AssertionError();
			}
		} else if (parent instanceof Arithmetic) {
			Arithmetic arithmetic = (Arithmetic) parent;
			if (oldExpr == arithmetic.left) {
				arithmetic.left = newExpr;
			} else if (oldExpr == arithmetic.right) {
				arithmetic.right = newExpr;
			} else {
				throw new AssertionError();
			}
		} else {
			throw new AssertionError();
		}
	}

	static boolean copyExprTree(Expression expr) {
		Expression other = alike(expr, Categories.ByType);
		if (other == null) {
			return false;
		}
		Expression copied = other.dup(expr.getParent());
		replaceExpr(expr, copied);
		return true;
	}

	static boolean copyCondTree(Condition cond) {
		Condition other = alike(cond, Categories.ByType);
		if (other == null) {
			return false;
		}
		Condition copied = other.dup(cond.getParent());
		replaceCond(cond, copied);
		return true;
	}

	private static Rule getParent(Command command) {
		Node parent = command.getParent();
		if (!(parent instanceof Rule)) {
			throw new AssertionError();
		}
		return (Rule) command.getParent();
	}

	static boolean removeMust(Command must) {
		Rule rule = getParent(must);
		if (must != rule.must) {
			throw new AssertionError();
		}
		if (rule.updates.isEmpty()) {
			return false;
		}
		Update replacement = Utils.randomlySelect(rule.updates);
		rule.updates.remove(replacement);
		rule.must = replacement;
		return true;
	}

	static boolean copyMustTree(Command command) {
		Rule rule = getParent(command);
		Command other = alike(command, Categories.ByType);
		if (other == null) {
			return false;
		}
		rule.must = other.dup(rule);
		return true;
	}

	static <T extends RichNode> boolean copyListedTree(T t, List<T> ts,
			RichNode parent) {
		T rule = alike(t, Categories.ByType);
		if (rule == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		T copied = (T) rule.dup(parent);
		boolean any = Collections.replaceAll(ts, t, copied);
		if (!any) {
			throw new AssertionError();
		}
		return true;
	}

}
