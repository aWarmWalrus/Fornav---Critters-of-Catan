package student;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import parse.Parser;
import parse.ParserFactory;
import util.P;
import ast.Program;

public class Creator extends Observable {

	private World thisWorld;
	private static Creator instance;
	
	private Creator() { }
	
	public static Creator getInstance(){
//		P.rint("Creator instance initialized");
		P.rototyping = false;
		if(instance == null){
			instance = new Creator();
			return instance;
		} else {
			return instance;
		}
	}
	
	/**
	 * Creator's current world steps forward by one time step. Requires that
	 * thisWorld not be null
	 */
	public void step() {
		assert (thisWorld != null);
		thisWorld.step();
	}
	
	/**
	 * Returns an integer describing the number of free spaces that are in this
	 * world
	 * @return
	 */
	public int getSpace() {
		return thisWorld.getSpace();
	}
	
	/**
	 * Returns the world that this Creator is in charge of
	 * @return
	 */
	public World getWorld() {
		return thisWorld;
	}
	
	/**
	 * Creator prints out the information of its current world. Requires that
	 * thisWorld not be null
	 */
	public void printInfo() {
		assert (thisWorld != null);
		thisWorld.printInfo();
	}

	/**
	 * Creator prints out information of the designated Hex within its current
	 * world. Requires that thisWorld not be null. The case that the given
	 * coordinates are invalid is handled by World.
	 * 
	 * @param c
	 * @param r
	 */
	public void printHexInfo(int c, int r) {
		assert (thisWorld != null);
		thisWorld.printHexInfo(c, r);
	}

	/**
	 * This Creator is given critters and is asked to put n critters into the
	 * world that it currently is in charge of. Requires that thisWorld not be
	 * null.
	 * 
	 * @param critter
	 * @param n
	 */
	public void addCritters(String critterFile, int n) {
		assert thisWorld != null;
		P.rint("Creator is adding " + n + " critters from "
				+ critterFile + "...");
		int failCounter = 0;
		try {
			for (int i = 0; i < n; i++) {
				failCounter += thisWorld.addCritter(newCritter(critterFile));
			}
			if (failCounter > 0)
				P.rint("    >>Max space reached. " + failCounter
						+ " critters were not added."
						+ " THIS SHOULDNT HAPPEN ANYMORE (Creator)");
		} catch (FileNotFoundException e) {
			P.rint("   >>Could not locate " + critterFile
					+ "\n     Critter placement failed.");
		} catch (IOException e) {
			// nothing. this is handled well in newCritter()
		}
	}
	
	/**
	 * Places a critter defined by the critterFile and places it at coordinates
	 * (col, row)
	 * 
	 * @param critterFile
	 * @param col
	 * @param row
	 */
	public void placeCritter(String critterFile, int col, int row) {
		assert thisWorld != null;
		if (getSpace() == 0) {
			P.rint("!!No more space left to place Critters (Creator)");
			return;
		}
		if(col > thisWorld.MAX_COLUMN || row > thisWorld.MAX_ROW ||
				col < 0 || row < 0) {
			P.rint("!!The given coordinates are out of bounds (Creator)");
			return;
		}
		Critter newCritter;
		try {
			newCritter = Creator.newCritter(critterFile);
		} catch (FileNotFoundException e) {
			P.rint("!!" + critterFile + " could not be found (Creator)");
			return;
		} catch (IOException e) {
			P.rint("idk...(Creator)");
			return;
		}
		Hex targetHex = thisWorld.getHex(col + 1, row + 1);
		if(targetHex.isOccupied() || targetHex.isRock()) {
			P.rint("!!The target hex is currently occupied.");
			return;
		}
		thisWorld.placeCritter(newCritter, targetHex);
		setChanged();
		P.rint("* Critter placed successfully");
	}

	/**
	 * Resets the current world with a new world that is passed to it as an
	 * argument
	 * 
	 * @param newWorld
	 */
	public void setWorld(World newWorld) {
		P.rint("Creator world set/reset");
		thisWorld = newWorld;
		setChanged();
		P.rint("setting the world (Creator)");
		notifyObservers(thisWorld);
	}

