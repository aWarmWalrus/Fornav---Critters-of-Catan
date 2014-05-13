package ast;

import student.Critter;

public class Comparison extends Binary<Expression, Comparison.Op> implements
		Condition {

	public enum Op {
		LT {
			@Override
			public String toString() {
				return "<";
			}
		},
		LE {
			@Override
			public String toString() {
				return "<=";
			}
		},
		EQ {
			@Override
			public String toString() {
				return "=";
			}
		},
		GE {
			@Override
			public String toString() {
				return ">=";
			}
		},
		GT {
			@Override
			public String toString() {
				return ">";
			}
		},
		NE {
			@Override
			public String toString() {
				return "!=";
			}
		};
	}

	public Comparison(Expression left, Op op, Expression right) {
		super(left, op, right);
	}
            
    @Override
    public boolean eval(Critter c) {
        // TODO implement me!
        // returns the boolean value of this condition for Critter c
		if (op.toString().equals("<"))
			return left.eval(c) < right.eval(c);
		else if (op.toString().equals("<="))
			return left.eval(c) <= right.eval(c);
		else if (op.toString().equals("="))
			return left.eval(c) == right.eval(c);
		else if (op.toString().equals(">"))
			return left.eval(c) > right.eval(c);
		else if (op.toString().equals(">="))
			return left.eval(c) >= right.eval(c);
		else if (op.toString().equals("!="))
			return left.eval(c) != right.eval(c);
		else
			System.out.println("Error in Comparison");
    	return true;
    }

	@Override
	public boolean handleRemove() {
		return false;
	}

	@Override
	public Comparison dup(RichNode dupParent) {
		Comparison dup = new Comparison(null, op, null);
		dup.setParent(dupParent);
		dup.left = left.dup(dup);
		dup.right = right.dup(dup);
		return dup;
	}

	@Override
	String leftGroup() {
		return "";
	}

	@Override
	String rightGroup() {
		return "";
	}

}
