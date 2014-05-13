package student;

import java.util.Random;

import util.P;
import ast.Command;
import ast.NullaryAction;
import ast.Program;
import ast.Rule;
import ast.UnaryAction;
import ast.Update;

public class Critter {

	// The world that this Critter belongs to
	private final Program program;
	private World world;
	private Hex hex;
	int direction;
	private Rule last;
	private int[] mem;
	public String[] memNames;
	public final int memlength;
	boolean isMating;
	String critterName = "";
	Verb.Type toExecute;

	/*
	 * 0 = memsize 1 = defense 2 = offense 3 = size 4 = energy 5 = pass number 6
	 * = tag 7 = posture
	 */

	public Critter(Program prog, int[] memAttributes, String[] memNames) {
		assert prog != null;
		assert memAttributes.length >= world.MIN_MEMORY;
		
		Random rand = new Random();
		program = prog;
		mem = memAttributes;
		this.memNames = memNames;
		direction = rand.nextInt(6);
		memlength = mem.length;
		isMating = false;
	}

	public void assignHex(Hex hex) {
		world = hex.getWorld();
		this.hex = hex;
	}

	/**
	 * If forward is true, then Critter moves to the hex in front of it. Else,
	 * it moves the the hex behind it. If the target hex is a rock or occupied,
	 * the action fails.
	 * 
	 * @param forward
	 * @return true if successful.
	 */
	public boolean moveForward(boolean forward) {
		Hex newHex;
		if (forward) {
			newHex = getSurroundingHex(0, 1);
			P.rint("Critter is moving forward");
		} else {
			newHex = getSurroundingHex(3, 1);
			P.rint("Critter is moving backward");
		}
		if (newHex.isOccupied() || newHex.isRock()) {
			System.out
					.println("Destination hex is occupied. Critter move failed");
			return false;
		}

		hex.evictResident();
		Hex oldhex = hex;
		hex = newHex;
		assert (hex != oldhex);
		P.rint("~ Destination hex is set. Placing critter...");
		world.placeCritter(this, newHex);
		return true;
	}

	/**
	 * Removes the critter's affiliation from its world and its hex.
	 */
	public void die() {
		hex.evictResident();
		hex = null;
		world.removeCritter(this);
		world = null;
	}

	public void setNextVerb(Verb.Type vb) {
		toExecute = vb;
		Random r = new Random();
		
	}

	/**
	 * Advances the state of this critter one time step forward.
	 */
	public void step() {
		Verb mrVerb;
		if (toExecute == null) {
			mrVerb = new Verb(this);
			boolean isAction = false;
			// make sure to take an action or 999 updates
			if (world == null || hex == null) { // This critter is a ghost...
				// assert (world == null && hex == null);
				P.rint("This critter is dead."
						+ " Nothing should happen here.");
				return;
			}
			P.rint("~Critter is initializing step");

			while (!isAction && mem[5] < world.MAX_RULES_PER_TURN) {

				mem[5]++; // incrementing the PASS

				for (int i = 0; i < program.getRules().size(); i++) {

					Rule thisRule = program.getRules().get(i);

					// If condition is true, start goin down updates and
					// then execute the action if it is not null.
					if (thisRule.getCondition().eval(this)) {
						last = thisRule;
						P.rint("  ~This Condition IS true");
						// First go through the updates if any
						for (Update u : thisRule.getUpdates()) {
							int index = u.getIdx().eval(this);
							int value = u.getVal().eval(this);
							if (index >= 7 && index < mem[0]) {
								assert index < mem.length;
								System.out
										.println("~Critter update being attempted.");
								if (index == 7 && (value < 0 || value > 99)) {
									System.out
											.println("    >>Invalid arguments. "
													+ "Update failed.");
								} else {
									System.out
											.println(" ~Critter update successful! "
													+ "mem["
													+ index
													+ "] := "
													+ value);
									mem[index] = value;
								}
							} else {
								P.rint("    >>Invalid arguments. "
										+ "Update failed.");
							}
						}// end of update if body
							// Then execute the action, if any
						if (thisRule.getCommand() != null) {
							isAction = true; // action is taken and step is over
							StringBuffer sb = new StringBuffer();
							thisRule.getCommand().prettyPrint(sb);
							P.rint("~Handling action: " + sb);
							// if it is a UnaryAction

							executeAction(mrVerb, thisRule.getCommand());

						}// end of action if body

						// then break the for loop. need to test to make
						// sure eclipse doesn't break the while loop
						break;
					}// end of rule if body

					else if (!thisRule.getCondition().eval(this)) {
						System.out
								.println("    ~Condition not true, nothing happens");
					} else {

						System.out
								.println("probably won't need to but HANDLE THIS");
					}// end of rule if body
				}// end of for loop body
			}// end of while loop body
				// we did 999 updates or we found an Action
			mem[5] = 0;
			if (!isAction) { // if we didn't find an Action after all

				mrVerb.handleUnVerb(Verb.Type.WAIT);
			}
		} else { // toExecute NOT null
			if (toExecute == Verb.Type.SERVE) {
				executeUnaryAction(toExecute, (new Random()).nextInt(this.getMem(4)));
				// we serve a random amount of food, for now...
			} else if (toExecute == Verb.Type.TAG) {
				executeUnaryAction(toExecute, (new Random()).nextInt(100));
			} else {
				executeNullaryAction(toExecute);
			}
		}
		toExecute = null;
	}

