/**
 * PointQuadTreeExtension
 * An extension on the PointQuadTree class that includes a toString method to print out a quad tree according
 * to the family hierarchy where children are indented
 *
 * @name -> Ethan Chen
 * @date -> October 7, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, explicit rectangle
 * @author CBK, Fall 2016, generic with Point2D interface
 * 
 */
public class PointQuadtreeExtension<E extends Point2D> {

	/**
	 * VARIABLES
	 * --------------------
	 */

	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtreeExtension<E> q1, q2, q3, q4;	// children

	private ArrayList<E> circleRectangleTested; // for DotTreeGUIExtension
	private ArrayList<E> pointInCircleTested; // for DotTreeGUIExtension

	/**
	 * CONSTRUCTOR
	 * --------------------
	 */

	/** Initializes a leaf quadtree, holding the point in the rectangle */
	public PointQuadtreeExtension(E point, int x1, int y1, int x2, int y2) {
		circleRectangleTested = new ArrayList<E>();
		pointInCircleTested = new ArrayList<E>();
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	/**
	 * GETTERS
	 * --------------------
	 */
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}

	public ArrayList<E> getCircleRectangleTested() {
		return circleRectangleTested;
	}

	public ArrayList<E> getPointInCircleTested() {
		return pointInCircleTested;
	}

	/**
	 * METHODS
	 * --------------------
	 */

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public PointQuadtreeExtension<E> getChild(int quadrant) {
		if (quadrant==1) return q1;
		if (quadrant==2) return q2;
		if (quadrant==3) return q3;
		if (quadrant==4) return q4;
		return null;
	}

	/**
	 * Returns whether or not there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && q1!=null) || (quadrant==2 && q2!=null) || (quadrant==3 && q3!=null) || (quadrant==4 && q4!=null);
	}

	/** Inserts the point into the tree */
	public void insert(E p2) {
		if(p2.getX() > point.getX() && p2.getY() < point.getY()) { // quadrant 1
			if(hasChild(1)) { // if the child exists
				q1.insert(p2); // add the child at that next tree
			} else {
				q1 = new PointQuadtreeExtension<E>(p2, (int) point.getX(), y1, x2, (int) point.getY()); // make it the child
			}
		}

		else if(p2.getX() < point.getX() && p2.getY() < point.getY()) { // quadrant 2, same as for quadrant 1
			if(hasChild(2)) {
				q2.insert(p2);
			} else {
				q2 = new PointQuadtreeExtension<E>(p2, x1, y1, (int) point.getX(), (int) point.getY());
			}
		}

		else if(p2.getX() < point.getX() && p2.getY() > point.getY()) { // quadrant 3, same as for above quadrants
			if(hasChild(3)) {
				q3.insert(p2);
			} else {
				q3 = new PointQuadtreeExtension<E>(p2, x1, (int) point.getY(), (int) point.getX(), y2);
			}
		}

		else if(p2.getX() > point.getX() && p2.getY() > point.getY()) { // quadrant 4, same as for above quadrants
			if(hasChild(4)) {
				q4.insert(p2);
			} else {
				q4 = new PointQuadtreeExtension<E>(p2, (int) point.getX(), (int) point.getY(), x2, y2);
			}
		}
	}
	
	/** Finds the number of points in the quadtree (including its descendants) */
	public int size() {
		int size = 1;
		if(hasChild(1)) { // add the single node size (1) + the size of each subtree, recurse
			size += q1.size();
		} if(hasChild(2)) { // add quadrant 2's size
			size += q2.size();
		} if(hasChild(3)) { // add quadrant 3's size
			size += q3.size();
		} if(hasChild(4)) { // add quadrant 4's size
			size += q4.size();
		}
		return size;
	}
	
	/** Builds a list of all the points in the quadtree (including its descendants)*/
	public List<E> allPoints() {
		ArrayList<E> pointList = new ArrayList<E>(); // creates new arraylist to store and return list
		getAllPoints(pointList); // calls recursive helper function
		return pointList;
	}

