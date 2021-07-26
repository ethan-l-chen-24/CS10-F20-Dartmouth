/**
 * Sketch Class
 * Contains the collection of objects and their ids, as well as methods to process requests from clients
 *
 * @name -> Ethan Chen
 * @date -> November 14, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class Sketch {

    /**
     * VARIABLES
     * --------------------
     */

    private TreeMap<Integer, Shape> shapes;
    private static int lastKey;

    /**
     * CONSTRUCTOR
     * --------------------
     */

    public Sketch() {
        shapes = new TreeMap<>();
    }

    /**
     * METHODS
     * --------------------
     */

    public TreeMap<Integer, Shape> getShapes() {
        return shapes;
    }

    /** gets the next available key (needs to be synchronized so that two requests do not grab the same key at the same time) */
    public static synchronized int getKey() {
        int id = lastKey; // gets the key and increments for next time method is called
        lastKey++;
        return id;
    }

    /** takes the string request message and performs appropriate operation */
    public synchronized void processRequest(String request) {
        String[] parts = request.split(" ");

        if (parts[0].equals("add")) { // figures out what operation and does correct one
            addShape(parts);
        } else if (parts[0].equals("move")) {
            moveShape(parts);
        } else if (parts[0].equals("recolor")) {
            recolorShape(parts);
        } else if (parts[0].equals("delete")) {
            deleteShape(parts);
        }
    }

    /** adds shape to the shapeList based on the given string */
    private void addShape(String[] parts) {
        if (parts[2].equals("ellipse")) { // creates and adds an ellipse
            int id = Integer.valueOf(parts[1]);
            int x1 = Integer.valueOf(parts[3]);
            int y1 = Integer.valueOf(parts[4]);
            int x2 = Integer.valueOf(parts[5]);
            int y2 = Integer.valueOf(parts[6]);
            int color = Integer.valueOf(parts[7]);
            Color c = new Color(color);
            Ellipse newEllipse = new Ellipse(x1, y1, x2, y2, c);
            shapes.put(id, newEllipse);

        } else if (parts[2].equals("rectangle")) { // creates and adds a rectangle
            int id = Integer.valueOf(parts[1]);
            int x1 = Integer.valueOf(parts[3]);
            int y1 = Integer.valueOf(parts[4]);
            int x2 = Integer.valueOf(parts[5]);
            int y2 = Integer.valueOf(parts[6]);
            int color = Integer.valueOf(parts[7]);
            Color c = new Color(color);
            Rectangle newRect = new Rectangle(x1, y1, x2, y2, c);
            shapes.put(id, newRect);

        } else if (parts[2].equals("segment")) { // creates and adds a segment
            int id = Integer.valueOf(parts[1]);
            int x1 = Integer.valueOf(parts[3]);
            int y1 = Integer.valueOf(parts[4]);
            int x2 = Integer.valueOf(parts[5]);
            int y2 = Integer.valueOf(parts[6]);
            int color = Integer.valueOf(parts[7]);
            Color c = new Color(color);
            Segment newSegment = new Segment(x1, y1, x2, y2, c);
            shapes.put(id, newSegment);

        } else if (parts[2].equals("polyline")) { // creates and adds a polyline
            int id = Integer.valueOf(parts[1]);
            int color = Integer.valueOf(parts[3]);
            int x1 = Integer.valueOf(parts[5]);
            int y1 = Integer.valueOf(parts[6]);
            Color c = new Color(color);

            Polyline newPolyline = new Polyline(x1, y1, c);
            for (int x = 11; x < parts.length; x += 6) { // continues for every segment there is (6 is space from one x and y value to the next)
                int x2 = Integer.valueOf(parts[x]);
                int y2 = Integer.valueOf(parts[x + 1]);
                newPolyline.nextPoint(x2, y2); // add the segment to that polyline
            }
            shapes.put(id, newPolyline);
        }
    }

    /** moves the identified shape by the given dx, dy amounts */
    private void moveShape(String[] parts) {
        Shape requested = shapes.get(Integer.valueOf(parts[1])); // gets object
        int dx = Integer.valueOf(parts[2]); // gets dy and dx
        int dy = Integer.valueOf(parts[3]);
        requested.moveBy(dx, dy); // moves the object by the amount
    }

    /** recolors the identified shape to the given color */
    private void recolorShape(String[] parts) {
        Shape requested = shapes.get(Integer.valueOf(parts[1])); // gets object
        int color = Integer.valueOf(parts[2]); // get color
        Color c = new Color(color);
        requested.setColor(c); // set object to color
    }

    /** deletes the identified object */
    private void deleteShape(String[] parts) {
        removeShape(Integer.valueOf(parts[1]));
    }

    /** returns the full list of add requests to create the current state of the shapes */
    /** this is important for when new editors join the server - brings them up to date */
    public ArrayList<String> getShapeMessages() {
        ArrayList<String> messages = new ArrayList<>();
        for (int id : shapes.keySet()) { // for every single shape
            String shapeMessage = ("add " + id + " " + shapes.get(id).toString()); // create the proper message and add it to the list
            messages.add(shapeMessage);
        }
        return messages;
    }

    /** deletes the shape from the shapes based off the ID */
    public void removeShape(int id) {
        shapes.remove(id);
    }

    /** adjusts the key to the value 1 greater than the highest key */
    /** without this method, every time a new client joins the server, lastKey is reset to 0 -> not ideal */
    /** important for when new editors join the server, also for freeing up id space if many many objects although */
    /** it is unlikely the upper limit on the int would ever be reached */
    public void fixKey() {
        if (shapes.isEmpty()) { // if nothing, reset to 0
            lastKey = 0;
        } else {
            lastKey = shapes.lastKey() + 1; // 1 greater than the highest key
        }
    }

    /** gets the highest id number (latest added) that contains the point */
    public int getTopID(int x, int y) {
        for (int c : shapes.descendingKeySet()) { // for every shape from highest id to lowest (latest to oldest)
            if (shapes.get(c).contains(x, y)) { // if it contains point
                return c; // return its id
            }
        }
        return -1;
    }

}
