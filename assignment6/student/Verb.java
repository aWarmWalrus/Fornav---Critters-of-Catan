package student;

import java.util.ArrayList;
import java.util.Random;

import ast.Mutation;
import ast.Program;
import ast.Rule;

public class Verb {
	
	//it would be nice to implement something such that
	//when the user is controlling the critter it is easy to waste energy
	//but when the critters are not supervised they are energy efficient in their actions
	
	private Critter ladybug;
	private World thisWorld;
	
	public Verb(Critter c) {
		ladybug = c;
		thisWorld = ladybug.getWorld();
	}
	
	public enum Type {
		WAIT, FORWARD, BACKWARD, LEFT, RIGHT, EAT, SERVE, ATTACK, TAG, 
		GROW, BUD, MATE, DIE;
	}
	
	/**
	 * Handles the Actions of a Critter
	 * @param type
	 * @return true if action executed, false otherwise
	 * common cases of returning false are 
	 * a) not enough energy
	 * b) not a valid move in context
	 */
	public boolean handleUnVerb(Verb.Type type){
		int nrgReq;
		int state;
		
		switch(type){
		case WAIT:
			System.out.println("~Critter is waiting. Energy gained: " + ladybug.getMem(3));
			ladybug.setEnergy((ladybug.getMem(4)+1*ladybug.getMem(3)));
			System.out.println("~Critter's current energy: " + ladybug.getMem(4));
			//if there is extra energy that is taken care of in setEnergy()
			return true;
			
		case FORWARD: //you use energy if you try to move but can't
			//so it is to your benefit to navigate the Hexes deftly and with precision
			System.out.println("this ladybug is null: " + ladybug == null);
			nrgReq = ladybug.getMem(3)*thisWorld.MOVE_COST;
			state = checkEnergy(nrgReq);
			if (state != 0){
				moveHelper(nrgReq, true);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			}
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
			
		case BACKWARD: //side effect: frog faces in opp direction
			nrgReq = ladybug.getMem(3)*thisWorld.MOVE_COST;
			state = checkEnergy(nrgReq);
			if (state != 0){
				moveHelper(nrgReq, false);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			}
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
		
		case RIGHT: //Java unfortunately doesn't have default params like Python
			nrgReq = ladybug.getMem(3);
			state = checkEnergy(nrgReq);
			if (state != 0){
				turnHelper(nrgReq, 1);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			} 
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
			
		case LEFT:
			nrgReq = ladybug.getMem(3);
			state = checkEnergy(nrgReq);
			if (state != 0){
				turnHelper(nrgReq, 5);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			} 
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
			
		case EAT: //you will waste energy if you attempt to eat food on hexes
			//that don't have food
			nrgReq = ladybug.getMem(3);
			state = checkEnergy(nrgReq);
			if (state != 0){
				eatHelper(nrgReq);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			}
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			

		case ATTACK: //you will waste energy if you attempt to attack a critter
			//that isn't there
			nrgReq = ladybug.getMem(3)*thisWorld.ATTACK_COST;
			state = checkEnergy(nrgReq);
			if (state != 0){
				attackHelper(nrgReq);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			} 
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			

		case GROW:
			nrgReq = ladybug.getMem(3)*ladybug.getComplexity()*thisWorld.GROW_COST;
			state = checkEnergy(nrgReq);
			if (state != 0){
				ladybug.setEnergy(ladybug.getMem(4) - nrgReq);
				ladybug.setSize(ladybug.getMem(3) + 1); // grows by one unit
				thisWorld.notifyOfCritter(ladybug);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			}
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
			
		case BUD:
			nrgReq = thisWorld.BUD_COST*ladybug.getComplexity();
			state = checkEnergy(nrgReq);
			if (state != 0){
				budHelper(nrgReq);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			} 			
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
			
		case MATE:
			ladybug.isMating = true;
			nrgReq = thisWorld.MATE_COST*ladybug.getComplexity();
			state = checkEnergy(nrgReq);
			if (state != 0){
				mateHelper(nrgReq);
				if (state < 0) handleUnVerb(Verb.Type.DIE);
				return true;
			} 
			assert ladybug.getWorld() == null; //state == 0, so critter has died.
			return false;			
			
		case DIE: //poor critter
			System.out.println("* A Critter has died!");
			int deadstuff = ladybug.getMem(3)*thisWorld.FOOD_PER_SIZE;
			Hex thatHex = ladybug.getHex();
			thatHex.setCheerios(thatHex.getCheerios() + deadstuff);
			ladybug.die();
			return true;
		
		default:
			System.out.println("WHY IS THIS HAPPENING?");
			throw new AssertionError();
		}
	}
	
