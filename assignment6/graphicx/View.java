package graphicx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import student.Creator;
import student.Critter;
import student.Hex;
import student.World;
import util.P;

public class View extends Parent implements Observer {

	private Creator pb;
	private World thatWorld;
	private Group big;
	private double RADIUS = 50.0;
	private double vertDisp = RADIUS * Math.cos(Math.PI / 6); // initial y
																// coordinate
	private Controller root;
	private double horzDisp = 1.5 * RADIUS;
	private double worldHeight;
	private static final Image water = new Image(
			"http://i.imgur.com/BrLSNId.png");
	private static final Image grass = new Image(
			"http://i.imgur.com/cUUfcpP.png");
	private static final Image ground = new Image(
			"http://i.imgur.com/Pepv5oP.png");
	private static final Image rock = new Image(
			"http://i.imgur.com/ONq4cnp.png");
	private static final Image ladybug = new Image(
			"http://www.cyberbotics.com/files/media/ladybug.png");
	private HashMap<Critter, Circle> bugList;
	private HashMap<Hex, HexImage> hexList;
	public Polygon selectedHex = null;
	private int selectedCol;
	private int selectedRow;
	private ArrayList<Critter> moveQueue;
	private ArrayList<Hex> changeQueue;
	private ArrayList<Circle> deathQueue;
	private Timer mainTimer;
	private double left;
	private double right;
	private double bottom;
	private double top;
	private Rectangle border;
	private Rectangle whiteSpace;
	public int maxHeight;
	public int maxWidth;

