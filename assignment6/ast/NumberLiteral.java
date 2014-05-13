package ast;

import java.util.List;

import student.Critter;
import util.F;
import util.Utils;

public class NumberLiteral extends AbstractNode implements Expression {

	private int val;

	public NumberLiteral(int val) {
		this.val = val;
	}
    
    @Override
	public int eval(Critter c) {
		return val;
	}
    
	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean mutate(Mutation.Type type) {
		switch (type) {
		case REMOVE:
			// parent needs replacement Expression, but NumberLiteral has no
			// children to replace it
		case SWAP:
		case REPLICATE:
			return false;
		case COPY_TREE:
			return Mutation.copyExprTree(this);
		case COPY:
			// does not use Mutation.copy since we can copy same values due to
			// randomization
			NumberLiteral numLit = alike(Categories.ByStructure);
			if (numLit == null) {
				return false;
			}
			val = numLit.val + Integer.MAX_VALUE / Utils.RNG.nextInt();
			return true;
		case CREATE_PARENT:
			Mutation.createExprParent(this);
			return true;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public void prettyPrint(StringBuffer sb) {
		sb.append(Integer.toString(val));
	}

	@Override
	public void query(F<Node, Boolean> p, List<Node> acc) {
		if (p.f(this)) {
			acc.add(this);
		}
	}

	@Override
	public NumberLiteral dup(RichNode dupParent) {
		NumberLiteral dup = new NumberLiteral(val);
		dup.setParent(dupParent);
		return dup;
	}

}
