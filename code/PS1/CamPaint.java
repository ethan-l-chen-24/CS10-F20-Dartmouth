/**
 * CamPaint
 * A class that can take the color of an object on a webcam and paint a screen as the object is moved around.
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

public class CamPaint extends Webcam {

    /**
     * VARIABLES
     * --------------------
     */

    private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
    private RegionFinder finder;			// handles the finding
    private Color targetColor;          	// color of regions of interest (set by mouse press)
    private Color paintColor = Color.blue;	// the color to put into the painting from the "brush"
    private BufferedImage painting;			// the resulting masterpiece
    private boolean paintMode = false;


    /**
     * CONSTRUCTOR
     * -------------------
     */

    /** Initializes the region finder and the drawing */
    public CamPaint() {
        finder = new RegionFinder();
        clearPainting();
    }


    /**
     * METHODS
     * -------------------
     */

    /** Resets the painting to a blank image */
    protected void clearPainting() {
        painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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

    /** Webcam method, here finding regions and updating the painting */
    @Override
    public void processImage() {
        finder.setImage(image); // set region finder image to new frame in webcam
        finder.recolorImage(paintColor); // recolor the recoloredImage
        finder.recolorImage(paintColor, finder.getLargestRegions(), finder.getRecoloredImage()); // draw in the largestRegions (paint)
        if(paintMode) { // if painting is enabled
            finder.clearRegions(); // clear all regions (avoids unnecessary redrawing)
            finder.findRegions(targetColor); // find the regions of the appropriate color and set regions to current paintColor
            finder.largestRegion(); // add this frames largest region to the list of largest regions

            finder.recolorImage(paintColor); // recolor the recoloredImage to the updated frame
            finder.recolorImage(paintColor, finder.getLargestRegions(), finder.getRecoloredImage());
            finder.recolorImage(paintColor, finder.getLargestRegions(), painting); // draw the largestRegions (paint) on the painting
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
                new CamPaint();
            }
        });
    }
}
