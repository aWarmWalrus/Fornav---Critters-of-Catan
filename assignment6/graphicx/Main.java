package graphicx;

import javafx.application.Application;
import javafx.scene.*;
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