	public boolean handleNuVerb(Verb.Type type, int num){
		int nrgReq;
		
		switch(type) {
		case SERVE:
			nrgReq = num + ladybug.getMem(3);
			if (checkEnergy(nrgReq) == 1) return serveHelper(nrgReq, num);
			else return false;
			
		case TAG:
			nrgReq = 1;
			if (checkEnergy(nrgReq) == 1) {
				ladybug.setEnergy(ladybug.getMem(4) - 1);
				if (ladybug.frontHex().isOccupied()) {
				ladybug.frontHex().getResident().setTag(num);
				}
			}
			return true;
			
			
		default:
			throw new AssertionError();
		}
	}
	
	/**
	 * Checks the energy of the critter agains the required energy.
	 * There are three possible returns.
	 * 0 if the critter dies immediately upon attempt. No action should
	 * be done by the critter
	 * 1 if the critter has the sufficient amount of energy.
	 * -1 if the critter goes directly to 0. Critter still gets to
	 * complete his action.
	 * @param energyReq
	 * @return 0, 1, or -1
	 */
	private int checkEnergy(int energyReq) {
		if (energyReq > ladybug.getMem(4)) {
			System.out.println("~Energy not sufficient. Critter ded.");
			Verb dead = new Verb(ladybug);
			dead.handleUnVerb(Verb.Type.DIE);
			return 0;	//immediate death
		}
		else if (energyReq == ladybug.getMem(4))
			return -1; //still executes action
		else return 1; //met energy requirements
	}
	
//	private enum Looks {
//		TAG, SIZE, POSTURE, DIRECTION;
//	}
	
//	private int extractAppearance(Verb.Looks component, int input) {
//		switch (component) {
//		
//		case TAG:
//			int tag = input/100000;
//			return tag;
//		case SIZE:
//			int size = input%10000/1000;
//			return size;
//		case POSTURE:
//			int posture = input%100/10;
//			return posture;
//		case DIRECTION:
//			int direction = input%10;
//			return direction;
//		default:
//			throw new AssertionError();
//		}
//	}
	
	private double logisticFunc(double x) {
		double a = 1/(1 + Math.exp(-x)); 
		return a;
	}
	
	
	/**
	 * for moving forwards and backwards.
	 */
	private boolean moveHelper(int nrgReq, boolean forward) {
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq);
		
		if(ladybug.moveForward(forward)) return true;
		
		return false;
	}
	
	/**
	 * For left and right turns.
	 */
	private boolean turnHelper(int nrgReq, int turn) {
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq);
		//Charlie's note: Critter has a setDir function. use it
		ladybug.setDir(ladybug.getDir() + turn);
		return true;
		
	}
	
	private boolean eatHelper(int nrgReq) {
		System.out.println("*Eating. Energy before: " +
				ladybug.getMem(4));
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq); //energy cost of eating
		Hex current = ladybug.getHex();
		if (current.getCheerios() > 0) {
			int capacity = thisWorld.ENERGY_PER_SIZE*ladybug.getMem(3); //max energy
			int remainder;
			if (ladybug.getMem(4) + current.getCheerios() > capacity){
				ladybug.setEnergy(capacity);
				remainder = (ladybug.getMem(4) + current.getCheerios()) - capacity;
			} else {
				ladybug.setEnergy(ladybug.getMem(4) + current.getCheerios());
				remainder = 0;
			}
			current.setCheerios(remainder);
		}
		System.out.println("        Energy after: " + ladybug.getMem(4));
		return true;
	}
	
	private boolean attackHelper(int nrgReq) {
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq); //energy for attacking
		
		if (ladybug.getSense(0) > 0) { //we found a Critter hooray
//			assert ladybug.hisHex().getResident() != null; 
			System.out.println("   ~Attacking a critter!");
			Critter thatKid = ladybug.frontHex().getResident();
			
			int s2 = thatKid.getMem(3);
			int s1 = ladybug.getMem(3);
			int d2 = thatKid.getMem(1);
			int o1 = ladybug.getMem(2);
			
			System.out.println("  Victim's energy bef: " + thatKid.getMem(4));
			
			double dmg = thisWorld.BASE_DAMAGE*s1*
					logisticFunc(thisWorld.DAMAGE_INC*(s1*o1 - s2*d2));
			Verb attacked = new Verb(thatKid);
			System.out.println("   Victim's energy aft: " + (thatKid.getMem(4) - (int)dmg));
			if ((int) dmg > thatKid.getMem(4)) {
				attacked.handleUnVerb(Verb.Type.DIE);
			} else {
				thatKid.setEnergy(thatKid.getMem(4) - (int)dmg);
				//subtracting energy
			}
		}
		return true;
	}
	
	/**
	 * Creates a new Critter identical to this one, with possible mutations.
	 * @return true if successful, false otherwise
	 */
	private boolean budHelper(int nrgReq) {
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq);
		
		if (ladybug.backHex().isOccupied()
				|| ladybug.backHex().isRock()) return false;
