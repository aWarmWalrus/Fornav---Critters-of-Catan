package student;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Random;

import util.P;

public class World extends Observable {

	private Hex[][] hexes;
	private ArrayList<Critter> critters;
	ArrayList<Critter> deathNote;
	private int step;
	final private String constants = ".\\assignment6\\constants.txt";
	int rockPop;
	int critPop;
	final int maxspace;
	
	
	/**
	 * Constructs a brand new world with no critters.
	 */
	public World(boolean fromFile){
		P.rototyping = false;
		setConstants();
		maxspace = (MAX_COLUMN + 3) * (MAX_ROW + 3);
		step = 0;
		rockPop = 0;
		critPop = 0;
		hexes = new Hex[MAX_COLUMN + 3][MAX_ROW + 3];
		critters = new ArrayList<Critter>();
		populate(fromFile);
	}
	
	protected World(boolean fromFile, int maxCol, int maxRow) {
		
		setConstants();
		MAX_COLUMN = maxCol;
		MAX_ROW = maxRow;
		maxspace = (MAX_COLUMN + 3) * (MAX_ROW + 3);
		step = 0;
		rockPop = 0;
		critPop = 0;
		hexes = new Hex[MAX_COLUMN + 3][MAX_ROW + 3];
		critters = new ArrayList<Critter>();
		populate(fromFile);
		
	}
	
	/**
	 * Advances the world one time step.
	 */
	public void step() {

		step++;
		deathNote = new ArrayList<Critter>();
		for(Critter c : critters){
			synchronized(c){
				c.step();
			}
		}
		for(Critter d : deathNote) {
			critters.remove(d);
			P.rint("Critter permanently removed");
		}
	}
	
	/**
	 * Returns the step that the world is at.
	 * @return
	 */
	public int getStep() {
		return step;
	}
	
	/**
	 * Returns the number of empty hexes in this world
	 * @return
	 */
	public int getSpace() {
		return maxspace - (rockPop + critPop);
	}
	
	/**
	 * Gives access to this world's array list of critters
	 * @return critters
	 */
	public ArrayList<Critter> getCritters(){
		return critters;
	}
	
	/**
	 * Gives access to this world's array list of dead critters
	 * @return removed critters
	 */
	public ArrayList<Critter> getDead() {
		return deathNote;
	}

	/**
	 * Returns the nested array of hexes.
	 * 
	 * @return the nested array of hexes.
	 */
	public Hex[][] getHexes() {
		return hexes;
	}

	/**
	 * Returns the hex with given coordinates [col+1][row+1]
	 * 
	 * @param col
	 * @param row
	 * @return the hex with the given coordinates
	 */
	public Hex getHex(int col, int row) {
		return hexes[col][row];
	}

	/**
	 * Prints a representation of the world to the system's standard output
	 * 
	 */
	public void printInfo() {

		HashMap<Integer, StringBuffer> lines = 
				new HashMap<Integer, StringBuffer>();
		
		// Part 1: store all the information as symbols in StringBuffers
		int dim = (MAX_COLUMN + 2) + ((MAX_ROW + 2) * 2 + 1);
		for (int col = 0; col <= MAX_COLUMN + 2; col++) {
			for (int row = 0; row <= MAX_ROW + 2; row++) {
				int lineno = 2 * ((MAX_ROW + 2) - row) + col;
				String hex = "- ";
				if (hexes[col][row].isRock())
					hex = "# ";
				else if (hexes[col][row].getCheerios() > 0) {
					hex = "F ";
					if (hexes[col][row].isOccupied())
						hex = "G" + hexes[col][row].getResident().direction;
				} else if (hexes[col][row].isOccupied())
					hex = "C" + hexes[col][row].getResident().direction;

				if (lines.containsKey(lineno)) {// && !(col == MAX_COLUMN + 2))
					lines.get(lineno).append("  " + hex);
				} else if (col == 0) {
					lines.put(lineno, new StringBuffer("  " + hex));// " "));
				} else if (col == 1) {
					lines.put(lineno, new StringBuffer("    " + hex));// " " +
																		// hex));
				}
			}
		}

		// Part 2: print out the beautiful information
		P.rint("\nINFORMATION: ");
		P.rint("Number of time steps elapsed: " + step);
		P.rint("Number of critters: " + critters.size() + "\n");

		for (int c = 0; c < dim; c++) {
			if (c - 1 > MAX_COLUMN && c < dim - MAX_COLUMN - 2) {
				// if(c > MAX_COLUMN + 1 !!This doesn't include the rock border
				// && c < dim - MAX_COLUMN - 3){ !!changes also in part 1
				// necessary
				P.rint(lines.get(c).toString());
			}
		}
		P.rint("\n==========================================\n");
	}