	/**
	 * Returns true if this Creator already has a world.
	 * 
	 * @return false if world has not been created yet.
	 */
	public boolean hasWorld() {
		return thisWorld != null;
	}

	/**
	 * Print out some more info than the normal printInfo method does.
	 */
	public void printAllInfo() {
		thisWorld.printAllInfo();
	}

	/**
	 * Statically creates a critter with the given program. The critter that it
	 * returns has no particular affiliation with any world or any hex.
	 * 
	 * @param crittertxt
	 * @return a new Critter
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Critter newCritter(String crittertxt)
			throws FileNotFoundException, IOException {

		BufferedReader script;
		try {
			script = new BufferedReader(new FileReader(crittertxt));
			if (!script.ready()) {
				script.close();
				throw new IOException();
			}
			Object[] mems = generateMem(script);
			int[] mem = (int[]) mems[0];
			String[] memNames = (String[]) mems[1];
			Parser parser = ParserFactory.getParser();
			Program prog = parser.parse(script);
			Critter critter = new Critter(prog, mem, memNames);
			critter.setName(crittertxt);
			script.close();
			return critter;
		} catch (FileNotFoundException e) {
			P.rint("ERROR: could not find: " + crittertxt);
			throw new FileNotFoundException();
		} catch (IOException e) {
			P.rint("   >>The provided critter file is faulty. "
					+ "Critter not initialized. We OUT homey");
			throw new IOException();
		}
	}

	/**
	 * Static method that returns a new world with randomly generated hexes and
	 * randomly placed rocks and food but no critters.
	 * 
	 * @return world
	 */
	public static World newWorld() {
		P.rint("Generating a new world...");
		World world = new World(false);
		return world;
	}

	/**
	 * Creates a new world from a given text file
	 * 
	 * @param worldtxt
	 * @throws IOException 
	 */
	public static World loadWorld(String worldtxt) throws IOException {
		World world = new World(true);
		P.rint("Generating a new world from " + worldtxt + "...");
		String error = "Error";

		try {

			BufferedReader script = new BufferedReader(new FileReader(worldtxt));
			String line = script.readLine();
			while (line != null) {

				String[] words = line.split(" ");
				// format: [string = rock] [int row] [int col]
				// create a rock at the given coordinates
				if (words[0].toLowerCase().equals("rock")) {

					if (words.length > 3){
						System.out
								.println("    >>Syntax Error: too many arguments:"
										+ line);
						throw new IOException();
					}

					int row = Integer.parseInt(words[1]);
					int col = Integer.parseInt(words[2]);
					error = row + ", " + col;

					if (row >= world.MAX_ROW || col >= world.MAX_COLUMN 
							|| row < 0 || col < 0) {
						// case when client gives bad coordinates
						P.rint("     >>Arguments out of bounds: "
								+ line);
						line = script.readLine();
						continue;
					}

					Hex thisHex = world.getHexes()[col + 1][row + 1];

					if (!thisHex.isOccupied())
						// case when the hex is actually empty
						world.getHexes()[col + 1][row + 1] = new Hex(col + 1,
								row + 1, true, world);

					else if (thisHex.isOccupied())
						P.rint("    >>The hex at (" + error
								+ ") is already occupied. Rock ignored.");

					else
						P.rint("    >>Case not accounted for");

				} else if (words[0].toLowerCase().equals("critter")) {
					// format:
					// [string=critter] [string file] [int r] [int c] [int dir]
					// create a critter at the given coordinates and direction

					if (words.length > 5){
						P.rint("    >>Syntax Error: "
								+ "too many arguments:" + line);
						throw new IOException();
					}

					int row = Integer.parseInt(words[2]);
					int col = Integer.parseInt(words[3]);
					int dir = Integer.parseInt(words[4]);
					error = col + " " + row + " " + dir;

					if (row >= world.MAX_ROW || col >= world.MAX_COLUMN
							|| row < 0 || col < 0) {
						// case when client gives bad coordinates
						P.rint("     >>Arguments out of bounds: "
								+ line);
						line = script.readLine();
						continue;
					}

					Critter critter = newCritter(words[1]);
					critter.setDir(dir);
					Hex thisHex = world.getHexes()[col + 1][row + 1];

					if (thisHex == null) {
						// case when the position is null
						P.rint("THIS POSITION SHOULD NEVER BE NULL.");
						world.getCritters().add(critter);
						Hex newHex = new Hex(col + 1, row + 1, false, world);
						newHex.newResident(critter);
						world.getHexes()[col + 1][row + 1] = newHex;
					}

					else if (thisHex.isOccupied() || thisHex.isRock())
						// case when the position is not occupied
						P.rint("    >>The hex at (" + row + ", "
								+ col + ") is already occupied. "
								+ "Critter not added");

					else if (!thisHex.isOccupied() && !thisHex.isRock()) {
						// case when hex is initialized and not occupied
						world.placeCritter(critter, thisHex);
					}

					else
						P.rint("    >>Case not accounted for");

				} else
					P.rint("    >>Syntax Error: " + line);

				line = script.readLine();

				// END WHILE
			}
			script.close();
			// END TRY
		} catch (FileNotFoundException e) {
			P.rint("    >>ERROR: could not find: " + worldtxt);
		} catch (IOException e) {
			P.rint("    >>IOException while reading " + worldtxt);
			throw new IOException();
		} catch (NumberFormatException e) {
			P.rint("    >>NumberFormatException: " + error);
			throw new IOException();
		}
		return world;
	}

