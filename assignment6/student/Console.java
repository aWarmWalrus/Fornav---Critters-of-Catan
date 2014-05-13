package student;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

/** The console user interface for Assignment 5. */
public class Console {

	private Scanner scan;
	private boolean done;
	private Creator princessBubblegum;
//	private World world;

	public static void main(String[] args) {
		Console console = new Console();
		
		System.out.println("TT===========================================================================TT"
				       + "\n||                               d( ^__^ )b                                  ||"
				       + "\n||                          WELCOME TO CRITTER WORLD                         ||"
				       + "\n||        Q('-'o)      <<a simulation of LIFE and DEATH>>      (>^o^)>       ||"
				       + "\n||                   Charles Qian cq38 / Kelly Yu kly24                      ||"
				       + "\n||                                <( x__x )>                                 ||"
				       + "\n++===========================================================================++");
		
		while (!console.done) {
			System.out
					.println("\nEnter a command or \"help\" for a list of commands.");
			try {
				console.handleCommand();
			} catch (InputMismatchException e){
				System.out.println("Please enter a valid argument. STRINGS "
						+ "CAN'T BE PARSED INTO INTEGERS YOU JERK. STOP "
						+ "TRYING TO RUIN OUR MARRIAGE.");
			}
		}
	}

	/**
	 * Processes a single console command provided by the user.
	 */
	void handleCommand() throws InputMismatchException{
		String command = scan.next();
		if (command.equals("new")) {
			newWorld();
		} else if (command.equals("load")) {
			String filename = scan.next();
			loadWorld(filename);
		} else if (command.equals("critters")) {
			String filename = scan.next();
			int n = scan.nextInt();
			loadCritters(filename, n);
		} else if (command.equals("step")) {
			int n = scan.nextInt();
			advanceTime(n);
		} else if (command.equals("info")) {
			worldInfo();
		} else if (command.equals("hex")) {
			int c = scan.nextInt();
			int r = scan.nextInt();
			hexInfo(c, r);
		} else if (command.equals("help")) {
			printHelp();
		} else if (command.equals("exit")) {
			done = true;
		} else if (command.equals("#print-all")) {
			printAll();
		} else if (command.equals("#play")) {
			play();
		} else if (command.equals("#customload")) {
			String filename = scan.next();
			int col = scan.nextInt();
			int row = scan.nextInt();
			customload(filename, col, row);
		} else
			System.out.println(command + " is not a valid command.");
	}

	/**
	 * Constructs a new Console capable of reading the standard input.
	 */
	public Console() {
		scan = new Scanner(System.in);
		done = false;
		princessBubblegum = Creator.getInstance();
	}

	/**
	 * Starts new random world simulation
	 */
	private void newWorld() {
		
		if (princessBubblegum.hasWorld()) {
			System.out.println("There is currently a world stored. "
					+ "Override the existing world? y/n");
			String choice = scan.next();
			if (!choice.toLowerCase().equals("y") &&
					!choice.toLowerCase().equals("yes"))
				return;
		}
		
		princessBubblegum.setWorld(Creator.newWorld());
		System.out.println("* New world created!");
	}
	
	/**
	 * Starts new simulation with world specified in filename
	 * 
	 * @param filename
	 */
	private void loadWorld(String filename) {
		
		if (princessBubblegum.hasWorld()) {
			System.out.println("There is currently a world stored. "
					+ "Override the existing world? y/n");
			String choice = scan.next();
			if (!choice.toLowerCase().equals("y") &&
					!choice.toLowerCase().equals("yes")){
				return;
			}
		}
		
		try {
			princessBubblegum.setWorld(Creator.loadWorld(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("* New world created!");
	}

	/**
	 * Starts new simulation with world specified in filename + row, column
	 * inputs
	 * @param filename
	 * @param row
	 * @param column
	 */
	private void customload(String filename, int col, int row) {

		if (princessBubblegum.hasWorld()) {
			System.out.println("There is currently a world stored. "
					+ "Override the existing world? y/n");
			String choice = scan.next();
			if (!choice.toLowerCase().equals("y") &&
					!choice.toLowerCase().equals("yes")){
				return;
			}
		}
		
		princessBubblegum.setWorld(Creator.loadWorld(filename, col, row));
		System.out.println("* New world created!");
		
	}
	
	
	/**
	 * Loads critter definition from filename and randomly places n critters
	 * with that definition into the world
	 * 
	 * @param filename
	 * @param n
	 */
	private void loadCritters(String filename, int n) {
		if (!princessBubblegum.hasWorld())
			System.out.println("ERROR: no world has been initialized yet.");
		else{
			princessBubblegum.addCritters(filename, n);
		}
		System.out.println("* All valid Critters loaded!");
	}

	/**
	 * advances the world by n timesteps
	 * 
	 * @param n
	 */
	private void advanceTime(int n) {
		if(!princessBubblegum.hasWorld())
			System.out.println("Error: no world has been initialized yet.");
		else
			for(int i = 0; i < n; i++)
				princessBubblegum.step();
	}

	/**
	 * prints current timestep, number of critters, and world map of the
	 * simulation
	 */
	private void worldInfo() {
		if(!princessBubblegum.hasWorld())
			System.out.println("Error: no world has been initialized yet.");
		else
			princessBubblegum.printInfo();
	}

	/**
	 * prints description of the contents of hex (c,r)
	 * 
	 * @param c
	 *            column of hex
	 * @param r
	 *            row of hex
	 */
	private void hexInfo(int c, int r) {
		if (!princessBubblegum.hasWorld())
			System.out.println("Error: no world has been initialized yet.");
		else
			princessBubblegum.printHexInfo(c, r);
	}
	
	private void printAll() {
		if (!princessBubblegum.hasWorld())
			System.out.println("Error: no world has been initialized yet.");
		else
			princessBubblegum.printAllInfo();
	}
	
	private void play() {
		if (!princessBubblegum.hasWorld())
			System.out.println("Error: no world has been initialized yet.");
		else{
			boolean cont = true;
			while(cont){
				princessBubblegum.step();
				princessBubblegum.printInfo();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					cont = false;
				} 
//				if (scan.next() == "x") break;
			}
		}
	}

	/**
	 * Prints a list of possible commands to the standard output.
	 */
	private void printHelp() {
		System.out.println("new: start a new simulation with a random world");
		System.out.println("load <world_file>: start a new simulation with"
				+ "the world loaded from world_file");
		System.out.println("critters <critter_file> <n>: add n critters"
				+ "defined by critter_file randomly into the world");
		System.out.println("step <n>: advance the world by n timesteps");
		System.out.println("info: print current timestep, number of critters"
				+ "living, and map of world");
		System.out.println("hex <c> <r>: print contents of hex"
				+ "at column c, row r");
		System.out.println("exit: exit the program");
	}
}