package graphicx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.util.Duration;
import student.Creator;
import student.Critter;
import student.Hex;
import student.World;
import util.P;
import student.Verb;

public class Controller extends AnchorPane {
	
	
	final Main main;
	final Creator princessBubblegum;
	private String critterFilePath = "";
	public View view;
	private Thread playGame;
	private double fps = 25;
	private Timer play;
	public boolean playing;
	private Critter currentCritter;
	ArrayList<Button> buttonList = new ArrayList<Button>();	
	
	private double height;
	private double width;


	@FXML AnchorPane stageAnchorPane;
	
	@FXML SplitPane bigHoncho;
	@FXML ScrollPane viewScrollPane;
	
	@FXML Label loadLabel;
	@FXML Label loadInfo;
	
	@FXML Label hexInfo;
	
	@FXML Label critterErrorMessage;
	@FXML TextField critterFileTextField;
	@FXML Button placeCritter;
	@FXML Button addCritters;
	@FXML TextField crittersBeingAdded;
	@FXML TextField critterPlaceCol;
	@FXML TextField critterPlaceRow;
	
	@FXML Label worldErrorMessage;
	@FXML Label worldFileLabel;
	@FXML ImageView playButton1;
	@FXML ImageView pauseButton1;
	@FXML ImageView playButton2;
	@FXML ImageView pauseButton2;
	@FXML ImageView skipButton1;
	@FXML ImageView skipButton2;
	@FXML Slider fpsSlider;
	@FXML TextField fpsTextField;
	@FXML StackPane playPause1;
	@FXML StackPane playPause2;
	
	@FXML AnchorPane controlPanel;
	@FXML AnchorPane theView;
	@FXML ImageView splitPaneControl;
	@FXML ScrollPane scrollpane;
	
	@FXML HBox coordinates;
	@FXML Label coordinatesText;
	@FXML Label stepCounterLabel;
	
	@FXML Button waitButton;
	@FXML Button forwardButton;
	@FXML Button backwardButton;
	@FXML Button leftButton;
	@FXML Button rightButton;
	@FXML Button eatButton;
	@FXML Button serveButton;
	@FXML Button attackButton;
	@FXML Button tagButton;
	@FXML Button growButton;
	@FXML Button budButton;
	@FXML Button mateButton;
	