	/**
	 * Loads a world, given the string representing the world, and the number of
	 * columns and rows
	 * 
	 * @return a newly initialized World
	 */
	public static World loadWorld(String worldtxt, int mrColumn, int mrRow) {
		World world = new World(true, mrColumn, mrRow);
		P.rint("*Generating a new world from " + worldtxt + "...");
		String error = "Error";

		try {

			BufferedReader script = new BufferedReader(new FileReader(worldtxt));
			String line = script.readLine();
			while (line != null) {

				String[] words = line.split(" ");
				// format: [string = rock] [int row] [int col]
				// create a rock at the given coordinates
				if (words[0].toLowerCase().equals("rock")) {

					if (words.length > 3)
						System.out
								.println("    >>Syntax Error: too many arguments:"
										+ line);

					int row = Integer.parseInt(words[1]);
					int col = Integer.parseInt(words[2]);
					error = row + ", " + col;

					if (row >= mrRow || col >= mrColumn || row < 0 || col < 0) {
						// case when client gives bad coordinates
						P.rint("     >>Arguments out of bounds: "
								+ line);
						line = script.readLine();
						continue;
					}

					Hex thisHex = world.getHexes()[col + 1][row + 1];

					if (!thisHex.isOccupied())
						// case when the hex is actually empty
						world.getHexes()[col + 1][row + 1] = new Hex(col + 1,
								row + 1, true, world);

					else if (thisHex.isOccupied())
						P.rint("    >>The hex at (" + error
								+ ") is already occupied. Rock ignored.");

					else
						P.rint("    >>Case not accounted for");

				} else if (words[0].toLowerCase().equals("critter")) {
					// format:
					// [string=critter] [string file] [int r] [int c] [int dir]
					// create a critter at the given coordinates and direction

					if (words.length > 5)
						P.rint("    >>Syntax Error: "
								+ "too many arguments:" + line);

					int row = Integer.parseInt(words[2]);
					int col = Integer.parseInt(words[3]);
					int dir = Integer.parseInt(words[4]);
					error = col + " " + row + " " + dir;

					if (row >= mrRow || col >= mrColumn || row < 0 || col < 0) {
						// case when client gives bad coordinates
						P.rint("     >>Arguments out of bounds: "
								+ line);
						line = script.readLine();
						continue;
					}

					Critter critter = newCritter(words[1]);
					critter.setDir(dir);
					Hex thisHex = world.getHexes()[col + 1][row + 1];

					if (thisHex == null) {
						// case when the position is null
						System.out
								.println("THIS POSITION SHOULD NEVER BE NULL.");
						world.getCritters().add(critter);
						Hex newHex = new Hex(col + 1, row + 1, false, world);
						newHex.newResident(critter);
						world.getHexes()[col + 1][row + 1] = newHex;
					}

					else if (thisHex.isOccupied() || thisHex.isRock())
						// case when the position is not occupied
						P.rint("    >>The hex at (" + row + ", "
								+ col + ") is already occupied. "
								+ "Critter not added");

					else if (!thisHex.isOccupied() && !thisHex.isRock()) {
						// case when hex is initialized and not occupied
						world.placeCritter(critter, thisHex);
					}

					else
						P.rint("    >>Case not accounted for");

				} else
					P.rint("    >>Syntax Error: " + line);

				line = script.readLine();

				// END WHILE
			}
			script.close();
			// END TRY
		} catch (FileNotFoundException e) {
			P.rint("    >>ERROR: could not find: " + worldtxt);
		} catch (IOException e) {
			P.rint("    >>IOException while reading " + worldtxt);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			P.rint("    >>NumberFormatException: " + error);
		}
		return world;
	}

