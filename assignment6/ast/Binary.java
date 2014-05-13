package ast;

import java.util.List;

import util.F;

abstract class Binary<Child extends RichNode, Operator> extends AbstractNode
		implements Operable<Operator> {

	Child left;
	Operator op;
	Child right;

	Binary(Child left, Operator op, Child right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	abstract boolean handleRemove();

	@Override
	public boolean mutate(Mutation.Type type) {
		switch (type) {
		case REPLICATE:
			return false;
		case SWAP:
			Mutation.swapArgs(this);
			return true;
		case COPY:
			return Mutation.copy(this);
		case COPY_TREE:
			return copyTree(this);
		case REMOVE:
			return handleRemove();
		case CREATE_PARENT:
			createParent(this);
			return true;
		default:
			throw new AssertionError();
		}
	}

	private static <T> T dispatch(RichNode node, F<Expression, T> expr,
			F<Condition, T> cond) {
		boolean isExpr = node instanceof Expression;
		boolean isCond = node instanceof Condition;
		if (isExpr == isCond) {
			throw new AssertionError();
		} else if (isExpr) {
			return expr.f((Expression) node);
		} else {
			return cond.f((Condition) node);
		}
	}

	private static boolean createParent(RichNode node) {
		return dispatch(node, new F<Expression, Boolean>() {
			@Override
			public Boolean f(Expression a) {
				Mutation.createExprParent(a);
				return true;
			}
		}, new F<Condition, Boolean>() {
			@Override
			public Boolean f(Condition a) {
				Mutation.createCondParent(a);
				return true;
			}
		});
	}

	private static boolean copyTree(RichNode node) {
		return dispatch(node, new F<Expression, Boolean>() {
			@Override
			public Boolean f(Expression a) {
				return Mutation.copyExprTree(a);
			}
		}, new F<Condition, Boolean>() {
			@Override
			public Boolean f(Condition a) {
				return Mutation.copyCondTree(a);
			}
		});
	}

	@Override
	public int size() {
		return left.size() + right.size() + 1;
	}

	abstract String leftGroup();

	abstract String rightGroup();

	@Override
	public void prettyPrint(StringBuffer sb) {
		sb.append(leftGroup());
		left.prettyPrint(sb);
		sb.append(String.format(" %s ", op));
		right.prettyPrint(sb);
		sb.append(rightGroup());
	}

	@Override
	public void query(F<Node, Boolean> p, List<Node> acc) {
		if (p.f(this)) {
			acc.add(this);
		}
		left.query(p, acc);
		right.query(p, acc);
	}

	@Override
	public Operator getOp() {
		return op;
	}

	@Override
	public void setOp(Operator op) {
		this.op = op;
	}

}
