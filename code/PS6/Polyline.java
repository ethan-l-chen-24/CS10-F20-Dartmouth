/**
 * Polyline Class
 * Creates a combination of lines/segments such that it looks like a curve/freehand drawing
 *
 * @name -> Ethan Chen
 * @date -> November 14, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {

    /**
     * VARIABLES
     * ------------------
     */

    ArrayList<Segment> segments; // the list of segments that make up the freehand drawing
    Color color; // color
    int lastX;
    int lastY;

    /**
     * CONSTRUCTOR
     * --------------------
     */

    public Polyline(int firstX, int firstY, Color color) {
        // initialize variables
        segments = new ArrayList<>();
        segments.add(new Segment(firstX, firstY, color)); // add an initial segment with no length
        this.lastX = firstX;
        this.lastY = firstY;
        this.color = color;
    }

    /**
     * VARIABLES
     * --------------------
     */

    /** adds the next point in the line to the polyline */
    public void nextPoint(int x, int y) {
        segments.add(new Segment(lastX, lastY, x, y, color)); // add a new segment from the last to the next
        lastX = x; // set nexts as lasts
        lastY = y;
    }

    /** moves the entire list of segments over by the given amount */
    @Override
    public void moveBy(int dx, int dy) {
        for (Segment s : segments) {
            s.moveBy(dx, dy);
        }
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        for (Segment s : segments) {
            s.setColor(color);
        }
        this.color = color;
    }

    @Override
    public boolean contains(int x, int y) {
        for (Segment s : segments) {
            if (s.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Graphics g) {
        for (Segment s : segments) {
            s.draw(g);
        }
    }

    @Override
    public String toString() {
        String output = "polyline " + color.getRGB() + " ";

        for (Segment s : segments) {
            output += (s.toString() + " "); // NOTE ***
        }
        // *** since there are no getters or setters for the x and y, I had to resort to using the toString method for
        // each segment in order to get the endpoints. While normally I would only need the x and y coordinates of each
        // subsequent point, I cannot do this, so I had to print out the entire segment which includes start and end,
        // color, and "segment". Not ideal, but I can just choose the indices that are actually significant when I process
        // the text

        return output;

    }
}
