package parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Tokenizer turns a Reader into a stream of tokens that can be iterated over
 * using a {@code for} loop.
 */
public class Tokenizer implements Iterator<Token> {

	/**
	 * The state of a Tokenizer is {@code NOT_READY} if the next token has not
	 * been processed.
	 */
	public static final int NOT_READY = 0;
	/**
	 * The state of a Tokenizer is {@code READY} if the next token has been
	 * processed.
	 */
	public static final int READY = 1;

	protected BufferedReader br;
	protected int lineNo;
	protected StringBuffer buf;
	protected int state;
	protected Token curTok;

	/**
	 * Create a tokenizer for a program to be read by the specified reader.
	 * 
	 * @param r
	 */
	public Tokenizer(Reader r) {
		this.br = new BufferedReader(r);
		this.buf = new StringBuffer();
		this.lineNo = 1;
	}

	@Override
	public boolean hasNext() {
		if (state == NOT_READY) {
			try {
				lexOneToken();
			} catch (NoSuchElementException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Token next() {
		Token tok = peek();
		state = NOT_READY;
		return tok;
	}

	/**
	 * Return the next token in the program without consuming the token.
	 * 
	 * @return
	 */
	public Token peek() {
		if (state == NOT_READY)
			lexOneToken();
		return curTok;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Close the reader opened by this tokenizer.
	 */
	public void close() {
		try {
			br.close();
		} catch (IOException e) {
			System.out.println("IOException:");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Read one token from the reader. One token is always produced if the end
	 * of file is not encountered, but that token may be an error token.
	 * 
	 * @throws NoSuchElementException
	 *             if EOF is encountered and a token cannot be produced.
	 */
	protected void lexOneToken() {
		char c;
		if (buf.length() == 1)
			c = buf.charAt(0);
		else
			c = nextChar();

		// consume whitespaces
		while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
			if (c == '\n')
				lineNo++;
			c = nextChar();
		}

		resetBuffer(c);

		if (c == '[')
			setNextToken(Token.LBRACKET);
		else if (c == ']')
			setNextToken(Token.RBRACKET);
		else if (c == '(')
			setNextToken(Token.LPAREN);
		else if (c == ')')
			setNextToken(Token.RPAREN);
		else if (c == '{')
			setNextToken(Token.LBRACE);
		else if (c == '}')
			setNextToken(Token.RBRACE);
		else if (c == ';')
			setNextToken(Token.SEMICOLON);
		else if (c == ':') {
			if (consume('='))
				setNextToken(Token.ASSIGN);
		} else if (c == '=')
			setNextToken(Token.EQ);
		else if (c == '!') {
			if (consume('='))
				setNextToken(Token.NE);
		} else if (c == '<')
			lexLAngle();
		else if (c == '>')
			lexRAngle();
		else if (c == '+')
			setNextToken(Token.PLUS);
		else if (c == '*')
			setNextToken(Token.MUL);
		else if (c == '/')
			setNextToken(Token.DIV);
		else if (c == '-')
			lexDash();
		else if (Character.isJavaIdentifierStart(c))
			lexIdentifier();
		else if (Character.isDigit(c))
			lexNum();
		else
			unexpected();
	}

	protected void lexLAngle() {
		int c = nextChar(false);
		if (c == -1)
			setNextToken(Token.LT);
		else {
			char cc = (char) c;
			buf.append(cc);
			if (cc == '=')
				setNextToken(Token.LE);
			else
				setNextToken(Token.LT, cc);
		}
	}

	protected void lexRAngle() {
		int c = nextChar(false);
		if (c == -1)
			setNextToken(Token.GT);
		else {
			char cc = (char) c;
			buf.append(cc);
			if (cc == '=')
				setNextToken(Token.GE);
			else
				setNextToken(Token.GT, cc);
		}
	}

	protected void lexDash() {
		int c = nextChar(false);
		if (c == -1)
			setNextToken(Token.MINUS);
		else {
			char cc = (char) c;
			buf.append(cc);
			if (cc == '-') {
				if (consume('>'))
					setNextToken(Token.ARR);
			} else
				setNextToken(Token.MINUS, cc);
		}
	}

	protected void lexIdentifier() {
		int c;
//		for (c = nextChar(false); c != -1 && Character.isJavaIdentifierPart(c); c = nextChar(false))
		for (c = nextChar(false); c != -1 && Character.isLetter(c); c = nextChar(false))
			buf.append((char) c);

		String id = buf.toString();
		if (id.equals("mem"))
			setNextToken(Token.MEM);
		else if (id.equals("wait"))
			setNextToken(Token.WAIT);
		else if (id.equals("forward"))
			setNextToken(Token.FORWARD);
		else if (id.equals("backward"))
			setNextToken(Token.BACKWARD);
		else if (id.equals("left"))
			setNextToken(Token.LEFT);
		else if (id.equals("right"))
			setNextToken(Token.RIGHT);
		else if (id.equals("eat"))
			setNextToken(Token.EAT);
		else if (id.equals("serve"))
			setNextToken(Token.SERVE);
		else if (id.equals("attack"))
			setNextToken(Token.ATTACK);
		else if (id.equals("tag"))
			setNextToken(Token.TAG);
		else if (id.equals("grow"))
			setNextToken(Token.GROW);
		else if (id.equals("bud"))
			setNextToken(Token.BUD);
		else if (id.equals("mate"))
			setNextToken(Token.MATE);
		else if (id.equals("or"))
			setNextToken(Token.OR);
		else if (id.equals("and"))
			setNextToken(Token.AND);
		else if (id.equals("mod"))
			setNextToken(Token.MOD);
		else if (id.equals("nearby"))
			setNextToken(Token.NEARBY);
		else if (id.equals("ahead"))
			setNextToken(Token.AHEAD);
		else if (id.equals("random"))
			setNextToken(Token.RANDOM);
		else if (id.equals("MEMSIZE"))
			setNextToken(Token.ABV_MEMSIZE);
		else if (id.equals("DEFENSE"))
			setNextToken(Token.ABV_DEFENSE);
		else if (id.equals("OFFENSE"))
			setNextToken(Token.ABV_OFFENSE);
		else if (id.equals("SIZE"))
			setNextToken(Token.ABV_SIZE);
		else if (id.equals("ENERGY"))
			setNextToken(Token.ABV_ENERGY);
		else if (id.equals("PASS"))
			setNextToken(Token.ABV_PASS);
		else if (id.equals("TAG"))
			setNextToken(Token.ABV_TAG);
		else if (id.equals("POSTURE"))
			setNextToken(Token.ABV_POSTURE);
		else
			unexpected();

		if (c != -1)
			buf.append((char) c);
	}

	protected void lexNum() {
		int c;
//		for (c = nextChar(false); c != -1 && Character.isJavaIdentifierPart(c); c = nextChar(false))
		for (c = nextChar(false); c != -1 && Character.isDigit(c); c = nextChar(false))
			buf.append((char) c);

		try {
			String num = buf.toString();
			int val = Integer.parseInt(num);
			curTok = new NumToken(val, lineNo);
			state = READY;
			buf = new StringBuffer();
			if (c != -1)
				buf.append((char) c);
		} catch (NumberFormatException e) {
			unexpected();
		}
	}

	/**
	 * Read the next character from the reader, treating EOF as an error. If
	 * successful, append the character to the buffer.
	 * 
	 * @return The next character
	 * @throws NoSuchElementException
	 *             if EOF is encountered
	 */
	protected char nextChar() {
		char c = (char) nextChar(true);
		buf.append(c);
		return c;
	}

	/**
	 * Read the next character from the reader. If isEOFerror, treat EOF as an
	 * error. If successful, append the character to the buffer.
	 * 
	 * @param isEOFerror
	 * @return The integer representation of the next character
	 * @throws NoSuchElementException
	 *             if EOF is encountered and isEOFerror is true
	 */
	protected int nextChar(boolean isEOFerror) {
		try {
			int c = br.read();
			if (isEOFerror && c == -1)
				throw new NoSuchElementException();
			return c;
		} catch (IOException e) {
			System.out.println("IOException:");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	protected void setNextToken(int tokenID) {
		curTok = new Token(tokenID, lineNo);
		state = READY;
		buf = new StringBuffer();
	}

	protected void setNextToken(int tokenID, char c) {
		setNextToken(tokenID);
		buf.append(c);
	}

	protected void resetBuffer(char c) {
		buf = new StringBuffer();
		buf.append(c);
	}

	/**
	 * Read the next character and determine whether it is the expected
	 * character. If not, the current buffer is an error token.
	 * 
	 * @param expected
	 *            The expected next character
	 * @return true if the next character is as expected
	 */
	protected boolean consume(char expected) {
		int c = nextChar();
		if (c != expected) {
			unexpected();
			return false;
		}
		return true;
	}

	/**
	 * Make the current buffer an error token.
	 */
	protected void unexpected() {
		curTok = new ErrorToken(buf.toString(), lineNo);
		state = READY;
		buf = new StringBuffer();
	}

}
