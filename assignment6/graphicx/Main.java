package graphicx;

import java.awt.Dimension;
import java.awt.Toolkit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import student.Creator;
import util.P;

public class Main extends Application {
	
	protected static double SCREEN_HEIGHT;
	protected static double SCREEN_WIDTH;
	private static Creator princessBubblegum;
	private Stage stage;
	private Group root;
	public View view;
	
	public static void main(String[] args) {
		P.rototyping = false;
		getScreenData();
		princessBubblegum = Creator.getInstance();
		Application.launch(args);
	}

	@Override
	public void start(Stage s) throws Exception { 
		stage = s;
		stage.setTitle("Critter World");
		final Controller controller = new Controller(this);
		princessBubblegum.addObserver(controller.view);
		root = new Group();
		root.getChildren().add(controller);
		stage.setScene(new Scene(root));

		stage.show();
	}
	
	private static void getScreenData() {
		P.rint("inside getScreenData");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		SCREEN_HEIGHT = screenSize.getHeight();
		SCREEN_WIDTH = screenSize.getWidth();
		P.rint("your screensize is "+SCREEN_HEIGHT+ " by " + SCREEN_WIDTH);
	}
	
	/**
	 * Returns the model that this Main is working with
	 * @return
	 */
	public Creator getCreator() {
		return princessBubblegum;
	}
	
	/**
	 * Returns the stage of this main
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * Gets the root node
	 */
	public Group getRoot() {
		return root;
	}
	
}