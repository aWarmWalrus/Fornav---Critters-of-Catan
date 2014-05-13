package parse;

import static parse.Token.*;

import java.io.Reader;
import java.util.*;

import ast.*;

public class ParserImpl implements Parser {

	/** The tokenizer from which input is read. */
	private Tokenizer tokenizer;

	@Override
	public Program parse(Reader r) {
		tokenizer = new Tokenizer(r);
		Program program = program();
		tokenizer.close();
		tokenizer = null;// gc
		return program.init();// parents
	}

	/**
	 * Parses a program from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program. All following methods with a
	 * name "parseX" have the same spec except that they parse syntactic form X.
	 * 
	 * @return the created AST
	 * @throws SyntaxError
	 *             if there the input tokens have invalid syntax
	 */
	private Program program() {
		List<Rule> rules = new ArrayList<Rule>();
		while (tokenizer.hasNext()) {
			Rule rule = rule();
			rules.add(rule);
		}
		return new Program(rules);
	}

	private Rule rule() {
		Condition condition = condition();
		consume(ARR);
		List<Update> updates = new ArrayList<Update>();
		Command must = null;
		while (true) {
			if (peekType() == MEM || peek().isMemSugar()) {
				Update update = update();
				updates.add(update);
			} else if (peek().isAction()) {
				must = action();
				if (peekType() != SEMICOLON) {
					throw new SyntaxError(peek());
				}
			} else if (peekType() == SEMICOLON) {
				break;
			} else {
				throw new SyntaxError(peek());
			}
		}
		consume(SEMICOLON);
		if (must == null) {
			if (updates.isEmpty()) {
				throw new SyntaxError(peek());
			}
			must = updates.remove(updates.size() - 1);
		}
		return new Rule(condition, updates, must);
	}

	private Update update() {
		switch (peekType()) {
		case MEM: {
			consume(MEM);
			consume(LBRACKET);
			Expression idx = expression();
			consume(RBRACKET);
			consume(ASSIGN);
			Expression val = expression();
			return new Update(idx, val);
		}
		case ABV_MEMSIZE:
		case ABV_DEFENSE:
		case ABV_OFFENSE:
		case ABV_SIZE:
		case ABV_ENERGY:
		case ABV_PASS:
		case ABV_TAG:
		case ABV_POSTURE: {
			int type = peekType();
			consume(type);
			Expression idx = new NumberLiteral(memoryLocation(type));
			consume(ASSIGN);
			Expression val = expression();
			return new Update(idx, val);
		}
		default:
			throw new SyntaxError(peek());
		}
	}

	private Action action() {
		switch (peekType()) {
		case WAIT:
			consume(WAIT);
			return new NullaryAction(NullaryAction.Op.WAIT);
		case FORWARD:
			consume(FORWARD);
			return new NullaryAction(NullaryAction.Op.FORWARD);
		case BACKWARD:
			consume(BACKWARD);
			return new NullaryAction(NullaryAction.Op.BACKWARD);
		case LEFT:
			consume(LEFT);
			return new NullaryAction(NullaryAction.Op.LEFT);
		case RIGHT:
			consume(RIGHT);
			return new NullaryAction(NullaryAction.Op.RIGHT);
		case EAT:
			consume(EAT);
			return new NullaryAction(NullaryAction.Op.EAT);
		case ATTACK:
			consume(ATTACK);
			return new NullaryAction(NullaryAction.Op.ATTACK);
		case GROW:
			consume(GROW);
			return new NullaryAction(NullaryAction.Op.GROW);
		case BUD:
			consume(BUD);
			return new NullaryAction(NullaryAction.Op.BUD);
		case MATE:
			consume(MATE);
			return new NullaryAction(NullaryAction.Op.MATE);
		case TAG:
			consume(TAG);
			consume(LBRACKET);
			Expression tagVal = expression();
			consume(RBRACKET);
			return new UnaryAction(UnaryAction.Op.TAG, tagVal);
		case SERVE:
			consume(SERVE);
			consume(LBRACKET);
			Expression serveVal = expression();
			consume(RBRACKET);
			return new UnaryAction(UnaryAction.Op.SERVE, serveVal);
		default:
			throw new SyntaxError(peek());
		}
	}

	private Condition condition() {
		Condition left = conjunction();
		while (peekType() == OR) {
			consume(OR);
			left = new Logical(left, Logical.Op.OR, conjunction());
		}
		return left;
	}

	private Condition conjunction() {
		Condition left = relation();
		while (peekType() == AND) {
			consume(AND);
			left = new Logical(left, Logical.Op.AND, relation());
		}
		return left;
	}