	/**
	 * Prints out the information of the hex at the given coordinates
	 * 
	 * @param c
	 * @param r
	 */
	public void printHexInfo(int c, int r) {
		if (c > MAX_COLUMN || r > MAX_ROW || c < 0 || r < 0)
			P.rint("ERROR: designated Hex does not exist: "
					+ "(" + c + ", " + r + ")");
		else {
			hexes[c + 1][r + 1].printInfo();
		}
	}
	
	/**
	 * Randomly places n Critters defined by the file, filename
	 * @param filename
	 * @param n
	 * @return 1 if critter not placed, 0 if critter is successfully placed
	 */
	public int addCritter(Critter critter) {
			
			Random rand = new Random();
			int col = rand.nextInt(MAX_COLUMN + 1);
			int row = rand.nextInt(MAX_ROW + 1);
			Hex thisHex = hexes[col + 1][row + 1];
			
			//Brute method to check when trying to add critters to a full world
			boolean full = rockPop + critters.size() >= maxspace; 
			while (thisHex.isOccupied() || thisHex.isRock()) {
				col = rand.nextInt(MAX_COLUMN + 1);
				row = rand.nextInt(MAX_ROW + 1);
				thisHex = hexes[col + 1][row + 1];
				if (full) {
//					P.rint("Oops world is full. Critter not placed!");
					return 1;
				}
			}

			placeCritter(critter, thisHex);
			return 0;
	}
	
	/**
	 * Places a critter on the given hex. Requires that the designated hex
	 * be a part of this world. Checks that this hex is unoccupied and not
	 * a rock
	 * @param critter
	 * @param hex
	 */
	public void placeCritter(Critter critter, Hex hex) { 
		assert hex.getWorld() == this;
		if(!hex.isOccupied() && !hex.isRock()) {
			if (!critters.contains(critter)) critters.add(critter);
			//else notifyObservers(critter.getHex()); //animation purposes later on
			//if critter already exists this keeps track of the old position
			hex.newResident(critter);
			critter.assignHex(hex);
			P.rint("notifying View of Critter placement (World)");
			setChanged();
			notifyObservers(critter); //this will draw the critter no matter what
		} else {
			P.rint("ERROR: tried to place a Critter on the" +
					"occupied hex, (" + hex.getCol() + "," + hex.getRow() +
					")\nCritter placement failed");
		}
	}
	
	/**
	 * Deletes an existing critter from the list to no longer track. 
	 * This method can ONLY be called in the context of a critter's step
	 * so when a critter dies
	 * @param c is a Critter.
	 */
	public void removeCritter(Critter c) {
		//sloppy way to evict the critter from its hex
		deathNote.add(c);
		P.rint("critter is being removed (World) ");
		setChanged();
		notifyObservers(c);
	}
	
