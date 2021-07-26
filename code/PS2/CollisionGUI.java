/**
 * CollisionGUI
 * A GUI that can create blobs (wanderers and bouncers) on screen, check when they collide with one another, and
 * change the color of those blobs to red or delete them
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
public class CollisionGUI extends DrawingGUI {

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

    public CollisionGUI() {
        super("super-collider", width, height);

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
            blobs.add(new Bouncer(x,y,width,height));
        }
        else if (blobType=='w') { // wanderer
            blobs.add(new Wanderer(x,y));
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
        PointQuadtree<Blob> blobTree = new PointQuadtree<Blob>(blobs.get(0), 0, 0, width, height);
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
    private void findCollisions(PointQuadtree<Blob> blobTree, Blob blob) {
        int blobX = (int) blob.getX(); // x, y, and radius of blob we are searching for collisions
        int blobY = (int) blob.getY();
        int blobR = (int) blob.getR();
        int x1 = blobTree.getX1(); // boundaries of tree region
        int y1 = blobTree.getY1();
        int x2 = blobTree.getX2();
        int y2 = blobTree.getY2();
        int x = (int) blobTree.getPoint().getX(); // x, y of blob of current tree
        int y = (int) blobTree.getPoint().getY();

        if(Geometry.circleIntersectsRectangle(blobX, blobY, 2 * blobR, x1, y1, x2, y2)) { // checks if blob is in the current blob's region
            if(!blob.equals(blobTree.getPoint())) { // if the blob is not itself - otherwise, all blobs are considered "collided" with themselves
                if (Geometry.pointInCircle(x, y, blobX, blobY, 2 * blobR)) { // check to see if they collide
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
                new CollisionGUI();
            }
        });
    }
}

