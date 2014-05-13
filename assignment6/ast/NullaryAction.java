package ast;

import java.util.List;

import util.F;

public class NullaryAction extends AbstractNode implements Action,
		Operable<NullaryAction.Op> {

	public enum Op {
		WAIT, FORWARD, BACKWARD, LEFT, RIGHT, EAT, ATTACK, GROW, BUD, MATE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	Op op;

	public NullaryAction(Op op) {
		this.op = op;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean mutate(Mutation.Type type) {
		switch (type) {
		case SWAP:
		case CREATE_PARENT:
		case REPLICATE:
			return false;
		case REMOVE:
			return Mutation.removeMust(this);
		case COPY_TREE:
			return Mutation.copyMustTree(this);
		case COPY:
			return Mutation.copy(this);
		default:
			throw new AssertionError();
		}
	}

	@Override
	public void prettyPrint(StringBuffer sb) {
		sb.append(op);
	}

	@Override
	public void query(F<Node, Boolean> p, List<Node> acc) {
		if (p.f(this)) {
			acc.add(this);
		}
	}

	@Override
	public NullaryAction dup(RichNode dupParent) {
		NullaryAction dup = new NullaryAction(op);
		dup.setParent(dupParent);
		return dup;
	}

	@Override
	public Op getOp() {
		return op;
	}

	@Override
	public void setOp(Op op) {
		this.op = op;
	}

}