	/**
	 * Populates the world with hexes. hexes[][] will be full after populate 
	 * is done. If load is true, then rocks will not be generated. Else
	 * rocks will be randomly placed on the board.
	 * 
	 * @param load
	 */
	private void populate(boolean load) {
		// build the hexes.
		for (int col = 0; col <= MAX_COLUMN + 2; col++) {
			for (int row = 0; row <= MAX_ROW + 2; row++) {

				Random rand = new Random();

				// populate the border with rocks.
				if (!(col - 1 <= (row - 1) * 2) || // The South Border
						!((col - 1) + (2 * MAX_ROW) - 
								MAX_COLUMN >= (row - 1) * 2)||
						// The North Border CHECK THESE BORDERS
						!(col - 1 >= 0) || // The West Border
						!(col - 1 <= MAX_COLUMN)) // The East Border
					hexes[col][row] = new Hex(col, row, true, this);

				// 10% of hexes will be rocks if not loading from file
				else if (!load && rand.nextDouble() <= ROCK_DENS)
					hexes[col][row] = new Hex(col, row, true, this);

				// case when the hex is already built upon;
				else if (hexes[col][row] == null)
					hexes[col][row] = new Hex(col, row, false, this);

				// case when there is something on the hex.
				else if (hexes[col][row].isOccupied()) {
					P.rint("HANDLE THIS");
				}

				// case when there is a rock on the hex.
				else if (hexes[col][row].isRock()) {
					P.rint("HANDLE THIS");
				} else
					P.rint("THIS CASE IS NOT ACCOUNTED FOR?SD?F");

			}
		}
	}
	
	private void setConstants() {
		String line = "";
		try {
			BufferedReader butt = new BufferedReader(new FileReader(constants));
			line = butt.readLine();
			while (line != null) {
				String[] fields = line.split(" ");
				if (fields[0].equals("BASE_DAMAGE"))
					BASE_DAMAGE = Integer.parseInt(fields[1]);
				else if (fields[0].equals("DAMAGE_INC"))
					DAMAGE_INC = Double.parseDouble(fields[1]);
				else if (fields[0].equals("ENERGY_PER_SIZE"))
					ENERGY_PER_SIZE = Integer.parseInt(fields[1]);
				else if (fields[0].equals("FOOD_PER_SIZE"))
					FOOD_PER_SIZE = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MAX_SMELL_DISTANCE"))
					MAX_SMELL_DISTANCE = Integer.parseInt(fields[1]);
				else if (fields[0].equals("ROCK_VALUE"))
					ROCK_VALUE = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MAX_COLUMN"))
					MAX_COLUMN = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MAX_ROW"))
					MAX_ROW = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MAX_RULES_PER_TURN"))
					MAX_RULES_PER_TURN = Integer.parseInt(fields[1]);
				else if (fields[0].equals("SOLAR_FLUX"))
					SOLAR_FLUX = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MOVE_COST"))
					MOVE_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("ATTACK_COST"))
					ATTACK_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("GROW_COST"))
					GROW_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("BUD_COST"))
					BUD_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MATE_COST"))
					MATE_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("RULE_COST"))
					RULE_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("ABILITY_COST"))
					ABILITY_COST = Integer.parseInt(fields[1]);
				else if (fields[0].equals("INITIAL_ENERGY"))
					INITIAL_ENERGY = Integer.parseInt(fields[1]);
				else if (fields[0].equals("MIN_MEMORY"))
					MIN_MEMORY = Integer.parseInt(fields[1]);
				else
					System.out
							.println("    >>constants.txt has invalid input: "
									+ line);

				line = butt.readLine();
			}
			butt.close();
		} catch (NumberFormatException e) {
			P.rint("    >>constants.txt is messed up son." + line);
		} catch (FileNotFoundException e) {
			P.rint("    >>constants.txt not found. Default constants used.");
		} catch (IOException e) {
			P.rint("    >>constants.txt is an empty file");
		} 
	}
	
	public void notifyOfHex(Hex theHex) {
		setChanged();
		notifyObservers(theHex);
	}
	
	public void notifyOfCritter(Critter theCritter) {
		setChanged();
		notifyObservers(theCritter);
	}

	// =================$$$$$$$$$$$ CONSTANTS $$$$$$$$$$$$=============== \\

	public int BASE_DAMAGE = 100; 
//	100 {The multiplier for all damage done by attacking}
	public double DAMAGE_INC = 0.2;
//	0.2 {Controls how quickly increased offensive or defensive ability affects damage}
	public int ENERGY_PER_SIZE = 500;
