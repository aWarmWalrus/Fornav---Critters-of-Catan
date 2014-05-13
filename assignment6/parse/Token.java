package parse;

import java.util.InputMismatchException;

/**
 * A Token represents a legal token (symbol) in the critter language
 */
public class Token {

	// Token types
	public static final int MEM = 0;
	public static final int WAIT = 10;
	public static final int FORWARD = 11;
	public static final int BACKWARD = 12;
	public static final int LEFT = 13;
	public static final int RIGHT = 14;
	public static final int EAT = 15;
	public static final int ATTACK = 16;
	public static final int GROW = 17;
	public static final int BUD = 18;
	public static final int MATE = 19;
	public static final int TAG = 20;
	public static final int SERVE = 21;
	public static final int OR = 30;
	public static final int AND = 31;
	public static final int LT = 32;
	public static final int LE = 33;
	public static final int EQ = 34;
	public static final int GE = 35;
	public static final int GT = 36;
	public static final int NE = 37;
	public static final int PLUS = 50;
	public static final int MINUS = 51;
	public static final int MUL = 60;
	public static final int DIV = 61;
	public static final int MOD = 62;
	public static final int ASSIGN = 70;
	public static final int NEARBY = 80;
	public static final int AHEAD = 81;
	public static final int RANDOM = 82;
	public static final int LBRACKET = 100;
	public static final int RBRACKET = 101;
	public static final int LPAREN = 102;
	public static final int RPAREN = 103;
	public static final int LBRACE = 104;
	public static final int RBRACE = 105;
	public static final int ARR = 110;
	public static final int SEMICOLON = 111;
	public static final int ABV_MEMSIZE = 200;
	public static final int ABV_DEFENSE = 201;
	public static final int ABV_OFFENSE = 202;
	public static final int ABV_SIZE = 203;
	public static final int ABV_ENERGY = 204;
	public static final int ABV_PASS = 205;
	public static final int ABV_TAG = 206;
	public static final int ABV_POSTURE = 207;
	public static final int NUM = 999;
	public static final int ERROR = -1;

	protected final int type;
	protected final int lineNo;

	/**
	 * Create a token with the specified type.
	 * 
	 * @param type
	 *            The ID of the desired token type
	 * @param lineNo
	 *            The line number in the input file containing this token.
	 */
	public Token(int type, int lineNo) {
		this.type = type;
		this.lineNo = lineNo;
	}

	/**
	 * 
	 * @return The type of this token
	 */
	public int getType() {
		return type;
	}

	/**
	 * 
	 * @return The line number in the input file of this token.
	 */
	public int lineNumber() {
		return lineNo;
	}

	/**
	 * Determine whether this token is of number type.
	 * 
	 * @return true if this token is of number type
	 */
	public boolean isNum() {
		return type == NUM;
	}

	/**
	 * 
	 * @return The number token associated with this token.
	 * @throws InputMismatchException
	 *             if this token is not of number type
	 */
	public NumToken toNumToken() {
		if (isNum())
			return (NumToken) this;
		throw new InputMismatchException("Token is not a number.");
	}

	/**
	 * Determine whether this token is of action type.
	 * 
	 * @return true if this token is of action type
	 */
	public boolean isAction() {
		return 10 <= type && type <= 20;
	}

	/**
	 * Determine whether this token is of addop type.
	 * 
	 * @return true if this token is of addop type
	 */
	public boolean isAddOp() {
		return 50 <= type && type <= 51;
	}

	/**
	 * Determine whether this token is of mulop type.
	 * 
	 * @return true if this token is of mulop type
	 */
	public boolean isMulOp() {
		return 60 <= type && type <= 62;
	}

	/**
	 * Determine whether this token is of sensor type.
	 * 
	 * @return true if this token is of sensor type
	 */
	public boolean isSensor() {
		return 80 <= type && type <= 82;
	}

	/**
	 * Determine whether this token is syntactic sugar for memory locations
	 * 
	 * @return true if this token is syntactic sugar for memory locations
	 */
	public boolean isMemSugar() {
		return 200 <= type && type <= 207;
	}

	@Override
	public String toString() {
		return toString(type);
	}

	/**
	 * Return the string representation of the given token type.
	 * 
	 * @param type
	 *            The ID of the token type
	 * @return
	 */
	public static String toString(int type) {
		switch (type) {
		case MEM:
			return "mem";
		case WAIT:
			return "wait";
		case FORWARD:
			return "forward";
		case BACKWARD:
			return "backward";
		case LEFT:
			return "left";
		case RIGHT:
			return "right";
		case EAT:
			return "eat";
		case ATTACK:
			return "attack";
		case SERVE:
			return "serve";
		case TAG:
			return "tag";
		case GROW:
			return "grow";
		case BUD:
			return "bud";
		case MATE:
			return "mate";
		case OR:
			return "or";
		case AND:
			return "and";
		case LT:
			return "<";
		case LE:
			return "<=";
		case EQ:
			return "=";
		case GE:
			return ">=";
		case GT:
			return ">";
		case NE:
			return "!=";
		case PLUS:
			return "+";
		case MINUS:
			return "-";
		case MUL:
			return "*";
		case DIV:
			return "/";
		case MOD:
			return "mod";
		case ASSIGN:
			return ":=";
		case NEARBY:
			return "nearby";
		case AHEAD:
			return "ahead";
		case RANDOM:
			return "random";
		case LBRACKET:
			return "[";
		case RBRACKET:
			return "]";
		case LPAREN:
			return "(";
		case RPAREN:
			return ")";
		case LBRACE:
			return "{";
		case RBRACE:
			return "}";
		case ARR:
			return "-->";
		case SEMICOLON:
			return ";";
		case ABV_MEMSIZE:
			return "MEMSIZE";
		case ABV_DEFENSE:
			return "DEFENSE";
		case ABV_OFFENSE:
			return "OFFENSE";
		case ABV_SIZE:
			return "SIZE";
		case ABV_ENERGY:
			return "ENERGY";
		case ABV_PASS:
			return "PASS";
		case ABV_TAG:
			return "TAG";
		case ABV_POSTURE:
			return "POSTURE";
		case NUM:
			return "<number>";
		default:
			throw new InputMismatchException(
					"Token ID does not match any of the defined tokens.");
		}
	}

}