	private Condition relation() {
		switch (peekType()) {
		case LBRACE:
			consume(LBRACE);
			Condition condition = condition();
			consume(RBRACE);
			return condition;
		case NUM:
		case MEM:
		case ABV_MEMSIZE:
		case ABV_DEFENSE:
		case ABV_OFFENSE:
		case ABV_SIZE:
		case ABV_ENERGY:
		case ABV_PASS:
		case ABV_TAG:
		case ABV_POSTURE:
		case LPAREN:
		case NEARBY:
		case AHEAD:
		case RANDOM:
			Expression l = expression();
			Comparison.Op op = cmp_op();
			Expression r = expression();
			return new Comparison(l, op, r);
		default:
			throw new SyntaxError(peek());
		}
	}

	private Comparison.Op cmp_op() {
		switch (peekType()) {
		case LT:
			consume(LT);
			return Comparison.Op.LT;
		case LE:
			consume(LE);
			return Comparison.Op.LE;
		case EQ:
			consume(EQ);
			return Comparison.Op.EQ;
		case GE:
			consume(GE);
			return Comparison.Op.GE;
		case GT:
			consume(GT);
			return Comparison.Op.GT;
		case NE:
			consume(NE);
			return Comparison.Op.NE;
		default:
			throw new SyntaxError(peek());
		}
	}

	private Expression expression() {
		return term();
	}

	private Expression term() {
		Expression left = factor();
		while (peek().isAddOp()) {
			Arithmetic.Op op = add_op();
			left = new Arithmetic(left, op, factor());
		}
		return left;
	}

	private Arithmetic.Op add_op() {
		switch (peekType()) {
		case PLUS:
			consume(PLUS);
			return Arithmetic.Op.PLUS;
		case MINUS:
			consume(MINUS);
			return Arithmetic.Op.MINUS;
		default:
			throw new SyntaxError(peek());
		}
	}

	private Expression factor() {
		Expression left = atom();
		while (peek().isMulOp()) {
			Arithmetic.Op op = mul_op();
			left = new Arithmetic(left, op, atom());
		}
		return left;
	}

	private Arithmetic.Op mul_op() {
		switch (peekType()) {
		case MUL:
			consume(MUL);
			return Arithmetic.Op.MUL;
		case DIV:
			consume(DIV);
			return Arithmetic.Op.DIV;
		case MOD:
			consume(MOD);
			return Arithmetic.Op.MOD;
		default:
			throw new SyntaxError(peek());
		}
	}

	private Expression atom() {
		switch (peekType()) {
		case NUM:
			NumToken num = (NumToken) peek();
			consume(NUM);
			return new NumberLiteral(num.getValue());
		case MEM:
			consume(MEM);
			consume(LBRACKET);
			Expression idx = expression();
			consume(RBRACKET);
			return new UnaryExpr(UnaryExpr.Op.MEM, idx);
		case ABV_MEMSIZE:
		case ABV_DEFENSE:
		case ABV_OFFENSE:
		case ABV_SIZE:
		case ABV_ENERGY:
		case ABV_PASS:
		case ABV_TAG:
		case ABV_POSTURE:
			int type = peekType();
			consume(type);
			NumberLiteral location = new NumberLiteral(memoryLocation(type));
			return new UnaryExpr(UnaryExpr.Op.MEM, location);
		case LPAREN:
			consume(LPAREN);
			Expression expr = expression();
			consume(RPAREN);
			return expr;
		case NEARBY:
			consume(NEARBY);
			consume(LBRACKET);
			Expression dir = expression();
			consume(RBRACKET);
			return new UnaryExpr(UnaryExpr.Op.NEARBY, dir);
		case AHEAD:
			consume(AHEAD);
			consume(LBRACKET);
			Expression dist = expression();
			consume(RBRACKET);
			return new UnaryExpr(UnaryExpr.Op.AHEAD, dist);
		case RANDOM:
			consume(RANDOM);
			consume(LBRACKET);
			Expression bound = expression();
			consume(RBRACKET);
			return new UnaryExpr(UnaryExpr.Op.RANDOM, bound);
		default:
			throw new SyntaxError(peek());
		}
	}

	private static int memoryLocation(int abv) {
		switch (abv) {
		case ABV_MEMSIZE:
			return 0;
		case ABV_DEFENSE:
			return 1;
		case ABV_OFFENSE:
			return 2;
		case ABV_SIZE:
			return 3;
		case ABV_ENERGY:
			return 4;
		case ABV_PASS:
			return 5;
		case ABV_TAG:
			return 6;
		case ABV_POSTURE:
			return 7;
		default:
			throw new IllegalArgumentException();
		}
	}

	private Token peek() {
		if (!tokenizer.hasNext()) {
			throw new SyntaxError(tokenizer.lineNo);
		}
		return tokenizer.peek();
	}

	private int peekType() {
		return peek().getType();
	}

	/**
	 * Consumes a token of the expected type. Throws a SyntaxError if the wrong
	 * kind of token is encountered.
	 */
	private void consume(int expected) {
		if (!tokenizer.hasNext()) {
			throw new SyntaxError(peek());
		}
		int encountered = tokenizer.next().getType();
		if (encountered != expected) {
			throw new SyntaxError(peek());
		}
	}

}