	private void executeAction(Verb b, Command c) {
		if (c instanceof UnaryAction) {

			UnaryAction action = (UnaryAction) c;
			UnaryAction.Op unary = action.getOp();
			int index = action.getExpr().eval(this);

			executeUnaryAction(unary, index);

		}// end of UnaryAction if body
			// if it is a NullaryAction
		else if (c instanceof NullaryAction) {

			NullaryAction action = (NullaryAction) c;
			NullaryAction.Op nullary = action.getOp();
			executeNullaryAction(nullary);

		}// end of nullary if body
	}
	
	private void executeAction(Verb b) {
		
	}

	private void executeUnaryAction(UnaryAction.Op unary, int index) {
		Verb mrVerb = new Verb(this);
		switch (unary) {
		case SERVE:
			P.rint("  ~Critter initializing serve...");
			mrVerb.handleNuVerb(Verb.Type.SERVE, index);
			break;
		case TAG:
			P.rint("  ~Critter initializing tag...");
			mrVerb.handleNuVerb(Verb.Type.TAG, index);
			break;
		default:
			P.rint("HANDLE THIS UNARY ACTION");
			throw new AssertionError();
		}
	}

	private void executeUnaryAction(Verb.Type type, int index) {
		Verb mrVerb = new Verb(this);
		switch (type) {
		case SERVE:
			P.rint("  ~Critter initializing serve...");
			mrVerb.handleNuVerb(Verb.Type.SERVE, index);
			break;
		case TAG:
			P.rint("  ~Critter initializing tag...");
			mrVerb.handleNuVerb(Verb.Type.TAG, index);
			break;
		default:
			P.rint("HANDLE THIS UNARY ACTION");
			throw new AssertionError();
		}
	}

	private void executeNullaryAction(NullaryAction.Op nullary) {
		Verb mrVerb = new Verb(this);

		switch (nullary) {
		case ATTACK:
			P.rint("  ~Critter is initializing attack...");
			mrVerb.handleUnVerb(Verb.Type.ATTACK);
			break;
		case BACKWARD:
			P.rint("  ~Critter is initializing backward...");
			mrVerb.handleUnVerb(Verb.Type.BACKWARD);
			break;
		case BUD:
			P.rint("  ~Critter is attempting to bud...");
			mrVerb.handleUnVerb(Verb.Type.BUD);
			break;
		case EAT:
			P.rint("  ~Critter is attempting to eat...");
			mrVerb.handleUnVerb(Verb.Type.EAT);
			break;
		case FORWARD:
			P.rint("  ~Critter is initializing forward...");
			mrVerb.handleUnVerb(Verb.Type.FORWARD);
			break;
		case GROW:
			P.rint("  ~Critter is intitialzing grow...");
			mrVerb.handleUnVerb(Verb.Type.GROW);
			break;
		case LEFT:
			P.rint("  ~Critter is attempting to turn left...");
			mrVerb.handleUnVerb(Verb.Type.LEFT);
			break;
		case MATE:
			P.rint("  ~Critter is attempting to mate...");
			mrVerb.handleUnVerb(Verb.Type.MATE);
			break;
		case RIGHT:
			P.rint("  ~Critter is attempting to turn right...");
			mrVerb.handleUnVerb(Verb.Type.RIGHT);
			break;
		case WAIT:
			P.rint("  ~Critter is waiting.");
			mrVerb.handleUnVerb(Verb.Type.WAIT);
			break;
		default:
			P.rint("HANDLE THIS DEFATAL");
			throw new AssertionError();
		}// end of switch statement
	}