	/**
	 * Loads the fxml file that comprises of the static view (aka the main
	 * user interface) as opposed to the dynamic view (aka the game components
	 * and visualizations.
	 * All listeners in the fxml correspond to an object or method here.
	 * @param m
	 */
	public Controller(Main m) {
		P.rototyping = false;
		main = m;
		princessBubblegum = main.getCreator();
		FXMLLoader fxmlLoader = 
				new FXMLLoader(getClass().getResource("activeUI_1.2.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		this.setWidth(main.getStage().getWidth());
		this.setHeight(main.getStage().getHeight());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        view = new View(this);
        
        setDimensions();

        theView.getChildren().add(view);
        addButtons();
	}
	
	private void setDimensions() {
		System.out.println("dimensions set!");
		height = main.getStage().getHeight();
		width = main.getStage().getWidth();
		double top = viewScrollPane.getVvalue() * (theView.getHeight() - height);
		double left = viewScrollPane.getHvalue() * (theView.getWidth() - width);
		view.setBorder(left, left + width, top, top + height);
	}
	
	/**
	 * Adds buttons to the array list of buttons to make iteration easier.
	 */
	private void addButtons() {
		buttonList.add(waitButton); 
		buttonList.add(forwardButton);
		buttonList.add(backwardButton);
		buttonList.add(leftButton);
		buttonList.add(rightButton);
		buttonList.add(eatButton);
		buttonList.add(serveButton);
		buttonList.add(attackButton);
		buttonList.add(tagButton);
		buttonList.add(growButton);
		buttonList.add(budButton);
		buttonList.add(mateButton);
	}
	
	/**
	 * When the new World button is clicked
	 */
	@FXML protected void handleNewWorld() {
		P.rint("* New World button clicked. (Controller)");
		P.rint(main.getStage().getHeight() + "," + main.getStage().getWidth());
		
		if(princessBubblegum.hasWorld()) {
			confirmSetWorld(false);
		} else {
			setNewWorld(false);
		}
	}
	
	/**
	 * When the load world button is clicked.
	 */
	@FXML protected void handleLoadWorld() {
		P.rint("* Load World button clicked. (Controller)");
		if(princessBubblegum.hasWorld()) {
			confirmSetWorld(true);
		} else {
			setNewWorld(true);
		}
	}
	
	/**
	 * When the select critter button is clicked.
	 */
	@FXML protected void handleSelectCritter() {
		FileChooser getCritterFile = new FileChooser();
		File critterFile = getCritterFile.showOpenDialog(main.getStage());
		if(critterFile == null) return;
		critterFilePath = critterFile.getAbsolutePath();
		Critter targetCritter = null;
		try {
			targetCritter = Creator.newCritter(critterFilePath);
		} catch (FileNotFoundException e) {
			P.rint("THIS SHOULD NEVER HAPPEN");
			e.printStackTrace();
		} catch (IOException e) {
			critterFilePath = "";
			critterErrorMessage.setText("Error: broken critter file");
			return;
		}
		critterFileTextField.setText(critterFile.getName());
		P.rint("* Critter selected! " + critterFile.getName());
		
		loadLabel.setText("Critter Profile");
		loadInfo.setText(targetCritter.getInfo().toString());
		P.rint(targetCritter.getInfo());
		placeCritter.setDisable(false);
		addCritters.setDisable(false);
		critterErrorMessage.setText("");
	}
	
	/**
	 * Add the number of critters into the world defined by the selected 
	 * critter and designated by the number in the crittersBeingAdded textField
	 */
	@FXML protected void handleAddCritters() {
		P.rint("* Add Critters button clicked. (Controller)");
		if(!princessBubblegum.hasWorld()) {
			//return some error statement here.
			critterErrorMessage.setText("Error: No World initialized yet");
			P.rint("!!Tried to add critters before initializing world!");
			return;
		}
		int pop = 0;
		try {
			pop = Integer.parseInt(crittersBeingAdded.getText());
		} catch (NumberFormatException e) {
			//return some error statement here.
			P.rint("!!Tried to put invalid input into critter Number field");
			critterErrorMessage.setText("Error: Invalid Add value");
			return;
		}
		if(pop > princessBubblegum.getSpace()) {
			//return some error statement here.
			critterErrorMessage.setText("Not enough space in world");
			P.rint("!!Not enough space in the world to add " + pop 
					+ " critters");
			return;
		}
		princessBubblegum.addCritters(critterFilePath, pop);
		critterErrorMessage.setText("");
	}

	/**
	 * When the place button is clicked.
	 */
	@FXML protected void handlePlaceCritter() {
		P.rint("* Place Crittesr button clicked. (Controller)");
		if(!princessBubblegum.hasWorld()) {
			//return some error statement here.
			critterErrorMessage.setText("Error: No World initialized yet");
			P.rint("!!Tried to add critters before initializing world!");
			return;
		}
		int row = -1;
		int col = -1;
		try {
			row = Integer.parseInt(critterPlaceRow.getText());
			col = Integer.parseInt(critterPlaceCol.getText());
		} catch (NumberFormatException e) {
			//return some error statement here.
			P.rint("!!Tried to put invalid input into critter Number field");
			critterErrorMessage.setText("Error: Invalid Coordinate values");
			return;
		}
		World world = princessBubblegum.getWorld();
		if(col < 0 || row < 0 || 
				col > world.MAX_COLUMN || row > world.MAX_ROW){
			critterErrorMessage.setText("Coordinates are out of bounds");
			return;
		}
		Hex targetHex = world.getHex(col + 1, row + 1);
		P.rint(targetHex.isOccupied());
		if (targetHex.isOccupied() || targetHex.isRock()) { 
			critterErrorMessage.setText("Target Hex is occupied");
			return;
		};
		princessBubblegum.placeCritter(critterFilePath, col, row);
		critterPlaceRow.setText("");
		critterPlaceCol.setText("");
		critterErrorMessage.setText("");
	}

	/**
	 * When the play button is clicked.
	 */
	@FXML protected void handlePlay() {
		P.rint("* playing!");
		play = new Timer();
		double waitTime = 1000 / fps;
		P.rint("we are waiting " + waitTime + " ms between steps");
		playing = true;
		play.schedule(
			    new TimerTask() {
			        @Override
			        public void run() {
			        	princessBubblegum.step();
			        }
			    }, 0, (long) waitTime);
		pauseButton1.setVisible(true);
		playButton1.setVisible(false);
		pauseButton2.setVisible(true);
		playButton2.setVisible(false);
	}

	/**
	 * when the user's mouse leaves the play button
	 */
	@FXML protected void darkenPlay() {
		ColorAdjust ca = new ColorAdjust();
		ca.setBrightness(0);
		playButton1.setEffect(ca);
		playButton2.setEffect(ca);
	}
	
	/**
	 * when the user's mouse enters the play button
	 */
	@FXML protected void lightenPlay() {
		ColorAdjust ca = new ColorAdjust();
		ca.setBrightness(0.5);
		playButton1.setEffect(ca);
		playButton2.setEffect(ca);
	}	
	
	/**
	 * when the user clicks the step button
	 */
	@FXML protected void handleSkip() {
		P.rint("* stepping forward one step!");
		princessBubblegum.step();
		stepCounterLabel.setText("Steps: " + 
				princessBubblegum.getWorld().getStep());
	}
	
	/**
	 * when the user's mouse leaves the skip button
	 */
	@FXML protected void darkenSkip() {
		ColorAdjust ca = new ColorAdjust();
		ca.setBrightness(0);
		skipButton1.setEffect(ca);
		skipButton2.setEffect(ca);
	}
	
	/**
	 * when the user's mouse enters the skip button
	 */
	@FXML protected void lightenSkip() {
		ColorAdjust ca = new ColorAdjust();
		ca.setBrightness(0.5);
		skipButton1.setEffect(ca);
		skipButton2.setEffect(ca);
	}
	
	/**
	 * when the pause button is clicked.
	 */
	@FXML protected void handlePause() {
		P.rint("* game pausing");
		assert playGame != null;
		play.cancel();
		pauseButton1.setVisible(false);
		playButton1.setVisible(true);
		pauseButton2.setVisible(false);
		playButton2.setVisible(true);
	}
	
	/**
	 * when the user's mouse leaves the pause button
	 */
	@FXML protected void darkenPause() {
		ColorAdjust ca = new ColorAdjust();
		ca.setBrightness(0);
		pauseButton1.setEffect(ca);
		pauseButton2.setEffect(ca);
	}
	
	/**
	 * when the user's mouse enters the pause button
	 */
	@FXML protected void lightenPause() {
		ColorAdjust ca = new ColorAdjust();
		ca.setBrightness(0.5);
		pauseButton1.setEffect(ca);
		pauseButton2.setEffect(ca);
	}
	
	/**
	 * when the user lets go of the FPS slider
	 */
	@FXML protected void handleSlideRelease() {
		fps = fpsSlider.getValue();
		String fpsString;
		if(fps > 9) {
			fpsString = Integer.toString((int) fps);
		} else {
			DecimalFormat df = new DecimalFormat("#.#");
			fpsString = df.format(fps);
		}
		fpsTextField.setText(fpsString);
	}
	
	/**
	 * when the user enters a value into the fps text field to set the fps
	 * slider
	 */
	@FXML protected void handleSlideEnter() {
		double fps = 0;
		try {
			fps = Double.parseDouble(fpsTextField.getText());
		} catch (NumberFormatException e) {
			handleSlideRelease();
			return;
		}
		//set the actual play speed to the double designated!
		fpsSlider.setValue(fps);
		fpsTextField.getText();
		P.rint(fps);
	}
	
	@FXML protected void handleMousePressed(MouseEvent e) {
//		setDimensions();
//		view.pruneWorld();
	}
	
	@FXML protected void handleMouseReleased(MouseEvent e) {
//		setDimensions();
//		view.pruneWorld();
	}
	
	@FXML protected void handleMouseDragging(MouseEvent e) {
		setDimensions();
		view.pruneWorld();
//		System.out.println(viewScrollPane.getHvalue() + ", " + viewScrollPane.getVvalue());
//		System.out.println("and " + theView.getWidth() + ", " + theView.getHeight());
	}
	
	/**
	 * when the user's 
	 * @param e
	 */
	@FXML protected void transparencyOn(MouseEvent e) {
		if(e.getX() / main.getStage().getWidth() 
				< bigHoncho.getDividerPositions()[0]-0.02){
			P.rint("bigHoncho's transparency on");
			bigHoncho.setMouseTransparent(true);
		}
	}
	
	@FXML protected void transparencyOff(MouseEvent e) {
		if(e.getX() / main.getStage().getWidth() 
				> bigHoncho.getDividerPositions()[0]-0.02){
			P.rint("bigHoncho's transparency off");
			bigHoncho.setMouseTransparent(false);
		}
	}
	
	@FXML protected void handleMouseEnterSplit() {
		splitPaneControl.setOpacity(1);
	}
	
	@FXML protected void handleMouseExitSplit() {
		splitPaneControl.setOpacity(.5);
	}
	
	@FXML protected void handleSplitPaneControl() {
		P.rint("splitpane button clicked");
		if(splitPaneControl.getRotate() == 180){
			P.rint("clicked!"); //prints this only when closing the panel
			view.handleCollapsePane();
			splitPaneControl.setRotate(0);
			controlPanel.setMinWidth(0);
			playPause2.setVisible(true);
			skipButton2.setVisible(true);
			final Timeline collapse = new Timeline();
			final KeyValue SPC = 
					new KeyValue(splitPaneControl.translateXProperty(), 280);
			final KeyValue CP = 
					new KeyValue(controlPanel.maxWidthProperty(), 0);
			final KeyFrame kf1 = 
					new KeyFrame(Duration.millis(500), SPC);
			final KeyFrame kf2 = 
					new KeyFrame(Duration.millis(500), CP);
			collapse.getKeyFrames().addAll(kf1, kf2);
			collapse.play();
		} else {
			view.handleExpandPane();
			splitPaneControl.setRotate(180);
			controlPanel.setMaxWidth(281);
			playPause2.setVisible(false);
			skipButton2.setVisible(false);
			final Timeline expand = new Timeline();
			final KeyValue SPC = 
					new KeyValue(splitPaneControl.translateXProperty(), 0);
			final KeyValue CP =
					new KeyValue(controlPanel.minWidthProperty(), 280);
			final KeyFrame kf1 = 
					new KeyFrame(Duration.millis(500), SPC);
			final KeyFrame kf2 = 
					new KeyFrame(Duration.millis(500), CP);
			expand.getKeyFrames().addAll(kf1, kf2);
			expand.play();

		}
	}
	
	@FXML protected void handleZoomIn() {
//		view.setHexRadius(((view.getHexRadius()+10) > 100) ? 
//			view.getHexRadius() : view.getHexRadius() + 10);
	}
	
	@FXML protected void handleZoomOut() {
//		view.setHexRadius(((view.getHexRadius()-10) < 0) ? 
//				view.getHexRadius() : view.getHexRadius() - 10);
	}
	
	/**
	 * updates the Hex Info panel
	 * @param currentHex
	 */
	public void updateHexInfo(Hex currentHex) {
		if (currentHex.isOccupied()) {
			currentCritter = currentHex.getResident();
			enableActions();
		}
		else {
			currentCritter = null;
			disableActions();
		}
		P.rint("inside updateHexInfo (Controller)");
		
		if (currentHex.isBorder()) {
			hexInfo.setText("Hex Stats: \n THIS IS PART OF THE BORDER");
		}
		else if (currentHex.isRock()) {
			hexInfo.setText("Hex Stats: \n THIS IS A ROCK");
		}
		else {
			hexInfo.setText("Hex Stats: \n FOOD: " + currentHex.getCheerios() + 
				"\n CRITTER: " + ((currentHex.isOccupied()) ? "Yep" : "Nah") + 
				((currentHex.isOccupied()) ? "\n TYPE: " + 
				currentHex.getResident().getInfo().toString() : "" ));
		}
	}
		
	
	public void disableActions() {
		for (Button b: buttonList) {
			b.setDisable(true);
		}
	}
	
	public void enableActions() {
		for (Button b: buttonList) {
			b.setDisable(false);
		}
	}
	
	/**
	 * The following methods handle the buttons that allow the user to control
	 * critters
	 */
	@FXML protected void handleWait() {
		currentCritter.setNextVerb(Verb.Type.WAIT);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleForward() {
		currentCritter.setNextVerb(Verb.Type.FORWARD);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());	}
	
	@FXML protected void handleBackward() {
		currentCritter.setNextVerb(Verb.Type.BACKWARD);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleLeft() {
		currentCritter.setNextVerb(Verb.Type.LEFT);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleRight() {
		currentCritter.setNextVerb(Verb.Type.RIGHT);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleEat() {
		currentCritter.setNextVerb(Verb.Type.EAT);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleServe() {
		currentCritter.setNextVerb(Verb.Type.SERVE);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleAttack() {
		currentCritter.setNextVerb(Verb.Type.ATTACK);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleTag() {
		currentCritter.setNextVerb(Verb.Type.TAG);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleGrow() {
		currentCritter.setNextVerb(Verb.Type.GROW);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleBud() {
		currentCritter.setNextVerb(Verb.Type.BUD);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	@FXML protected void handleMate() {
		currentCritter.setNextVerb(Verb.Type.MATE);
		handleSkip();
		if(currentCritter.getHex() != null)
			view.update(currentCritter.getHex());
	}
	
	/**
	 * Sets a new world in the Creator that represents our model.
	 * @param fromFile
	 */
	private void setNewWorld(boolean fromFile){
		P.rint("* Setting a new world up. (Controller)");
		if (fromFile) {
			FileChooser getWorldFile = new FileChooser();
			File worldFile = getWorldFile.showOpenDialog(main.getStage());
			String fileName = "";
			if (worldFile == null) return;
			fileName = worldFile.getAbsolutePath();
			try {
				view.getRoot().getChildren().removeAll();
				World newWorld =Creator.loadWorld(fileName);
				princessBubblegum.setWorld(newWorld);
				princessBubblegum.getWorld().addObserver(view);
			} catch (IOException e) {
				P.rint("    >>ERROR: World file is faulty (Controller).");
				worldErrorMessage.setText("Provided world file is invalid");
			}
			worldFileLabel.setText(worldFile.getName());
		} else {
			World newWorld = Creator.newWorld();
			princessBubblegum.setWorld(newWorld);
			princessBubblegum.getWorld().addObserver(view);
		}
		playPause1.setDisable(false);
		playPause2.setDisable(false);
		
		playButton1.setDisable(false);
		playButton1.setOpacity(1);
		pauseButton1.setDisable(false);
		
		playButton2.setDisable(false);
		playButton2.setOpacity(1);
		pauseButton2.setDisable(false);
		
		skipButton1.setDisable(false);
		skipButton1.setOpacity(1);
		skipButton2.setDisable(false);
		skipButton2.setOpacity(1);
		worldErrorMessage.setText("World is up and running!");

	}
	
	/**
	 * Creates a popup asking the user if he really wants to build a world
	 * over the world that is already in store. This method is in charge
	 * of calling the newWorld method
	 * @param fromFile
	 * @return 
	 */
	private void confirmSetWorld(final boolean fromFile){
		
		/*
		 * CREATE THE BUTTONS AND RECTANGLES AND STUFF. 'VIEW' STUFF HERE.
		 */
		final Popup confirm = new Popup();
		
		Group confirmElem = new Group();
		confirm.centerOnScreen();
		confirm.setOpacity(1.0);
		confirm.setHeight(Main.SCREEN_HEIGHT);
		confirm.setWidth(Main.SCREEN_WIDTH);
		confirm.setHideOnEscape(true);
		
		final Rectangle shading = new Rectangle(confirm.getWidth(), 
				confirm.getHeight(),
				Color.BLACK);
		FadeTransition ft = new FadeTransition(Duration.millis(500), shading);
		ft.setFromValue(0);
		ft.setToValue(.5);
		
		Button yes = new Button("Yes");
		Button no = new Button("No");
		HBox yesno = new HBox();
		yesno.getChildren().addAll(yes, no);
		yesno.setSpacing(10);
		yesno.setAlignment(Pos.CENTER);
		
		Label query = new Label("There is already a world stored,"
				+ "\noverwrite saved world?");
		query.setTextAlignment(TextAlignment.CENTER);
		VBox text = new VBox();
		text.getChildren().addAll(query, yesno);
		text.setAlignment(Pos.CENTER);
		text.setMinSize(confirm.getWidth(), confirm.getHeight());
		
		Rectangle border = new Rectangle(250, 150, 
				new Color(1.0, 0.6, 0.0, 1.0));
		border.setStroke(Color.SILVER);
		Rectangle space = new Rectangle(250 - 6,
				150 - 6, Color.WHITE);
		StackPane win = new StackPane();
		win.getChildren().addAll(border, space, text);
		win.setAlignment(Pos.CENTER);
		confirmElem.getChildren().addAll(win);
		confirm.getContent().addAll(shading, confirmElem);
//		shading.setEffect(new BoxBlur(main.getStage().getWidth(),
//				main.getStage().getHeight(),
//				3));
		ft.play();
		confirm.show(main.getStage());
		
		/*
		 * CONTROLLER STUFF HERE.
		 */
		yes.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent e){
				confirm.hide();
				setNewWorld(fromFile);
			}
		});
		
		no.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent e){
				confirm.hide();
			}
		});
	}
}
