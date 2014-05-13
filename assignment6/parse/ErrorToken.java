package parse;

/**
 * An ErrorToken is a token containing unrecognized string in the critter
 * language.
 * 
 * @author Chinawat
 */
public class ErrorToken extends Token {

	private final String value;

	public ErrorToken(String value, int lineNo) {
		super(ERROR, lineNo);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "[error] " + value;
	}

}
