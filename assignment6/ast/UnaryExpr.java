package ast;

import student.Critter;
import java.util.Random;

public class UnaryExpr extends Unary<UnaryExpr.Op> implements Expression {

	public enum Op {
		MEM, NEARBY, AHEAD, RANDOM;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public UnaryExpr(Op op, Expression expr) {
		super(op, expr);
	}
    
    @Override
	public int eval(Critter c) {
		// returns the value of this expression for Critter c
    	Random rand = new Random();
    	int index = expr.eval(c);
    	int value;
    	String sensor = op.toString();
    	if(sensor.equals("mem")){
    		if(index >= c.memlength)
    			return 0;
    		value = c.getMem(index);
    	} else if (sensor.equals("nearby")){
    		value = c.getSense(index);
    	} else if (sensor.equals("ahead")){
    		value = c.getAhead(index);
    	} else if (sensor.equals("random")){
    		value = rand.nextInt(index);
    		if(index < 2)
    			value = 0;
    	} else {
    		System.out.println("Error in UnaryExpr eval method");
    		value = 0;
    	}
    		
    	return value;
	}

	@Override
	public boolean mutate(Mutation.Type type) {
		switch (type) {
		case SWAP:
		case REPLICATE:
			return false;
		case REMOVE:
			Mutation.replaceExpr(this, expr);
			return true;
		case COPY_TREE:
			return Mutation.copyExprTree(this);
		case COPY:
			return unaryCopy();
		case CREATE_PARENT:
			Mutation.createExprParent(this);
			return true;
		default:
			throw new AssertionError();
		}
	}

	@Override
	public UnaryExpr dup(RichNode dupParent) {
		UnaryExpr dup = new UnaryExpr(op, null);
		dup.setParent(dupParent);
		dup.expr = expr.dup(dup);
		return dup;
	}

}