	private void executeNullaryAction(Verb.Type type) {
		Verb mrVerb = new Verb(this);

		switch (type) {
		case ATTACK:
			P.rint("  ~Critter is initializing attack...");
			mrVerb.handleUnVerb(Verb.Type.ATTACK);
			break;
		case BACKWARD:
			P.rint("  ~Critter is initializing backward...");
			mrVerb.handleUnVerb(Verb.Type.BACKWARD);
			break;
		case BUD:
			P.rint("  ~Critter is attempting to bud...");
			mrVerb.handleUnVerb(Verb.Type.BUD);
			break;
		case EAT:
			P.rint("  ~Critter is attempting to eat...");
			mrVerb.handleUnVerb(Verb.Type.EAT);
			break;
		case FORWARD:
			P.rint("  ~Critter is initializing forward...");
			mrVerb.handleUnVerb(Verb.Type.FORWARD);
			break;
		case GROW:
			P.rint("  ~Critter is intitialzing grow...");
			mrVerb.handleUnVerb(Verb.Type.GROW);
			break;
		case LEFT:
			P.rint("  ~Critter is attempting to turn left...");
			mrVerb.handleUnVerb(Verb.Type.LEFT);
			break;
		case MATE:
			P.rint("  ~Critter is attempting to mate...");
			mrVerb.handleUnVerb(Verb.Type.MATE);
			break;
		case RIGHT:
			P.rint("  ~Critter is attempting to turn right...");
			mrVerb.handleUnVerb(Verb.Type.RIGHT);
			break;
		case WAIT:
			P.rint("  ~Critter is waiting.");
			mrVerb.handleUnVerb(Verb.Type.WAIT);
			break;
		default:
			P.rint("HANDLE THIS DEFATAL");
			throw new AssertionError();
		}// end of switch statement
	}

	/**
	 * Prints out the information of this critter to standard output
	 */
	public void printInfo() {

		P.rint("| Resident Critter: " + critterName);

		P.rint("|  Attributes");// + "\n|  > memsize: " + mem[0]
		// + "\n|  > defense: " + mem[1] + "\n|  > offense: " + mem[2]
		// + "\n|  > size: " + mem[3] + "\n|  > energy: " + mem[4]
		// + "\n|  > pass: " + mem[5] + "\n|  > tag: " + mem[6]
		// + "\n|  > posture: " + mem[7]);
		// for (int i = World.MIN_MEMORY; i < mem.length; i++) {
		// P.rint("|  > mem-slot " + i + ": " + mem[i]);
		// }

		for (int i = 0; i < mem.length; i++) {
			P.rint("|  > " + memNames[i] + " " + mem[i]);
		}

		if (last != null) {
			StringBuffer rule = new StringBuffer();
			last.prettyPrint(rule);
			sugarMomma(rule);
			P.rint("|\n|  Last Rule Executed: \n|  > "
					+ rule.substring(1));
		} else {
			System.out
					.println("|\n|  This critter has not executed any rules yet");
		}
		System.out
				.println("|\n|  "
						+ "=================| Resident Critter's Rules |================\n|");
		StringBuffer sb = new StringBuffer();
		// P.rint("| ~Generating pretty printed rules...");
		program.prettyPrint(sb);
		// P.rint("| ~Sugaring up the string buffer...");
		sugarMomma(sb);
		P.rint(sb);
		P.rint("|");
	}