//		int behind = (ladybug.direction + 3)%6;
//		if (behind == -1 || behind > 0) return false; 
		//can't bud because we can't put the new Critter behind us
		
		Program p = ladybug.getProgram();
		Program p2 = p.dup(p); // makes a duplicate of the current program
		Random r = new Random();
		if (r.nextInt(3) == 0) { //25% chance of mutation
			int num = r.nextInt(6);
			switch (num) { //even chance of one of 6 mutations
			case 0:
				p2.mutate(Mutation.Type.COPY);
				break;
			case 1:
				p2.mutate(Mutation.Type.COPY_TREE);
				break;
			case 2:
				p2.mutate(Mutation.Type.CREATE_PARENT);
				break;
			case 3:
				p2.mutate(Mutation.Type.REMOVE);
				break;
			case 4:
				p2.mutate(Mutation.Type.REPLICATE);
				break;
			case 5:
				p2.mutate(Mutation.Type.SWAP);
				break;
			default:
				throw new AssertionError();
			}
		}
		//assuming we only have 8 positions
		int hisMem[] = new int[ladybug.memlength];
		hisMem[0] = ladybug.getMem(0);
		hisMem[1] = ladybug.getMem(1);
		hisMem[2] = ladybug.getMem(2);
		hisMem[3] = 1;
		hisMem[4] = thisWorld.INITIAL_ENERGY;
		for (int i = 5; i < ladybug.memlength; i++) hisMem[i] = 0;
		
		Critter c = new Critter(p2, hisMem, ladybug.getMemNames()); //new critter
//		ladybug.backHex().newResident(c);
		ladybug.getWorld().placeCritter(c, ladybug.backHex());
		return true;
	}
	
	private boolean mateHelper(int nrgReq) {
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq);
		
		if (!ladybug.frontHex().isOccupied()) return false; //nobody to mate with
		
		Critter mate = ladybug.frontHex().getResident(); //somebody to mate with
		if ((ladybug.backHex().isOccupied() || ladybug.backHex().isRock()) 
				&& (mate.backHex().isOccupied() || ladybug.backHex().isRock()))
			return false; //no space for kids
		
		if (mate.getMem(5) == ladybug.getMem(5) && mate.isMating) {
			//checking that it's the same time step and it's also mating
			
			//setting up mem values
			Random ran = new Random();
			int hisMem[] = new int[ladybug.memlength];
			for (int i = 0; i < 3; i++) {
				if (ran.nextInt(1) == 0) hisMem[i] = ladybug.getMem(i);
				else hisMem[i] = mate.getMem(i);
			}
			hisMem[3] = 1;
			hisMem[4] = thisWorld.INITIAL_ENERGY;
			for (int i = 5; i < ladybug.memlength; i++) hisMem[i] = 0;
			
			//setting up rules and the program
			int rulesize;
			ArrayList<Rule> list = new ArrayList<Rule>();
			Program p = new Program(list);
			if (ran.nextInt(1) == 0) { 
				rulesize = ladybug.getProgram().getRules().size();
			} else rulesize = mate.getProgram().getRules().size();
			for (int i = 0; i < rulesize; i++) {
				Rule r;
				if (ran.nextInt(1) == 0 
						&& ladybug.getProgram().getRules().size() >= i) { 
					r = ladybug.getProgram().getRules().get(i);
				} else r = mate.getProgram().getRules().get(i);
				Rule r2 = r.dup(p);
				list.add(r2);
			}
			
			//setting up the critter
			Critter baby = new Critter(p, hisMem, ladybug.getMemNames());
			if (ran.nextInt(1) == 0) {
				if (!ladybug.backHex().isOccupied() 
					&& !ladybug.backHex().isRock()) 
				ladybug.backHex().newResident(baby);
				else mate.backHex().newResident(baby);
			} else if (ran.nextInt(1) == 1) {
				if (!mate.backHex().isOccupied()
					&& !mate.backHex().isRock()) 
				mate.backHex().newResident(baby);
				else ladybug.backHex().newResident(baby);
			}
			
			return true;
		} else return false; //bad timing, no mating happened
	}
	
	private boolean serveHelper(int nrgReq, int num) {
		ladybug.setEnergy(ladybug.getMem(4) - nrgReq);
		
		ladybug.setEnergy(ladybug.getMem(4) - num); //subtracting energy
		ladybug.getHex().setCheerios(ladybug.getHex().getCheerios() + num); //put food\
		return true;
		
		
	}

}
