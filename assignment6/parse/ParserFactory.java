package parse;

/**
 * A factory that gives access to instances of parser.
 */
public class ParserFactory {

	/**
	 * @return a parser object for parsing a critter program
	 */
	public static Parser getParser() {
		return new ParserImpl();
	}

}
