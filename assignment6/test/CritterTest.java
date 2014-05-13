package test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.*;

import student.*;

public class CritterTest {

	Critter main;
	Critter protagonist;
	Critter victim;
	World earth;
	World pandora;

	@Before
	public void initCritter() { //setting up Critters
		
		//initializing worlds
		earth = Creator.loadWorld("projectrunway.txt", 1, 11); //make a blank world
		pandora = Creator.loadWorld("fencingtournament.txt", 1, 11); //2nd world
		
		protagonist = earth.getCritters().get(0);
		victim = pandora.getCritters().get(0); // getting Critters from the ArrayList
		main = pandora.getCritters().get(1);
	}
		

	@Test
	public void test() {
		System.out.println("start of test, energy is " + protagonist.getMem(4));
		assertTrue(protagonist.getMem(4) == 2005);
		System.out.println("critter is currently at col " + protagonist.getHex().getCol()
				+ " and row " + protagonist.getHex().getRow());
		
//WORLD 1 (earth) AND CRITTER 1 (dragonfly)
		
	//TESTING WAIT AND ENERGY ADDITIONS
	for (int i = 0; i < 2; i++) {
		assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 1);
		int prev = protagonist.getMem(4);
//		System.out.println("round" + i);
//		System.out.println("before step, energy is " + test.getMem(4));
		earth.step(); //waiting
//		System.out.println("after step, energy is " + test.getMem(4));
//		System.out.println("previous energy is " + prev);
//		System.out.println("new energy is " + test.getMem(4));
		assertTrue(protagonist.getMem(4) == prev + 5*earth.SOLAR_FLUX);
	}

	
//TESTING ENERGY DEDUCTIONS AS WELL AS CRITTER MOVEMENTS
	earth.step(); //forward: 2015 - 5(3) = 2000
//	System.out.println("currently:" + test.getMem(4));
	assertTrue(protagonist.getMem(4) == 2015 - protagonist.getMem(3)*earth.MOVE_COST); 
	assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 2);
	
	earth.step(); //backward: 2000 - 5(3) = 1985
//	System.out.println("currently:" + test.getMem(4));
	assertTrue(protagonist.getMem(4) == 2000 - protagonist.getMem(3)*earth.MOVE_COST);
	assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 1);
	
	earth.step(); //forward: 1985 - 5(3) = 1970
	assertTrue(protagonist.getMem(4) == 1985 - protagonist.getMem(3)*earth.MOVE_COST);
	assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 2);
	
	earth.step(); //backward: 1970 - 5(3) = 1955
//	System.out.println("before it crashed: " +test.getMem(4));
	assertTrue(protagonist.getMem(4) == 1970 - protagonist.getMem(3)*earth.MOVE_COST);
	assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 1);
	
	
//TESTING TURNS
	earth.step(); //left: 1955 - 5 = 1950
	assertTrue(protagonist.getMem(4) == 1955 - protagonist.getMem(3));
	assertTrue(protagonist.getDir() == 5);
	assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 1);
	
	earth.step(); //left: 1950 - 5 = 1945
	assertTrue(protagonist.getMem(4) == 1950 - protagonist.getMem(3));
	assertTrue(protagonist.getDir() == 4);	
	
	earth.step(); //left: 1945 - 5 = 1940
	assertTrue(protagonist.getMem(4) == 1945 - protagonist.getMem(3));
	assertTrue(protagonist.getDir() == 3);
	
	earth.step(); //right: 1940 - 5 = 1935
	assertTrue(protagonist.getMem(4) == 1940 - protagonist.getMem(3));
	assertTrue(protagonist.getDir() == 4);
	
	earth.step(); //right: 1935 - 5 = 1930
	assertTrue(protagonist.getMem(4) == 1935 - protagonist.getMem(3));
	assertTrue(protagonist.getDir() == 5);
	
	earth.step(); //right: 1930 - 5 = 1925
	assertTrue(protagonist.getMem(4) == 1930 - protagonist.getMem(3));
	assertTrue(protagonist.getDir() == 0);
	
//TESTING EATING
	earth.step(); //forward: 1925 - 5(3) = 1910
	assertTrue(protagonist.getMem(4) == 1925 - protagonist.getMem(3)*earth.MOVE_COST);
	
	earth.step(); //forward: 1910 - 5(3) = 1895
	assertTrue(protagonist.getMem(4) == 1910 - protagonist.getMem(3)*earth.MOVE_COST);
	assertTrue(protagonist.getHex().getCol() == 1 && protagonist.getHex().getRow() == 3);
	
	protagonist.getHex().setCheerios(10); 
	//controlling the environment b/c random food generation
	int food = protagonist.getHex().getCheerios(); //before eating
	earth.step(); //eat: 1895 - 5 = 1890
	assertTrue(protagonist.getMem(4) == 1895 - protagonist.getMem(3) + food);
	System.out.println("current energy: " + protagonist.getMem(4));
	int beforeGrowing = protagonist.getMem(4);
	
//TESTING GROW
	int growcost = protagonist.getMem(3)*protagonist.getComplexity()*earth.GROW_COST;
	System.out.println("grow cost:" + growcost);
	earth.step(); //grow:
	assertTrue(protagonist.getMem(3) == 6); //it grew!
	assertTrue(protagonist.getMem(4) == beforeGrowing - growcost); //1900 - 1330 = 570
	
//"TRYING TO DO AN ACTION WITHOUT SUFFICENT ENERGY = DEATH" TEST
	earth.step(); //bud:
	assertTrue(protagonist.getHex() == null && protagonist.getWorld() == null);
	
//WORLD 2 (pandora) AND CRITTER 2 (mantis) AND CRITTER 3 (simple)
	System.out.println("\nNOW TESTING NEW WORLD AND TWO NEW CRITTERS\n");
	
	//TESTING THE 999 PASSES
	System.out.println("energy is " + main.getMem(4));
	assertTrue(main.getMem(4) == 100);
	pandora.step(); //ultimately will wait
	System.out.println(main.getMem(5));
	assertTrue(main.getMem(4) == 101);

	//TESTING TAG
	int orig = main.getMem(4);
	pandora.step(); //tag[10]
	System.out.println("energy here is: " + main.getMem(4));
	assertTrue(main.getMem(4) == orig - 1);
	//posture changed?
	
	System.out.println("other guy's tag is " + protagonist.getMem(6));
//	assertITrue(dragonfly.getMem(6) == 10); for some reason tag doesn't work
	
	
//	TESTING ATTACK
	int victimbefore = victim.getMem(4);
	System.out.println("energy before being attacked is " + victimbefore);
	main.setTag(33); //to force the execution of the next rule
	main.setEnergy(121); 
	pandora.step();
	int victimafter = victim.getMem(4);
	System.out.println("energy after: " + victimafter);
	assertTrue(victimbefore > victim.getMem(4)); //making sure the victim lost energy
	
	
	
	
	
	}
	
	@After
	public void cleanResources() {
		earth = null;
		pandora = null;
		main = null;
		protagonist = null;
		victim = null;
	}
		
	
}
