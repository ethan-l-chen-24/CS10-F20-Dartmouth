/**
 * CamPaintExtension
 * A class that can take the color of an object on a webcam and paint a screen as the object is moved around.
 *
 * 1, 2, 3, 0 keys paint different colors
 * c clears the painting
 * t toggles paint mode on and off once initial color is clicked
 *
 * @student -> Ethan Chen & Kysen Osburn
 * @date -> September 28, 2020
 * @class -> CS10, Fall 2020, Pierson
 * --------------------
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Webcam-based drawing
 * Scaffold for PS-1, Dartmouth CS 10, Fall 2016
 *
 * @author Chris Bailey-Kellogg, Spring 2015 (based on a different webcam app from previous terms)
 */

public class CamPaintExtension extends Webcam {

    /**
     * VARIABLES
     * --------------------
     */

    private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
    private RegionFinderExtension finder;	// handles the finding
    private Color targetColor = null;       // color of regions of interest (set by mouse press)
    private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
    private BufferedImage painting;			// the resulting masterpiece
    private boolean paintMode = false;      // controls if region painting is activated


    /**
     * CONSTRUCTOR
     * -------------------
     */

    /** Initializes the region finder and the drawing */
    public CamPaintExtension() {
        finder = new RegionFinderExtension(); // initializes the regionFinder
        clearPainting(); // sets painting to a blank canvas
    }


    /**
     * METHODS
     * -------------------
     */

    /** Resets the painting to a blank image */
    protected void clearPainting() {
        painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // sets the painting to a blank canvas
    }

    /**
     * DrawingGUI method, here drawing one of live webcam, recolored image, or painting,
     * depending on display variable ('w', 'r', or 'p')
     */
    @Override
    public void draw(Graphics g) {
        if(displayMode == 'w') { // shows webcam
            g.drawImage(image, 0, 0, null);
        } else if(displayMode == 'r') { // shows recolored image
            g.drawImage(finder.getRecoloredImage(), 0, 0, null);
        } else if(displayMode == 'p') { // shows painting
            g.drawImage(painting, 0, 0, null);
        }
    }

    /** Webcam method, here finding regions and updating the painting. */
    @Override
    public void processImage() {
        finder.setImage(image); // set region finder image to new frame in webcam
        finder.recolorImage(); // recolor the recoloredImage
        finder.recolorImage(finder.getLargestRegions(), finder.getRecoloredImage()); // draw in the largestRegions (paint)
        if(paintMode) { // if painting is enabled
            finder.clearRegions(); // clear all regions (avoids unnecessary redrawing)
            finder.findRegions(targetColor, paintColor); // find the regions of the appropriate color and set regions to current paintColor
            finder.largestRegion(); // add this frames largest region to the list of largest regions

            finder.recolorImage(); // recolor the recoloredImage to the updated frame
            finder.recolorImage(finder.getLargestRegions(), finder.getRecoloredImage());
            finder.recolorImage(finder.getLargestRegions(), painting); // draw the largestRegions (paint) on the painting

        }
    }

    /** Overrides the DrawingGUI method to set the track color. */
    @Override
    public void handleMousePress(int x, int y) {
        if(!paintMode) { // only if not currently in paint mode
            targetColor = new Color(image.getRGB(x, y)); // set target color as color at point where clicked
            paintMode = true; // enable painting
        }
    }

    /** DrawingGUI method, here doing various drawing commands */
    @Override
    public void handleKeyPress(char k) {
        if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
            displayMode = k;
            if(k == 'p') {
                System.out.println("You have switched to paint mode.");
            } else if(k == 'r') {
                System.out.println("You have switched to recolor mode.");
            } else {
                System.out.println("You have switched to webcam mode.");
            }
        }
        else if (k == 'c') { // clear
            clearPainting();
            finder.setLargestRegions(new ArrayList());
            System.out.println("You have cleared the canvas.");
        }
        else if (k == 'o') { // save the recolored image
            saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
            System.out.println("You have saved the recolored image.");
        }
        else if (k == 's') { // save the painting
            saveImage(painting, "pictures/painting.png", "png");
            System.out.println("You have saved the painting.");
        }
        else if (k == 't') { // toggle paint mode, only works after first color selected
            if(targetColor != null) {
                paintMode = !paintMode;
                if(paintMode == true) {
                    System.out.println("You have turned on paint mode");
                } else {
                    System.out.println("You have turned off paint mode");
                }
            }
        }


        else if(k == '1') { // set color as blue
            paintColor = Color.blue;
            System.out.println("Color changed to blue.");
        }
        else if(k == '2') { // set color as red
            paintColor = Color.red;
            System.out.println("Color changed to red.");
        }
        else if(k == '3') { // set color as green
            paintColor = Color.green;
            System.out.println("Color changed to green.");
        }
        else if(k == '4') { // set color as yellow
            paintColor = Color.yellow;
            System.out.println("Color changed to yellow.");
        }
        else if(k == '0') { // set color as color of paintbrush
            if(targetColor != null) {
                paintColor = targetColor;
                System.out.println("Color changed to color of paintbrush.");
            }
        }

        else {
            System.out.println("unexpected key "+k);
        }
    }

    /**
     * RUNNER
     * --------------------
     */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CamPaintExtension();
            }
        });
    }
}
