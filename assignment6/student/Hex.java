package student;

import java.util.Random;

import util.P;

public class Hex {

	final private boolean isRock;
	final private int row;
	final private int column;
	private int cheerios;
	private Critter resident;
	final private World world;

	public Hex(int c, int r, boolean isRock, World wor) {

		Random rand = new Random();
		world = wor;
		// I might put a function here to randomly make a Hex a rock instead of
		// it being assigned via the constructor.
		this.isRock = isRock;
		if (isRock) {
			world.rockPop++;
//			P.rint("RockPop = " + world.rockPop);
		}
		// check for validity of row and column
		row = r;
		column = c;
		resident = null;

		// Hex has a 25% chance of being initialized with some amount of food
		if (!isRock)
			cheerios = Math.max(0, rand.nextInt(100) - 75);
	}

	/**
	 * Returns a hex's col coordinate
	 * 
	 * @return column
	 */
	public int getCol() {
		return column;
	}

	/**
	 * Returns a hex's row coordinate
	 * 
	 * @return row
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Returns the world that this Hex belongs to.
	 * 
	 * @return world
	 */
	public World getWorld(){
		return world;
	}
	
	/**
	 * Add a Critter to this hex as a resident.
	 * 
	 * @param a
	 *            Critter
	 */
	public void newResident(Critter c) {
		if (!isOccupied()){
			resident = c;
			world.critPop++;
		}
		else
			P.rint("    >>Tried to place a critter in an"
					+ " occupied location," + column + ", " + row);
	}

	public void evictResident(){
		if (!isOccupied()){
			P.rint("    >>Woop! Tried to evict a nonexistent "
					+ "critter from this hex. CHECK YO INVARIANTS SON");
		} else {
			resident = null;
			world.critPop--;
		}
			
			
	}
	
	/**
	 * Returns true if there is a critter on this hex
	 * 
	 * @return true if this hex contains a critter
	 */
	public boolean isOccupied() {
		return resident != null;
	}

	/**
	 * Returns true if the hex is a rock.
	 * 
	 * @return true if the hex contains a rock
	 */
	public boolean isRock() {
		return isRock;
	}
	
	/**
	 * Returns true if the hex is not in the world.
	 * 
	 * @return
	 */
	public boolean isBorder() {
		int dim = (world.MAX_COLUMN + 2) + ((world.MAX_ROW + 2) * 2 + 1);
		int newRow = 2 * ((world.MAX_ROW + 2) - row) + column;
		return (column == 0 || column == world.MAX_COLUMN + 2 ||
				newRow <= world.MAX_COLUMN + 2 || 
				newRow >= dim - world.MAX_COLUMN - 3);
	}

	/**
	 * Returns the amount of food that is on this hex.
	 * 
	 * @return amount of food
	 */
	public int getCheerios() {
		return cheerios;
	}
	
	/**
	 * Sets the amount of food that is on this hex.
	 * 
	 * @return amount of food
	 */
	public void setCheerios(int actualFood) {
		cheerios = actualFood;
		world.notifyOfHex(this);
	}

	/**
	 * Returns the critter that is on this hex.
	 * 
	 * @return null if no critter is on this hex.
	 */
	public Critter getResident() {
		return resident;
	}

	public void printInfo() {
		P.rint("\n+-----|     INFORMATION     |-------"
				+ "\n|     | for Hex at: "
				+ "(" + (column - 1) + ", " + (row - 1) + ")\n|");
		
		if (isRock())
			P.rint("| This Hex is a Rock with no critters or cheerios");

		else {
			P.rint("| Food: " + cheerios + "\n|");

			if (isOccupied()) 
				resident.printInfo();			
			else
				P.rint("| This hex has no critter.");
		}
		P.rint("|\n+\n");
	}
}
