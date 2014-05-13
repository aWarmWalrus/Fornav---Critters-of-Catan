package parse;

public class SyntaxError extends RuntimeException {

	private final String token;
	private final int line;

	public SyntaxError(int line) {
		token = null;
		this.line = line;
	}

	public SyntaxError(Token token) {
		this.token = token.toString();
		this.line = token.lineNo;
	}

	private static final long serialVersionUID = -3800218422490333047L;

	@Override
	public String toString() {
		String unexpected = token == null ? "<none>" : token;
		return String.format("%s @ %d", unexpected, line);
	}

}
