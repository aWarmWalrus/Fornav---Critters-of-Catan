package ast;

import java.util.*;

class Categories {

	private Categories() {
	}

	/**
	 * Maps nodes to their possible replacements by virtue of their types
	 */
	static final Map<Class<? extends Node>, Class<? extends Node>> ByType = new HashMap<Class<? extends Node>, Class<? extends Node>>();
	static {
		ByType.put(Arithmetic.class, Expression.class);
		ByType.put(NumberLiteral.class, Expression.class);
		ByType.put(UnaryExpr.class, Expression.class);
		ByType.put(Logical.class, Condition.class);
		ByType.put(Comparison.class, Condition.class);
		ByType.put(NullaryAction.class, Action.class);
		ByType.put(UnaryAction.class, Action.class);
		ByType.put(Update.class, Update.class);
		ByType.put(Rule.class, Rule.class);
		// for completeness
		ByType.put(Program.class, null);
	}

	/**
	 * Maps nodes to nodes which have the same operator families
	 */
	static final Map<Class<? extends Node>, Class<? extends Node>> ByStructure = new HashMap<Class<? extends Node>, Class<? extends Node>>();
	static {
		ByStructure.put(Arithmetic.class, Arithmetic.class);
		ByStructure.put(NumberLiteral.class, NumberLiteral.class);
		ByStructure.put(UnaryExpr.class, UnaryExpr.class);
		ByStructure.put(Logical.class, Logical.class);
		ByStructure.put(Comparison.class, Comparison.class);
		ByStructure.put(NullaryAction.class, NullaryAction.class);
		ByStructure.put(UnaryAction.class, UnaryAction.class);
		// for completeness
		ByStructure.put(Update.class, null);
		ByStructure.put(Rule.class, null);
		ByStructure.put(Program.class, null);
	}

}