	/**
	 * The view has a timer that updates itself every 33 ms. It keeps track of
	 * the things it has to draw by pulling things in and out of its group's
	 * children list.
	 * 
	 * @param head
	 */
	public View(Controller head) {
		root = head;
		pb = root.princessBubblegum;
		big = new Group();
		RADIUS = 50; // !!
		getChildren().add(big);
		Platform.setImplicitExit(false);
		moveQueue = new ArrayList<Critter>();
		changeQueue = new ArrayList<Hex>();
		deathQueue = new ArrayList<Circle>();
		mainTimer = new Timer();
//		border = new Rectangle();
		whiteSpace = new Rectangle();
//		big.getChildren().add(whiteSpace);
		mainTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateView();
			}
		}, 0, 33);
	}

	/**
	 * Called by the Model whenever a new world is set.
	 */
	private void initializeNewWorld(World world) {
		P.rint("should draw critters now");
		assert world == pb.getWorld();
		world.addObserver(this);
		thatWorld = world;
		drawWorld();
	}

	/**
	 * Overridden from the Observer class. Will add things to the update queue
	 * to be updated later
	 */
	@Override
	public void update(Observable world, final Object d) {
		P.rint("updating! (View)");

		if (d instanceof World) {
			initializeNewWorld((World) d);
		} else if (d instanceof Critter) {
			if (((Critter) d).getHex() == null) {
				P.rint("erasing critter (View)");
				Circle toGo = bugList.get(d);
				deathQueue.add(toGo);
			} else {
				P.rint("adding Critter to the view (View)");
				assert d instanceof Critter;
				moveQueue.add((Critter) d);
			}
		} else if (d instanceof Hex) {
			changeQueue.add((Hex) d);
		} else if (d instanceof ArrayList<?>) {
			P.rint("uh...what");
		} else {
			P.rint("what what what");
		}
	}

	/**
	 * This method will be called every 33 ms to update the image of the world.
	 * This is a prototype. It might be possible for critters to not be removed.
	 */
	private void updateView() {
		// might want to remove everything before the step;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Critter c : moveQueue) {
					if (deathQueue.contains(bugList.get(c))) {
						continue;
					}
					final int cX = (int) xx(c.getCol());
					final int cY = (int) yy(c.getCol(), c.getRow());
					Circle crit;
					if (!bugList.containsKey(c)) {
						ImagePattern x = new ImagePattern(ladybug, 0, 0, 1, 1,
								true);
						crit = new Circle(RADIUS, x);
						bugList.put(c, crit);
						crit.setMouseTransparent(true);
					} else {
						crit = bugList.get(c);
					}
					
					ColorAdjust ca = new ColorAdjust();
					ca.setHue((c.getMem(7) / 100.0));
					crit.setEffect(ca);
					crit.setCenterX(cX);
					crit.setCenterY(cY);
					crit.setRotate(c.getDir() * 60 - 33);
					crit.setRadius(RADIUS - 20 + c.getMem(3) * 1.5);
				}
				for (Hex h : changeQueue) {
					HexImage hi = hexList.get(h);
					Polygon hex = hi.hex;
					if (h.isBorder()) {
						hex.setFill(new ImagePattern(water));
						hex.setStrokeWidth(0);
					} else if (h.isRock())
						hex.setFill(new ImagePattern(rock));
					else if (h.getCheerios() > 0)
						hex.setFill(new ImagePattern(grass));
					else
						hex.setFill(new ImagePattern(ground));
				}
				for (Circle c : deathQueue) {
					P.rint("Erase Critter status: "
							+ (big.getChildren().remove(c) ? "good" : "bad"));
				}
				deathQueue.clear();
				moveQueue.clear();
				changeQueue.clear();
				Stage i = root.main.getStage();
				root.stageAnchorPane.setMinSize(i.getWidth(), i.getHeight());
				root.stageAnchorPane.setMaxSize(i.getWidth(), i.getHeight());
				root.stepCounterLabel.setText("Steps: "
						+ (pb.hasWorld() ? pb.getWorld().getStep() : 0));
			}
		});
	}
	
	public void pruneWorld() {
		for(HexImage c : hexList.values()) {
			if(c.cX + RADIUS > right + RADIUS * 2 || 
					c.cX - RADIUS < left - RADIUS * 2||
					c.cY - RADIUS < top - RADIUS * 2||
					c.cY + RADIUS > bottom + RADIUS * 2){
				big.getChildren().remove(c.hex);
			} else {
				if (!big.getChildren().contains(c.hex)) {
					big.getChildren().add(c.hex);
				}
			}
		}
	}
	
	public void setBorder(double L, double R, double T, double B){
		left = L;
		right = R;
		top = T;
		bottom = B;		
//		border.setX(L);
//		border.setY(T);
//		border.setWidth(R-L);
//		border.setHeight(B-T);
////		border.setStroke(Color.RED);
////		border.setStrokeWidth(3);
//		border.setFill(null);
	}

	/**
	 * Updates the status of the selected Hex
	 * @param currentHex
	 */
	public void update(Hex currentHex) {
		P.rint("Update() is being called");
		selectedHex.setStroke(Color.WHITE);
		selectedHex.setStrokeWidth(5);
		P.rint("about to updateHexInfo");
		root.updateHexInfo(currentHex);
		root.critterPlaceCol.setText(String.valueOf(selectedCol));
		root.critterPlaceRow.setText(String.valueOf(selectedRow));
	}
	
	/**
	 * Draws the world. Calls a more specific function, but it's deprecated
	 */
	private void drawWorld() {
		double right = root.theView.getWidth() + RADIUS;
		double top = root.theView.getHeight() + RADIUS;
		drawWorld(right, top, 0, 0);
	}

	/**
	 * Draws the initial state of the world, the border, any food hexes and 
	 * any critters that are in the world
	 * @param right
	 * @param top
	 * @param left
	 * @param bot
	 */
	protected void drawWorld(double right, double top, double left, double bot) {
		big.getChildren().clear();
		hexList = new HashMap<Hex, HexImage>();
		bugList = new HashMap<Critter, Circle>();
		P.rint("drawing world");
		int numrows = thatWorld.MAX_ROW;
		int numcols = thatWorld.MAX_COLUMN;
		int usableRows = numrows - (numcols/2);
		
		handleExpandPane();
		
		setWorldHeight((usableRows + 2)*2*RADIUS*Math.cos(Math.PI/6));
		int dim = (thatWorld.MAX_COLUMN + 2)
				+ ((thatWorld.MAX_ROW + 2) * 2 + 1);
		for (int col = 0; col < thatWorld.MAX_COLUMN + 3; col++) {
			for (int row = 0; row < thatWorld.MAX_ROW + 3; row++) {
				int newRow = 2 * ((thatWorld.MAX_ROW + 2) - row) + col;
				if (newRow > thatWorld.MAX_COLUMN
						&& newRow < dim - thatWorld.MAX_COLUMN - 1) {
					drawHexagon(thatWorld.getHex(col, row), right, top, left,
							bot);
				}
			}
		}
		for (Critter c : thatWorld.getCritters()) {
			P.rint("drawing critters");
			moveQueue.add(c);
		}
		whiteSpace.setX(0);
		whiteSpace.setY(0);
		whiteSpace.setHeight(yy(2, 0));
		whiteSpace.setWidth(xx(thatWorld.MAX_COLUMN + 2));
		whiteSpace.setStroke(null);
		whiteSpace.setStrokeWidth(5);
		whiteSpace.setFill(Color.WHITESMOKE);
		if(!big.getChildren().contains(whiteSpace)){
			big.getChildren().add(whiteSpace);
			System.out.println("added whitespace");
		}
	}

	/**
	 * Returns the radius of the hex
	 * @return
	 */
	public double getHexRadius() {
		return RADIUS;
	}

	/**
	 * Was initially meant for zooming functionality. got too hard, so this
	 * doesn't really do anything.
	 * @param radius
	 */
	public void setHexRadius(double radius) {
		// use this for a zooming function?
		RADIUS = radius;
	}

	/**
	 * This function sets the World Height.
	 * @param wh
	 */
	public void setWorldHeight(double wh) {
		worldHeight = wh;
	}

	/**
	 * This returns the vertical Displacement, which depends on the Radius.
	 * @return
	 */
	private double vertDisp() {
		vertDisp = RADIUS * Math.cos(Math.PI / 6);
		return vertDisp;
	}

	/**
	 * This draws a Hexagon based upon the current bounds of the AnchorPane.
	 * We intended to use this function to only draw the Hexes the view could
	 * see, but we ended up not.
	 * @param hexy
	 * @param right
	 * @param top
	 * @param left
	 * @param bot
	 */
	private void drawHexagon(final Hex hexy, double right, double top,
			double left, double bot) {
		final int col = hexy.getCol();
		final int row = hexy.getRow();
		double hexCenterX = xx(col);
		double hexCenterY = yy(col, row);
		
		final Polygon hex = new Polygon(hexCenterX + .5 * RADIUS,
				(hexCenterY + vertDisp()), hexCenterX + RADIUS, hexCenterY,
				hexCenterX + .5 * RADIUS, (hexCenterY - vertDisp()), hexCenterX
						- .5 * RADIUS, (hexCenterY - vertDisp()), hexCenterX
						- RADIUS, hexCenterY, hexCenterX - .5 * RADIUS,
				(hexCenterY + vertDisp()));
		hex.setStroke(Color.GREEN);
		hex.setStrokeWidth(2);
		hex.setStrokeType(StrokeType.INSIDE);
		
		HexImage ih = new HexImage(hex, hexCenterX, hexCenterY);
		
		if (!hexList.containsValue(hex))
			hexList.put(hexy, ih);

		if (hexy.isBorder()) {
			hex.setFill(new ImagePattern(water));
			hex.setStrokeWidth(0);
			return;
		}

		hex.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				hex.setStroke(Color.WHITE);
				P.rototyping = false;
				P.rint("whatwhat");
				P.rototyping = true;
				if (!root.coordinates.isVisible()) {
					root.coordinates.setVisible(true);
				}
				root.coordinatesText.setText((hexy.getCol() - 1) + ", "
						+ (hexy.getRow() - 1));
			}
		});

		hex.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (hex.equals(selectedHex)) {
					hex.setStroke(Color.WHITE);
				} else
					hex.setStroke(Color.GREEN);
			}
		});
		hex.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (selectedHex != null) {
					selectedHex.setStroke(Color.GREEN);
					selectedHex.setStrokeWidth(2);
				}
				selectedHex = hex;
				selectedCol = hexy.getCol() - 1;
				selectedRow = hexy.getRow() - 1;
				update(hexy);
			}
		});
		changeQueue.add(hexy);
	}
	
	/**
	 * Converts the column position to an x position.
	 * @param col
	 * @return
	 */
	private double xx(int col) {
		return RADIUS + col * horzDisp;
	}

	/**
	 * Converts the row position to a y position;
	 * @param col
	 * @param row
	 * @return
	 */
	private double yy(int col, int row) {
		int newRow = row - (col + 1) / 2;
		return (col % 2 == 1) ? worldHeight - (2 * vertDisp() * newRow
						+ vertDisp()) : worldHeight - (2 * vertDisp() * newRow);
	}

	protected void handleCollapsePane() {
//		whiteSpace.setWidth(thatWorld.MAX_COLUMN* 2 * RADIUS);
	}
	
	protected void handleExpandPane() {
		int numcols = thatWorld.MAX_COLUMN;
		double preferredWidth = (numcols + 2)*2*RADIUS;
//		whiteSpace.setWidth(preferredWidth);
	}
	
	
	/**
	 * Returns the root.
	 * @return
	 */
	public Group getRoot() {
		return big;
	}
	
	class HexImage {
		
		final public Polygon hex;
		final public double cX;
		final public double cY;
		
		HexImage(Polygon h, double cX, double cY) {
			hex = h;
			this.cX = cX;
			this.cY = cY;
		}
	}
}
