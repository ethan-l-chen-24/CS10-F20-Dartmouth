/**
 * RegionFinderExtension
 * A class that contains functionality such that it can take an image, identify regions of that image of a certain
 * color and separate them into specific "regions", and recolor the image in those regions.
 *
 * Introduces ColorRegion, which contain an ArrayList of points as well as a color
 *
 * @student -> Ethan Chen & Kysen Osburn
 * @date -> September 28, 2020
 * @class -> CS10, Fall 2020, Pierson
 * --------------------
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 *
 * @author Chris Bailey-Kellogg, Winter 2014 (based on a very different structure from Fall 2012)
 * @author Travis W. Peters, Dartmouth CS 10, Updated Winter 2015
 * @author CBK, Spring 2015, updated for CamPaint
 */
public class RegionFinderExtension {

    /**
     * VARIABLES
     * ------------------
     */

    private static final int maxColorDiff = 30;				// how similar a pixel color must be to the target color, to belong to a region
    private static final int minRegion = 30; 				// how many points in a region to be worth considering

    private BufferedImage image;                            // the image in which to find regions
    private BufferedImage recoloredImage;                   // the image with identified regions recolored

    private ArrayList<ColoredRegion> regions;			// a region is a list of points
    private ArrayList<ColoredRegion> largestRegions;
    // necessary for CamPaintExtension - keeps tracks of all of the largest regions from each time largestRegion() is called/each frame of the webcam

    /**
     * CONSTRUCTORS
     * --------------------
     */

    public RegionFinderExtension() { // default empty constructor
        this.image = null;
        this.regions = new ArrayList<ColoredRegion>(); // initializing regions in constructor
        this.largestRegions = new ArrayList<ColoredRegion>();
        this.recoloredImage = null;
    }

