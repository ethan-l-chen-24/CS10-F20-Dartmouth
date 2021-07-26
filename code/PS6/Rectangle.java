/**
 * Rectangle Class
 * Knows how to make a rectangle
 *
 * @name -> Ethan Chen
 * @date -> November 14, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 */
public class Rectangle implements Shape {

    /**
     * VARIABLES
     * --------------------
     */

    private int x1, y1, x2, y2;        // upper left and lower right
    private Color color;

    /**
     * CONSTRUCTOR
     * --------------------
     */

    public Rectangle(int x1, int y1, int x2, int y2, Color color) {
        this.x1 = x1; // initialize
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
    }

    /** set the corners */
    public void setCorners(int x1, int y1, int x2, int y2) {
        this.x1 = x1; // set each point to necessary value
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public void moveBy(int dx, int dy) {
        this.x1 += dx; // move each point by necessary amount
        this.x2 += dx;
        this.y1 += dy;
        this.y2 += dy;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean contains(int x, int y) {
        return (x > x1 && x < x2 && y > y1 && y < y2); // if x is between x1 and x2 and y is between y1 and y2
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x1, y1, x2 - x1, y2 - y1); // draws the rectangle
    }

    public String toString() {
        return "rectangle " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + color.getRGB();
    }
}
