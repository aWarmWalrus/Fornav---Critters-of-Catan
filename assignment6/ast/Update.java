package ast;

import util.F;

import java.util.List;

public class Update extends AbstractNode implements Command {

	Expression idx;
	Expression val;

	public Update(Expression idx, Expression val) {
		this.idx = idx;
		this.val = val;
	}

	@Override
	public int size() {
		return idx.size() + val.size() + 1;
	}
	
	public Expression getIdx(){
		return idx;
	}
	
	public Expression getVal(){
		return val;
	}

	@Override
	public boolean mutate(Mutation.Type type) {
		RichNode parent = getParent();
		if (!(parent instanceof Rule)) {
			throw new AssertionError();
		}
		Rule rule = (Rule) parent;
		switch (type) {
		case CREATE_PARENT:
		case COPY:
			// COPY has no effect on Updates
		case REPLICATE:
			return false;
		case REMOVE:
			if (rule.updates.contains(this)) {
				rule.updates.remove(this);
				return true;
			} else if (rule.must == this) {
				return Mutation.removeMust(this);
			} else {
				throw new AssertionError();
			}
		case SWAP:
			Expression tmp = idx;
			idx = val;
			val = tmp;
			return true;
		case COPY_TREE:
			if (rule.updates.contains(this)) {
				return Mutation.copyListedTree(this, rule.updates, rule);
			} else if (rule.must == this) {
				return Mutation.copyMustTree(this);
			} else {
				throw new AssertionError();
			}
		default:
			throw new AssertionError();
		}
	}

	@Override
	public void prettyPrint(StringBuffer sb) {
		sb.append("mem[");
		idx.prettyPrint(sb);
		sb.append("] := ");
		val.prettyPrint(sb);
	}

	@Override
	public void query(F<Node, Boolean> p, List<Node> acc) {
		if (p.f(this)) {
			acc.add(this);
		}
		idx.query(p, acc);
		val.query(p, acc);
	}

	@Override
	public Update dup(RichNode dupParent) {
		Update dup = new Update(null, null);
		dup.setParent(dupParent);
		dup.idx = idx.dup(dup);
		dup.val = val.dup(dup);
		return dup;
	}

}