//	500 {How much energy a critter can have per point of size}
	public int FOOD_PER_SIZE= 200;
//	200 {How much food is created per point of size when a critter dies}
	public int MAX_SMELL_DISTANCE = 10;
//	10 {Maximum distance at which food can be sensed}
	public int ROCK_VALUE = -1;
//	-1 {The value reported when a rock is sensed}
	public int MAX_COLUMN = 10;
//	49 {Maximum column index in the world map}
	public int MAX_ROW = 10;
//	67 {Maximum row index in the world map}
	public int MAX_RULES_PER_TURN = 999;
//	999 {The maximum number of rules that can be run per critter turn}
	public int SOLAR_FLUX = 1;
//	1 {Energy gained from sun by doing nothing}
	public int MOVE_COST = 3;
//	3 {Energy cost of moving (per unit size)}
	public int ATTACK_COST = 5;
//	5 {Energy cost of attacking (per unit size)}
	public int GROW_COST = 1;
//	1 {Energy cost of growing (per size and complexity)}
	public int BUD_COST = 9;
//	9 {Energy cost of budding (per unit complexity)}
	public int MATE_COST = 5;
//	5 {Energy cost of successful mating (per unit complexity)}
	public int RULE_COST = 2;
//	2 {Complexity cost of having a rule}
	public int ABILITY_COST = 25;
//	25 {Complexity cost of having an ability point}
	public int INITIAL_ENERGY = 250;
//	250 {Energy of a newly birthed critter}
	public int MIN_MEMORY = 8;
//	8 {Minimum number of memory entries in a critter}
	
	// my own constants
	public double ROCK_DENS = 0.2; // density of rocks within world.
	
	
	
	//JUNK CODE
	/**
	 * Prints all the hexes including the border hexes.
	 */
	public void printAllInfo() {
		HashMap<Integer, StringBuffer> lines = new HashMap<Integer, StringBuffer>();
		int dim = (MAX_COLUMN + 2) + ((MAX_ROW + 2) * 2 + 1);
		for (int col = 0; col <= MAX_COLUMN + 2; col++) {
			for (int row = 0; row <= MAX_ROW + 2; row++) {
				int lineno = 2 * ((MAX_ROW + 2) - row) + col;
				if (lines.containsKey(lineno)) {
					lines.get(lineno).append("     " + col + "," + row);
				} else if (col == 0) {
					lines.put(lineno, new StringBuffer(col + "," + row));
				} else if (col == 1) {
					lines.put(lineno,
							new StringBuffer("    " + col + "," + row));
				} else if (row == 0) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < col; i++) {
						sb.append("    ");
					}
					sb.append("" + col + "," + row);
					lines.put(lineno, sb);
				}
			}
		}

		for (int c = 0; c < dim; c++) {
			P.rint(lines.get(c).toString());
		}

		lines = new HashMap<Integer, StringBuffer>();
		for (int col = 0; col <= MAX_COLUMN + 2; col++) {
			for (int row = 0; row <= MAX_ROW + 2; row++) {
				int lineno = 2 * ((MAX_ROW + 2) - row) + col;
				String hex = "- ";
				if (hexes[col][row].isRock())
					hex = "# ";
				else if (hexes[col][row].getCheerios() > 0) {
					hex = "F" + hexes[col][row].getCheerios();
					if (hexes[col][row].isOccupied())
						hex = "G" + hexes[col][row].getResident().direction;
				} else if (hexes[col][row].isOccupied())
					hex = "C" + hexes[col][row].getResident().direction;
				if (lines.containsKey(lineno)) {
					lines.get(lineno).append("  " + hex);
				} else if (col == 0) {
					lines.put(lineno, new StringBuffer(hex));
				} else if (col == 1) {
					lines.put(lineno, new StringBuffer("  " + hex));
				} else if (row == 0) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < col; i++) {
						sb.append("  ");
					}
					sb.append(hex);
					lines.put(lineno, sb);
				}
			}
		}

		for (int c = 0; c < dim; c++) {
			P.rint(lines.get(c).toString());
		}
	}
}