	/**
	 * Reads the critter file and parses the first part of the file to create
	 * the Critter's mem attribute. 
	 * 
	 * @param script
	 * @return an Object array containing the int[] of mem attributes and the
	 * String[] of mem Names for customizable memory names.
	 * @throws IOException 
	 */
	private static Object[] generateMem(BufferedReader script)
			throws IOException {
		String line = "ERROR";
		try {
			ArrayList<String[]> lines = new ArrayList<String[]>();
			script.mark(1000);
			line = script.readLine();
			while (true) {
				String[] words = line.split(" ");
				if (!words[0].endsWith(":")) {
					script.reset();
					break;
				}
				lines.add(words);
				script.mark(1000);
				line = script.readLine();
			}
			int memsize = 0;
			for (String[] s : lines) 
				if (s[0].equals("memsize:"))
					memsize = Integer.parseInt(s[1]);
			if (memsize < 8) {
				P.rint("    >>ERROR: memsize is invalid: " + memsize);
				throw new IOException();
			}
			String[] memNames = new String[memsize];
			int[] mem = new int[memsize];
			//initialize every slot in mem and memNames
			for(int i = 0; i < memsize; i++) {
				mem[i] = 0;
				memNames[i] = "blank: ";
			}
			memNames[5] = "pass:";
			memNames[6] = "tag:";
			int extra = 8;
			for(String[] s : lines){
				int value = Integer.parseInt(s[1]);
				if(s[0].equals("memsize:")){
					memNames[0] = s[0];
					mem[0] = value;
					assert value == memsize;
				} else if (s[0].equals("defense:")) {
					memNames[1] = s[0];
					mem[1] = value;
				} else if (s[0].equals("offense:")) {
					memNames[2] = s[0];
					mem[2] = value;
				} else if (s[0].equals("size:")) {
					memNames[3] = s[0];
					mem[3] = value;
				} else if (s[0].equals("energy:")) {
					memNames[4] = s[0];
					mem[4] = value;
				} else if (s[0].equals("posture:")) {
					memNames[7] = s[0];
					mem[7] = value;
				} else if (extra < memsize){	//If memsize does not
					memNames[extra] = s[0];		//allow for the addition
					mem[extra] = value;			//of extra slots, these
					extra++;					//slots are ignored.
				}
			}

			Object[] mems = new Object[2];
			mems[0] = mem;
			mems[1] = memNames;
			return mems;
		} catch (IOException e) {
			P.rint("    >>IOException while reading " + line);
			throw new IOException();
		} catch (NumberFormatException e) {
			P.rint("    >>Could not parse the following line: "
					+ line);
			throw new IOException();
		} catch (ArrayIndexOutOfBoundsException e){
			P.rint("    >>Array Index Out Of Bounds Exception."
					+ " Oopsie on our part! Sorry!");
			throw new IOException();
		}
	}
}