	/**
	 * 
	 * @return StringBuffer
	 */
	public StringBuffer getInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append(" > File name: " + critterName + "\n" + " > Attributes: \n");
		for (int i = 0; i < mem.length; i++) {
			sb.append(" > " + memNames[i] + " " + mem[i] + "\n");
		}
		if (last != null) {
			StringBuffer rule = new StringBuffer();
			last.prettyPrint(rule);
			sugarMomma(rule);
			sb.append("\n  Last Rule Executed: \n  > " + rule.substring(1));
		} else {
			sb.append("\nThis critter hasn't executed a rule yet\n");
		}

		StringBuffer rules = new StringBuffer();
		program.prettyPrint(rules);
		sugarMomma(rules);
		sb.append(rules);
		return sb;
	}

	/**
	 * Optional method to set the name of this Critter. Not a big deal
	 * 
	 * @param name
	 */
	public void setName(String name) {
		if (name.lastIndexOf("\\") != -1)
			critterName = name.substring(name.lastIndexOf("\\") + 1);
		else if (name.lastIndexOf("/") != -1)
			critterName = name.substring(name.lastIndexOf("/") + 1);
		else
			critterName = name;
	}

	/**
	 * Getter functions for the different memory locations [0:mem] [1:def]
	 * [2:off] [3:size] [4:nrg] [5:pass] [6:tag] [7:pos] Defined for at least
	 * 0..7
	 * 
	 * @return -1 if that memory location is not defined
	 */
	public int getMem(int index) {
		if (!(index >= 0 && index <= mem.length))
			return -1;
		else
			return mem[index];
	}

	public String[] getMemNames() {
		return memNames;
	}

	/**
	 * Setter function for size. Condition: Must be greater than or equal to 1.
	 * does nothing if Condition is not true.
	 */
	public void setSize(int size) {
		if (size >= 1)
			mem[3] = size;
	}

	/**
	 * Setter function for energy. Condition: Must be greater than or equal to
	 * 1. Condition 2: Must not exceed the max energy the Critter may have. does
	 * nothing if Condition is not true.
	 */
	public void setEnergy(int nrg) {
		if (nrg >= 1 && 
				nrg <= mem[3] * world.ENERGY_PER_SIZE) {
			// we make sure the nrg is within bounds
			mem[4] = nrg;
		} else if (nrg > mem[3] * world.ENERGY_PER_SIZE) {
			P.rint("=Max energy reached. ");
			mem[4] = mem[3] * world.ENERGY_PER_SIZE; // max energy
		}
	}

	/**
	 * Setter function for tag. Condition: Must be between 0 and 99. does
	 * nothing if Condition is not true.
	 */
	public void setTag(int tag) {
		if (tag >= 0 && tag <= 99)
			mem[6] = tag;
	}

	/**
	 * Getter for critter complexity.
	 * 
	 * @return int representing complexity
	 */
	public int getComplexity() { // we recalculate complexity to be most updated
		int complexity = program.getRules().size() * world.RULE_COST
				+ (mem[2] + mem[1]) * world.ABILITY_COST;
		return complexity;
	}

	/**
	 * Getter for the column number.
	 * 
	 * @return the column number
	 */
	public int getCol() {
		return hex.getCol();
	}

	/**
	 * Getter for the row number.
	 * 
	 * @return the row number
	 */
	public int getRow() {
		return hex.getRow();
	}

	/**
	 * Getter for the direction.
	 * 
	 * @return the direction
	 */
	public int getDir() {
		return direction;
	}

	/**
	 * Sets absolute direction according to the direction indicated.
	 * 
	 * @param dir
	 */
	public void setDir(int dir) {
		while (dir < 0)
			dir += 6;
		direction = dir % 6;
		if (world != null) {
			world.notifyOfCritter(this);
		}
	}

	/**
	 * Returns the appearance of this Critter.
	 * 
	 * @return the appearance of this Critter
	 */
	public int getAppearance() {
		// return the formula for size.
		int appearance = 100000 * mem[6];
		appearance += 1000 * mem[3];
		appearance += 10 * mem[7];
		appearance += direction;
		return appearance;
	}

	/**
	 * Return an integer describing the contents of the adjacent Hex in the
	 * given direction.
	 * 
	 * @return 0 if the hex is empty, -1 if a rock, the appearance of the
	 *         resident Critter if there is one, or n < -1 where the total
	 *         amount of food is equal to -(n + 1)
	 */
	public int getSense(int dir) {

		Hex theHex = getSurroundingHex(dir, 1);
		if (theHex.isOccupied()) {
			return theHex.getResident().getAppearance();
		} else if (theHex.isRock()) {
			return -1;
		} else {
			// return some other integer value for food.
			if (theHex.getCheerios() == 0)
				return 0;
			return -1 * (theHex.getCheerios() + 1);
		}
	}

	/**
	 * Returns the world in which the Critter resides.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Returns the program which the Critter is based on.
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * Return an integer describing the contents of the Hex in front of the
	 * critter dist Hexes away.
	 * 
	 * @return 0 if the hex is empty, -1 if a rock, the appearance of the
	 *         resident Critter if there is one, or n < -1 where the total
	 *         amount of food is equal to -(n + 1)
	 */
	public int getAhead(int dist) {

		boolean ignoreCritter = dist < 0;

		if (ignoreCritter)
			dist = (-1 * dist) - 1;

		Hex theHex = getSurroundingHex(0, dist);

		if (theHex.isOccupied() && !ignoreCritter) {
			return theHex.getResident().getAppearance();
		} else if (theHex.isRock()) {
			return world.ROCK_VALUE;
		} else { // is neither rock nor occupied
			if (theHex.getCheerios() == 0)
				return 0;
			return -1 * (theHex.getCheerios() + 1);
		}
	}

	/**
	 * The Critter Checks out its Hex.
	 * 
	 * @return the current Hex the Critter is on.
	 */
	public Hex getHex() {
		return hex;
	}

	/**
	 * The Critter checks out the Hex in front of Him.
	 * 
	 * @return a Hex.
	 */
	public Hex frontHex() {
		return getSurroundingHex(0, 1);
	}

	/**
	 * Get the Hex behind the current Critter.
	 */
	public Hex backHex() {
		return getSurroundingHex(3, 1);
	}

	/**
	 * Returns the Hex that is in the direction given by int dir, relative to
	 * the critter's original direction.
	 * 
	 * @param n
	 * @return the hex as indicated by the parameters. Returns the hex at (0,0)
	 *         if the given hex is out of bounds.
	 */
	private Hex getSurroundingHex(int dir, int dist) {
		int absoluteDirection = (dir + direction) % 6;

		int destRow = hex.getRow();
		int destCol = hex.getCol();

		if (absoluteDirection == 0) {
			destRow += dist;
		} else if (absoluteDirection == 1) {
			destCol += dist;
			destRow += dist;
		} else if (absoluteDirection == 2) {
			destCol += dist;
		} else if (absoluteDirection == 3) {
			destRow -= dist;
		} else if (absoluteDirection == 4) {
			destCol -= dist;
			destRow -= dist;
		} else if (absoluteDirection == 5) {
			destCol -= dist;
		}

		if (destRow > world.MAX_ROW + 2 || destCol > world.MAX_COLUMN + 2
				|| destRow < 0 || destCol < 0) {
			P.rint("    >>ERROR: tried to access a"
					+ " hex that is out of bounds.");
			return world.getHexes()[0][0];
		}

		Hex theHex = world.getHexes()[destCol][destRow];

		return theHex;
	}

	// when a critter buds or mates:
	// treat it as a failed budding, and subtract energy.
	// or, attempt to put the new critter on an alternate hex.

	// World files should not specify critters that share the same hex
	// When adding critters: test that no critters try to go to hexes that are
	// already taken.

	/**
	 * Goes through the StringBuffer sb and replaces all mem[x] with their
	 * sugary sweetness pimpnames if they exist. only if they exist.
	 * 
	 * ;)
	 * 
	 * @param sb
	 */
	private static void sugarMomma(StringBuffer sb) {
		char[] dst = new char[7];
		for (int i = 0; i < sb.length() - 5; i++) {
			sb.getChars(i, i + 6, dst, 0);
			if (dst[0] == 109 && dst[1] == 101 && dst[2] == 109 && dst[5] == 93) {
				// we expect dst[4] to be an integer.
				int mem = Character.getNumericValue(dst[4]);
				if (mem == 0) {
					sb.replace(i, i + 6, "MEMSIZE");
				} else if (mem == 1) {
					sb.replace(i, i + 6, "DEFENSE");
				} else if (mem == 2) {
					sb.replace(i, i + 6, "OFFENSE");
				} else if (mem == 3) {
					sb.replace(i, i + 6, "SIZE");
				} else if (mem == 4) {
					sb.replace(i, i + 6, "ENERGY");
				} else if (mem == 5) {
					sb.replace(i, i + 6, "PASS");
				} else if (mem == 6) {
					sb.replace(i, i + 6, "TAG");
				} else if (mem == 7) {
					sb.replace(i, i + 6, "POSTURE");
				}
			}
		}
		// P.rint("| ~Mastering whitespace...");
		whiteSpaceMaster(sb);
	}

	/**
	 * Goes through StringBuffer sb and makes everything all lined up and pretty
	 * looking and stuff.
	 * 
	 * @param sb
	 */
	private static void whiteSpaceMaster(StringBuffer sb) {
		int rule = sb.indexOf("-->", 0);
		int longest = 33;
		while (true) { // WHILE LOOP 1
			int lnbreak = sb.indexOf("\n", rule);
			if (lnbreak == -1)
				break;
			rule = sb.indexOf("-->", lnbreak);
			if (rule == -1)
				break;
			// THIS IS WHEN THE LEFT SIDE IS TOO BIGGGG
			if (rule - lnbreak >= 33) {
				int newbreak = sb.indexOf("and", lnbreak);
				if (newbreak == -1)
					newbreak = sb.indexOf("or", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf(">", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("<", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("<=", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf(">=", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("=", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("!=", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("+", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("-", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("*", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("/", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("mod", lnbreak);
				// it has to be one of these. there is no else statement because
				// there is no reason for the string to be long if it doesn't
				// have one of these bastards in them.
				sb.insert(sb.indexOf(" ", newbreak) + 1, "#\n|");
				rule = newbreak;
				continue;
			}

			// THIS IS WHEN THE RIGHT SIDE IS TOO UNAACCEEPTABLBLLLEEEL
			if (sb.indexOf("\n", rule) - rule >= 40) {
				int newbreak = sb.indexOf(":=", lnbreak);
				if (newbreak == -1)
					newbreak = sb.indexOf("+", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("-", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("*", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("/", lnbreak);
				else if (newbreak == -1)
					newbreak = sb.indexOf("mod", lnbreak);
				// it has to be one of these. there is no else statement because
				// there is no reason for the string to be long if it doesn't
				// have one of these bastards in them.
				sb.insert(sb.indexOf(" ", newbreak) + 1, "$ ");
				rule = sb.indexOf("$", rule);
				continue;
			}
		}

		int lnbreak = 0;
		while (true) { // WHILE LOOP 2
			rule = sb.indexOf("-->", lnbreak);
			int hash = sb.indexOf("#", lnbreak);
			if (rule == -1 && hash == -1)
				break;
			if (hash < rule && hash != -1)
				rule = hash;
			int leftlength = rule - lnbreak;
			if (leftlength < longest) {
				sb.insert(lnbreak + 1, " ");
				continue;
			}
			lnbreak = sb.indexOf("\n", rule);
			if (lnbreak == -1)
				break;
		}

		while (sb.indexOf("#") != -1) { // WHILE LOOP 3
			sb.deleteCharAt(sb.indexOf("#"));
		}

		int george = sb.indexOf("$");
		while (george != -1) { // WHILE LOOP 4
			sb.replace(george, george + 1, "\n");
			for (int i = 0; i < longest + 2; i++)
				sb.insert(george + 1, " ");
			george = sb.indexOf("$");
		}

		// int linebreak = sb.indexOf("\n");
		// while (linebreak != -1) { // WHILE LOOP 5
		// if (sb.charAt(linebreak + 1) != 124) {
		// sb.insert(linebreak + 1, "|");
		// }
		// linebreak = sb.indexOf("\n", linebreak + 2);
		// }
	}
}
