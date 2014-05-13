package ast;

import java.util.List;

import util.F;

abstract class Unary<Operator> extends AbstractNode implements
		Operable<Operator> {

	Operator op;
	Expression expr;

	Unary(Operator op, Expression expr) {
		this.op = op;
		this.expr = expr;
	}
    
            
	@Override
	public int size() {
		return expr.size() + 1;
	}

	@Override
	public void prettyPrint(StringBuffer sb) {
		sb.append(op);
		sb.append("[");
		expr.prettyPrint(sb);
		sb.append("]");
	}

	@Override
	public void query(F<Node, Boolean> p, List<Node> acc) {
		if (p.f(this)) {
			acc.add(this);
		}
		expr.query(p, acc);
	}

	public boolean unaryCopy() {
		return Mutation.copy(this);
	}

	@Override
	public Operator getOp() {
		return op;
	}

	@Override
	public void setOp(Operator op) {
		this.op = op;
	}
	
	public Expression getExpr() {
		return expr;
	}

}
