package ast;

import student.Critter;

// A critter program expression that has an integer value.
public interface Expression extends RichNode {
    
    /**
	 * Evaluates the int value of this expression.
	 * @param c The critter to be evaluated for
	 * @return The int value of this condition
	 */
	int eval(Critter c);

	Expression dup(RichNode dupParent);

}
