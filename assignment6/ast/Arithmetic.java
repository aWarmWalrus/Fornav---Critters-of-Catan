package ast;

import student.Critter;

public class Arithmetic extends Binary<Expression, Arithmetic.Op> implements
		Expression {

	public enum Op {
		PLUS {
			@Override
			public String toString() {
				return "+";
			}
		},
		MINUS {
			@Override
			public String toString() {
				return "-";
			}
		},
		MUL {
			@Override
			public String toString() {
				return "*";
			}
		},
		DIV {
			@Override
			public String toString() {
				return "/";
			}
		},
		MOD {
			@Override
			public String toString() {
				return "mod";
			}
		};
	}

	public Arithmetic(Expression left, Op op, Expression right) {
		super(left, op, right);
	}
            
    @Override
    public int eval(Critter c) {
        // TODO implement me!
        // returns the boolean value of this expression for Critter c
    	int value;
    	if (op.toString().equals("+"))
    		value = left.eval(c) + right.eval(c);
    	else if (op.toString().equals("-"))
    		value = left.eval(c) - right.eval(c);
    	else if (op.toString().equals("*"))
    		value = left.eval(c) * right.eval(c);
    	else if (op.toString().equals("/")){
    		int r = right.eval(c);
    		if(r == 0) return 0;
    		value = left.eval(c) / r;
    	}
    	else if (op.toString().equals("mod")){
    		int r = right.eval(c);
    		if(r == 0) return 0;
    		value = left.eval(c) % right.eval(c);
    	}
    	else {
    		System.out.println("Error in Arithmetic eval");
    		value = 0;
    	}
    	return value;
    }
            
	@Override
	public boolean handleRemove() {
		Expression randomArg = Mutation.randomArg(this);
		Mutation.replaceExpr(this, randomArg);
		return true;
	}

	@Override
	public Arithmetic dup(RichNode dupParent) {
		Arithmetic dup = new Arithmetic(null, op, null);
		dup.setParent(dupParent);
		dup.left = left.dup(dup);
		dup.right = right.dup(dup);
		return dup;
	}

	@Override
	String leftGroup() {
		return "(";
	}

	@Override
	String rightGroup() {
		return ")";
	}

}
