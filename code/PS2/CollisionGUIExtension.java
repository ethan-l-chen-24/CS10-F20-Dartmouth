/**
 * CollisionGUIExtension
 * An extension on the CollisionGUI class to allow collisions of blobs of all sizes and adds
 * Teleporter and WanderingPulsar blobs
 *
 * @name -> Ethan Chen
 * @date -> October 7, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.awt.*;

import javax.swing.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Using a quadtree for collision detection
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, updated for blobs
 * @author CBK, Fall 2016, using generic PointQuadtree
 */
public class CollisionGUIExtension extends DrawingGUI {

	/**
	 * VARIABLES
	 * --------------------
	 */

	private static final int width=800, height=600;		// size of the universe

	private List<Blob> blobs;							// all the blobs
	private List<Blob> colliders;						// the blobs who collided at this step
	private char blobType = 'b';						// what type of blob to create
	private char collisionHandler = 'c';				// when there's a collision, 'c'olor them, or 'd'estroy them
	private int delay = 100;							// timer control

	/**
	 * CONSTRUCTOR
	 * --------------------
	 */

	public CollisionGUIExtension() {
		super("super-colliderExtension", width, height);

		blobs = new ArrayList<Blob>(); // initializes as empty ArrayLists
		colliders = new ArrayList<Blob>();

		// Timer drives the animation.
		startTimer();
	}

	/**
	 * METHODS
	 * --------------------
	 */

	/** Adds an blob of the current blobType at the location */
	private void add(int x, int y) {
		if (blobType=='b') { // bouncer
			Bouncer newBouncer = new Bouncer(x, y, width, height); // edited from original to add random radius
			newBouncer.setR(Math.random() * 20);
			blobs.add(newBouncer);
		}
		else if (blobType=='w') { // wanderer
			Wanderer newWanderer = new Wanderer(x, y); // edited from original to add random radius
			newWanderer.setR(Math.random() * 20);
			blobs.add(newWanderer);
		}
		else if (blobType=='p') { // wandering pulsar
			WanderingPulsar newWP = new WanderingPulsar(x, y); // edited from original to add random radius
			newWP.setR(Math.random() * 20);
			blobs.add(newWP);
		}
		else if (blobType=='t') { // teleporter
			Teleporter newTeleporter = new Teleporter(x, y, width, height); // NOTE: Teleporter teleport() method must be changed to step()
			newTeleporter.setR(Math.random() * 20);
			blobs.add(newTeleporter);
		}
		else if(blobType=='.') { // blob
			Blob newBlob = new Blob(x, y);
			blobs.add(newBlob);
		}
		else {
			System.err.println("Unknown blob type "+blobType);
		}
	}

	/** DrawingGUI method, here creating a new blob */
	public void handleMousePress(int x, int y) {
		add(x,y); // adds a blob at the location of the mouse press
		repaint();
	}

	/** DrawingGUI method */
	public void handleKeyPress(char k) {
		if (k == 'f') { // faster
			if (delay>1) delay /= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 's') { // slower
			delay *= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		}
		else if (k == 'r') { // add some new blobs at random positions
			for (int i=0; i<10; i++) {
				add((int)(width*Math.random()), (int)(height*Math.random()));
				repaint();
			}			
		}
		else if (k == 'c' || k == 'd') { // control how collisions are handled
			collisionHandler = k;
			System.out.println("collision:"+k);
		}
		else { // set the type for new blobs
			blobType = k;			
		}
	}

	/** DrawingGUI method, here drawing all the blobs and then re-drawing the colliders in red */
	public void draw(Graphics g) {
		for(Blob blob : blobs) { // draw all of the blobs in existence
			blob.draw(g);
		}
		for(Blob blob : colliders) { // change the color to red and draw all the blobs that have previously collided
			g.setColor(Color.red);
			blob.draw(g);
		}
	}

	/** Sets colliders to include all blobs in contact with another blob */
	private void findColliders() {

		// Create the tree, set first blob as tree head
		PointQuadtreeExtension<Blob> blobTree = new PointQuadtreeExtension<Blob>(blobs.get(0), 0, 0, width, height);
		for(Blob blob : blobs) {
			if(!blob.equals(blobs.get(0))) { // adds every blob that's not the first blob to the tree
				blobTree.insert(blob);
			}
		}
		// For each blob, see if anybody else collided with it
		for(Blob blob : blobs) {
			findCollisions(blobTree, blob); // run method on every blob
		}
	}

	/** Recursive helper method that finds any blobs in blobTree that collide with "blob" and adds them to colliders */
	private void findCollisions(PointQuadtreeExtension<Blob> blobTree, Blob blob) {
		int blobX = (int) blob.getX(); // x, y, and radius of blob we are searching for collisions
		int blobY = (int) blob.getY();
		int blobR = (int) blob.getR();
		int x1 = blobTree.getX1(); // boundaries of tree region
		int y1 = blobTree.getY1();
		int x2 = blobTree.getX2();
		int y2 = blobTree.getY2();
		int x = (int) blobTree.getPoint().getX(); // x, y of blob of current tree
		int y = (int) blobTree.getPoint().getY();
		int r = (int) blobTree.getPoint().getR();

		// *** changed from 2*r to blobR + r to be able to accommodate collisions between blobs of different radii

		if(Geometry.circleIntersectsRectangle(blobX, blobY, r + blobR, x1, y1, x2, y2)) { // checks if blob is in the current blob's region ***
			if(!blob.equals(blobTree.getPoint())) { // if the blob is not itself - otherwise, all blobs are considered "collided" with themselves
				if (Geometry.pointInCircle(x, y, blobX, blobY, blobR + r)) { // check to see if they collide ***
					colliders.add(blobTree.getPoint()); // add them to colliders list
				}
			}
			if(blobTree.hasChild(1)) { // recurse into each of the child trees
				findCollisions(blobTree.getChild(1), blob);
			} if(blobTree.hasChild(2)) {
				findCollisions(blobTree.getChild(2), blob);
			} if(blobTree.hasChild(3)) {
				findCollisions(blobTree.getChild(3), blob);
			} if(blobTree.hasChild(4)) {
				findCollisions(blobTree.getChild(4), blob);
			}
		}
	}

	/** DrawingGUI method, here moving all the blobs and checking for collisions */
	public void handleTimer() {
		// Ask all the blobs to move themselves.
		for (Blob blob : blobs) {
			blob.step();
		}
		// Check for collisions
		if (blobs.size() > 0) {
			findColliders();
			if (collisionHandler=='d') {
				blobs.removeAll(colliders);
				colliders.clear();
			}
		}
		// Now update the drawing
		repaint();
	}

	/**
	 * RUNNER
	 * --------------------
	 */

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CollisionGUIExtension();
			}
		});
	}
}