    public RegionFinderExtension(BufferedImage image) {
        this.image = image;
        this.regions = new ArrayList<ColoredRegion>(); // initializing regions in constructor
        this.largestRegions = new ArrayList<ColoredRegion>();
        this.recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);

    }

    /**
     * GETTERS AND SETTERS
     * --------------------
     */

    public void setImage(BufferedImage image) {
        this.image = image;
        this.recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage getRecoloredImage() {
        return this.recoloredImage;
    }

    public ArrayList<ColoredRegion> getRegions() {
        return this.regions;
    }

    public ArrayList<ColoredRegion> getLargestRegions() {
        return this.largestRegions;
    }

    public void setLargestRegions(ArrayList<ColoredRegion> regions) {
        this.largestRegions = regions;
    }

    /**
     * METHODS
     * --------------------
     */

    /**
     * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
     */
    public void findRegions(Color targetColor, Color paintColor) {

        BufferedImage visited = new BufferedImage(this.image.getWidth(), this.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // keeps track of which pixels have already been visited by the algorithm

        for(int x = 0; x<this.image.getWidth(); x++) { // loop through all of the pixels in the image
            for(int y = 0; y<this.image.getHeight(); y++) {

                Color currColor = new Color(this.image.getRGB(x, y)); // get the pixel's color
                if(visited.getRGB(x, y) == 0 && colorMatch(targetColor, currColor)) { // check if the pixel is visited and the color matches the targetColor

                    ArrayList<Point> colorRegion = new ArrayList<Point>(); // declare and initialize a new region
                    ArrayList<Point> toVisit = new ArrayList<Point>(); // declare and initialize a list of points that will match the color and be within the same region
                    Point coloredPoint = new Point(x, y);
                    toVisit.add(coloredPoint); // add the first point in region

                    /** Pierson's Algorithm */

                    visited.setRGB(x, y, 1);
                    while(toVisit.size() != 0) { // as long as there are still points to visit
                        Point p = toVisit.get(0);
                        colorRegion.add(p); // add the point to the region
                        toVisit.remove(p); // remove it from toVisit - it has now been visited
                        for(Point neighbor : getNeighbors(p)) { // get all of the points neighbors
                            int neighborX = (int) neighbor.getX();
                            int neighborY = (int) neighbor.getY();
                            Color neighborColor = new Color(this.image.getRGB(neighborX, neighborY));
                            if(visited.getRGB(neighborX, neighborY) == 0 && colorMatch(targetColor, neighborColor)) { // make sure the neighbors haven't been visited and are of the correct color
                                toVisit.add(neighbor); // add the neighbor to toVisit
                                visited.setRGB(neighborX, neighborY, 1); // set the neighbor added to toVisit as visited
                            }
                        }
                    }

                    // -------------------------------------
                    /** Ethan and Kysen's Algorithm */
                    /*
                    while(toVisit.size() != 0) { // as long as there are points
                        Point p = toVisit.get(0);
                        int pX = (int) p.getX();
                        int pY = (int) p.getY();

                        if(visited.getRGB(pX, pY) == 0) { // check to see if it has been visited
                            Color pColor = new Color(this.image.getRGB(pX, pY));
                            if(colorMatch(pColor, targetColor)) { // make sure it passes color test
                                colorRegion.add(p); // add it to the region
                                addNeighbors(toVisit, p); // add its neighbors to get checked
                            }
                            visited.setRGB(pX, pY, 1); // this point has now been visited
                        }
                        toVisit.remove(p); // remove it from the list
                    } */
                    // -------------------------------------


                    if(colorRegion.size() >= minRegion) { // if the region is larger than the min size, add it to regions
                        regions.add(new ColoredRegion(colorRegion, paintColor));
                    }
                }
            }
        }
    }

    /**
     * Helper Method: Get Neighbors - Part of Pierson's Algorithm
     * Returns an ArrayList with each of the 8 neighbors to point "point"
     */
    private ArrayList<Point> getNeighbors(Point point) {

        ArrayList<Point> neighbors = new ArrayList<Point>();

        int x = (int) point.getX();
        int y = (int) point.getY();

        // adds all of the neighboring points to arraylist and returns, as long as those points have valid indices
        if(point.getX() > 0) {
            if(point.getY() > 0) {
                neighbors.add(new Point(x - 1, y - 1));
                neighbors.add(new Point(x - 1, y));
            }
            if(point.getY() < this.image.getHeight()-1) {
                neighbors.add(new Point(x - 1, y + 1));
                neighbors.add(new Point(x, y + 1));
            }
        }

        if(point.getX() < this.image.getWidth()-1) {
            if(point.getY() > 0) {
                neighbors.add(new Point(x + 1, y - 1));
                neighbors.add(new Point(x, y - 1));
            }
            if(point.getY() < this.image.getHeight()-1) {
                neighbors.add(new Point(x + 1, y + 1));
                neighbors.add(new Point(x + 1, y));
            }
        }

        return neighbors; // return the arraylist of neighbors
    }

    /**
     * Helper Method: Add Neighbors - Part of Ethan and Kysen's Algorithm
     * Adds each of the eight neighbors of point "point" to the given ArrayList toVisit
     */
    private void addNeighbors(ArrayList<Point> toVisit, Point point) {
        int x = (int) point.getX();
        int y = (int) point.getY();

        // adds all of the neighboring points to given arraylist and returns, as long as those points have valid indices
        if(point.getX() > 0) {
            if(point.getY() > 0) {
                toVisit.add(new Point(x - 1, y - 1));
                toVisit.add(new Point(x - 1, y));
            }
            if(point.getY() < this.image.getHeight() - 1) {
                toVisit.add(new Point(x - 1, y + 1));
                toVisit.add(new Point(x, y + 1));
            }
        }

        if(point.getX() < this.image.getWidth() - 1) {
            if(point.getY() > 0) {
                toVisit.add(new Point(x + 1, y - 1));
                toVisit.add(new Point(x, y - 1));
            }
            if(point.getY() < this.image.getHeight() - 1) {
                toVisit.add(new Point(x + 1, y + 1));
                toVisit.add(new Point(x + 1, y));
            }
        }
    }

    /**
     * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
     */
    private static boolean colorMatch(Color c1, Color c2) {
        double rDiff = c1.getRed() - c2.getRed(); // get the differences of all of the colors
        double gDiff = c1.getGreen() - c2.getGreen();
        double bDiff = c1.getBlue() - c2.getBlue();
        double colorDiff = Math.sqrt(Math.pow(rDiff, 2) + Math.pow(gDiff, 2) + Math.pow(bDiff, 2)); // find euclidean distance from the target color
        if(colorDiff <= maxColorDiff) { // if the distance from the color is less then the max allowed, return true, else false
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the largest region detected (if any region has been detected)
     */
    public ColoredRegion largestRegion() {
        int largestRegionSize = 0;
        ColoredRegion largestRegion = null;
        for(ColoredRegion region : regions) { // goes through all of the regions
            if(region.getRegion().size() > largestRegionSize) { // if it is larger than the largest, set as largest
                largestRegionSize = region.getRegion().size();
                largestRegion = region;
            }
        }
        largestRegions.add(largestRegion); // add it to largestRegions - important for CamPaintExtension
        return largestRegion;
    }

    /**
     * Clears the regions arraylist for new mouseclick
     */
    public void clearRegions() {
        this.regions = new ArrayList<ColoredRegion>();
    } // resets regions to empty ArrayList

    /**
     * Sets recoloredImage to be a copy of image,
     * but with each region a uniform random color,
     * so we can see where they are
     */
    public void recolorImage() {
        // First copy the original
        recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
        // Now recolor the regions in it
        for(ColoredRegion region : regions) { // for each region
            if(region != null) { // if it exists
                for (Point p : region.getRegion()) { // color each point the color the region matches
                    recoloredImage.setRGB((int) p.getX(), (int) p.getY(), region.getColor().getRGB());
                }
            }
        }
    }

    /**
     * Sets recoloredImage to be a copy of image,
     * but with each region a given color
     */
    public void recolorImage(ArrayList<ColoredRegion> regions, BufferedImage image) {
        for(ColoredRegion region : regions) { // for each region
            if(region != null) { // if it exists
                for (Point p : region.getRegion()) { // color each point the color the region matches
                    image.setRGB((int) p.getX(), (int) p.getY(), region.getColor().getRGB());
                }
            }
        }
    }

    /**
     * PRIVATE CLASS
     *
     * ColoredRegion
     * A class that contains a region (ArrayList of Points) and a color as well as getters and setters to access said variables
     */

    private class ColoredRegion {

        /**
         * VARIABLES
         */

        ArrayList<Point> region;
        Color color;

        /**
         * CONSTRUCTORS
         * @param region - list of points
         * @param color - color of points when painted
         */

        private ColoredRegion(ArrayList<Point> region, Color color) {
            this.region = region;
            this.color = color;
        }

        /**
         * GETTERS AND SETTERS
         */

        public ArrayList<Point> getRegion() {
            return region;
        }

        public Color getColor() {
            return color;
        }

    }

}