	/** Helper method for allPoints(), adds points in quadTree into list */
	private void getAllPoints(List<E> list) {
		list.add(point); // add point at current location in tree
		if(hasChild(1)) { // go through all branches and add them and their children
			q1.getAllPoints(list);
		} if(hasChild(2)) {
			q2.getAllPoints(list);
		} if(hasChild(3)) {
			q3.getAllPoints(list);
		} if(hasChild(4)) {
			q4.getAllPoints(list);
		}
	}

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {
		ArrayList<E> pointsInCircle = new ArrayList<E>(); // creates new arraylist to store and return list
		circleRectangleTested = new ArrayList<E>(); // clearing arrayLists
		pointInCircleTested = new ArrayList<E>();
		findPointsInCircle(cx, cy, cr, pointsInCircle, this.circleRectangleTested, this.pointInCircleTested); // calls recursive helper method
		return pointsInCircle;
	}

	/** Helper method for findInCircle(), adds points in circle to list */
	private void findPointsInCircle(double cx, double cy, double cr, List<E> list, List<E> circleRectangleTested, List<E>pointInCircleTested) {
		circleRectangleTested.add(point); // adds points that are being tested to see if circle intersects rectangle - for extension
		if(Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) { // checks if circle intersects region of current point
			pointInCircleTested.add(point); // adds points that are being tested to see if point is in circle - for extension
			if(Geometry.pointInCircle(point.getX(), point.getY(), cx, cy, cr)) { // checks if the point is in that circle
				list.add(point); // if it does, it is a point in the circle, added to list
			}
			if(hasChild(1)) { // check for all children if its region was intersected
				q1.findPointsInCircle(cx, cy, cr, list, circleRectangleTested, pointInCircleTested);
			} if(hasChild(2)) {
				q2.findPointsInCircle(cx, cy, cr, list, circleRectangleTested, pointInCircleTested);
			} if(hasChild(3)) {
				q3.findPointsInCircle(cx, cy, cr, list, circleRectangleTested, pointInCircleTested);
			} if(hasChild(4)) {
				q4.findPointsInCircle(cx, cy, cr, list, circleRectangleTested, pointInCircleTested);
			}
		}
	}

	/** StringOut method */
	@Override
	public String toString() {
		String string = getStringOut(0); // calls recursive helper method
		return string;
	}

	/** helper method for toString(), actually creates string */
	public String getStringOut(int level) { // numIndents - number of indents/levels down in tree
		String string = "";
		for(int x = 0; x<level; x++) {
			string += "   "; // indents (level) times
		}
		string += ("X:" + point.getX() + "  Y:" + point.getY() + "\n"); // string output for x and y coordinate
		if(hasChild(1)) { // add the string output of all of the other children in order by quadrant
			string += q1.getStringOut(level + 1);
		} if(hasChild(2)) { // quadrant 2
			string += q2.getStringOut(level + 1);
		} if(hasChild(3)) { // quadrant 3
			string += q3.getStringOut(level + 1);
		} if(hasChild(4)) { // quadrant 4
			string += q4.getStringOut(level + 1);
		}
		return string;
	}

	/**
	 * RUNNER
	 * --------------------
	 */

	public static void main(String[] args) {
		PointQuadtreeExtension<Dot> rootTree = new PointQuadtreeExtension<Dot>(new Dot(500, 500), 0, 0, 1000, 1000);
		rootTree.insert(new Dot(130, 670)); // adding points
		rootTree.insert(new Dot(120, 800));
		rootTree.insert(new Dot(280, 680));
		rootTree.insert(new Dot(680, 330));
		rootTree.insert(new Dot(120, 120));
		rootTree.insert(new Dot(680, 970));
		System.out.println(rootTree); // testing toString method
		System.out.println(rootTree.size()); // testing size method
		for(Dot p : rootTree.allPoints()) { // testing allPoints method
			System.out.println(p.getX() + " " + p.getY());
		}
	}

}
