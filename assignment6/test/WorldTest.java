package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.*;

import student.Creator;
import student.Hex;
import student.World;

public class WorldTest {
	
	World world;
	
	@Test
	public void testNew(){
		
		world = new World(false);
		System.out.println("New world created.");

		for(Hex[] hex1 : world.getHexes()){
			for(Hex hex : hex1){
				assertNotNull("there is a null hex", hex);
			}
		}
		world = new World(false);
		world.printAllInfo();
		world.printHexInfo(2, 5);
		try {
			world.addCritter(Creator.newCritter("critter1.txt"));
		} catch (IOException e) {
			fail();
		}
		world.printInfo();
		
	}
}
