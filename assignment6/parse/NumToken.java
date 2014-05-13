package parse;

/**
 * A NumToken is a token containing a number.
 * 
 * @author Chinawat
 */
public class NumToken extends Token {

	private final int value;

	public NumToken(int value, int lineNo) {
		super(NUM, lineNo);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
