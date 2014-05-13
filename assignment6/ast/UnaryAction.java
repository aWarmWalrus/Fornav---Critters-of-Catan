package ast;

public class UnaryAction extends Unary<UnaryAction.Op> implements Action {

	public enum Op {
		TAG {
			@Override
			public String toString() {
				return "tag";
			}
		},
		SERVE {
			@Override
			public String toString() {
				return "serve";
			}
		}
	}

	public UnaryAction(Op op, Expression expr) {
		super(op, expr);
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
			return unaryCopy();
		default:
			throw new AssertionError();
		}
	}

	@Override
	public UnaryAction dup(RichNode dupParent) {
		UnaryAction dup = new UnaryAction(op, null);
		dup.setParent(dupParent);
		dup.expr = expr.dup(dup);
		return dup;
	}

}
