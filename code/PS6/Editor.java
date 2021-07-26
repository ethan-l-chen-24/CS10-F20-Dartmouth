/**
 * Editor Class
 * Creates a graphical editor in which shapes can be drawn
 *
 * @name -> Ethan Chen
 * @date -> November 14, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {
    private static String serverIP = "localhost";            // IP address of sketch server
    // "localhost" for your own machine;
    // or ask a friend for their IP address

    private static final int width = 800, height = 800;        // canvas size

    // Current settings on GUI
    public enum Mode {
        DRAW, MOVE, RECOLOR, DELETE
    }

    private Mode mode = Mode.DRAW;                // drawing/moving/recoloring/deleting objects
    private String shapeType = "ellipse";        // type of object to add
    private Color color = Color.black;            // current drawing color

    // Drawing state
    // these are remnants of my implementation; take them as possible suggestions or ignore them
    private Shape curr = null;                    // current shape (if any) being drawn
    private Sketch sketch;                        // holds and handles all the completed objects
    private int movingId = -1;                    // current shape id (if any; else -1) being moved
    private int drawId = -1;
    private Point drawFrom = null;                // where the drawing started
    private Point moveFrom = null;                // where object is as it's being dragged


    // Communication
    private EditorCommunicator comm;            // communication with the sketch server

    public Editor() {
        super("Graphical Editor");

        sketch = new Sketch();

        // Connect to server
        comm = new EditorCommunicator(serverIP, this);
        comm.start();

        // Helpers to create the canvas and GUI (buttons, etc.)
        JComponent canvas = setupCanvas();
        JComponent gui = setupGUI();

        // Put the buttons and canvas together into the window
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        cp.add(gui, BorderLayout.NORTH);

        // Usual initialization
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Creates a component to draw into
     */
    private JComponent setupCanvas() {
        JComponent canvas = new JComponent() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSketch(g);
            }
        };

        canvas.setPreferredSize(new Dimension(width, height));

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                handlePress(event.getPoint());
            }

            public void mouseReleased(MouseEvent event) {
                handleRelease();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent event) {
                handleDrag(event.getPoint());
            }
        });

        return canvas;
    }

    /**
     * Creates a panel with all the buttons
     */
    private JComponent setupGUI() {
        // Select type of shape
        String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
        JComboBox<String> shapeB = new JComboBox<String>(shapes);
        shapeB.addActionListener(e -> shapeType = (String) ((JComboBox<String>) e.getSource()).getSelectedItem());

        // Select drawing/recoloring color
        // Following Oracle example
        JButton chooseColorB = new JButton("choose color");
        JColorChooser colorChooser = new JColorChooser();
        JLabel colorL = new JLabel();
        colorL.setBackground(Color.black);
        colorL.setOpaque(true);
        colorL.setBorder(BorderFactory.createLineBorder(Color.black));
        colorL.setPreferredSize(new Dimension(25, 25));
        JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
                "Pick a Color",
                true,  //modal
                colorChooser,
                e -> {
                    color = colorChooser.getColor();
                    colorL.setBackground(color);
                },  // OK button
                null); // no CANCEL button handler
        chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

        // Mode: draw, move, recolor, or delete
        JRadioButton drawB = new JRadioButton("draw");
        drawB.addActionListener(e -> mode = Mode.DRAW);
        drawB.setSelected(true);
        JRadioButton moveB = new JRadioButton("move");
        moveB.addActionListener(e -> mode = Mode.MOVE);
        JRadioButton recolorB = new JRadioButton("recolor");
        recolorB.addActionListener(e -> mode = Mode.RECOLOR);
        JRadioButton deleteB = new JRadioButton("delete");
        deleteB.addActionListener(e -> mode = Mode.DELETE);
        ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
        modes.add(drawB);
        modes.add(moveB);
        modes.add(recolorB);
        modes.add(deleteB);
        JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
        modesP.add(drawB);
        modesP.add(moveB);
        modesP.add(recolorB);
        modesP.add(deleteB);

        // Put all the stuff into a panel
        JComponent gui = new JPanel();
        gui.setLayout(new FlowLayout());
        gui.add(shapeB);
        gui.add(chooseColorB);
        gui.add(colorL);
        gui.add(modesP);
        return gui;
    }

    /**
     * Getter for the sketch instance variable
     */
    public Sketch getSketch() {
        return sketch;
    }

    /**
     * Draws all the shapes in the sketch,
     * along with the object currently being drawn in this editor (not yet part of the sketch)
     */
    public void drawSketch(Graphics g) {
        if (!sketch.getShapes().isEmpty()) { // assuming shapes do exist
            for (int id : sketch.getShapes().keySet()) {
                Shape s = sketch.getShapes().get(id); // draw every shape in the sketch's shape list
                s.draw(g);
            }
        }
    }

    // Helpers for event handlers

    /**
     * Helper method for press at point
     * In drawing mode, start a new object;
     * in moving mode, (request to) start dragging if clicked in a shape;
     * in recoloring mode, (request to) change clicked shape's color
     * in deleting mode, (request to) delete clicked shape
     */
    private void handlePress(Point p) {
        if (mode.equals(Mode.DRAW)) { // if in DRAW mode
            drawFrom = new Point(p.x, p.y); // starting point
            drawId = sketch.getKey(); // new ID of new shape
            if (shapeType.equals("ellipse")) { // decides which shape to draw
                curr = new Ellipse(p.x, p.y, p.x, p.y, color); // shape that begins as a single point
            } else if (shapeType.equals("rectangle")) {
                curr = new Rectangle(p.x, p.y, p.x, p.y, color);
            } else if (shapeType.equals("freehand")) {
                curr = new Polyline(p.x, p.y, color);
            } else if (shapeType.equals("segment")) {
                curr = new Segment(p.x, p.y, p.x, p.y, color);
            }
            sketch.getShapes().put(drawId, curr); // put the shape into the local sketch

        } else if (mode.equals(Mode.MOVE)) { // if in MOVE mode
            moveFrom = new Point(p.x, p.y); // grab the point it was clicked
            movingId = sketch.getTopID(p.x, p.y); // get the id of the top item that contains the point

        } else if (mode.equals(Mode.RECOLOR)) { // if in RECOLOR mode
            int topID = sketch.getTopID(p.x, p.y); // get the id of the top item that contains the point
            if (topID != -1) {
                comm.send("recolor " + topID + " " + color.getRGB()); // if exists, request to recolor to server
            }
            repaint();

        } else if (mode.equals(Mode.DELETE)) {
            int topID = sketch.getTopID(p.x, p.y); // get the id of the top item that contains the point
            if (topID != -1) {
                comm.send("delete " + topID); // if exists, request to delete to server
            }
            repaint();
        }
    }

    /**
     * Helper method for drag to new point
     * In drawing mode, update the other corner of the object;
     * in moving mode, (request to) drag the object
     */
    private void handleDrag(Point p) {
        if (mode.equals(Mode.DRAW)) { // if in DRAW mode
            if (shapeType.equals("ellipse")) { // remake the shape with new endpoints
                Ellipse currEllipse = (Ellipse) curr;
                currEllipse.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
                curr = currEllipse;

            } else if (shapeType.equals("rectangle")) {
                Rectangle currRect = (Rectangle) curr;
                currRect.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
                // fillRect only allows drawing from top left corner to bottom right, so adjust coordinates to compensate
                if(p.x < drawFrom.x) {
                    currRect.setCorners(p.x, drawFrom.y, drawFrom.x, p.y);
                }
                if(p.y < drawFrom.y) {
                    currRect.setCorners(drawFrom.x, p.y, p.x, drawFrom.y);
                }
                if(p.x < drawFrom.x && p.y < drawFrom.y) {
                    currRect.setCorners(p.x, p.y, drawFrom.x, drawFrom.y);
                }
                curr = currRect;

            } else if (shapeType.equals("segment")) {
                Segment currSeg = (Segment) curr;
                currSeg.setEnd(p.x, p.y);
                curr = currSeg;

            } else if (shapeType.equals("freehand")) {
                Polyline currPolyline = (Polyline) curr; // in case of polyline, add the next point to its list
                currPolyline.nextPoint(p.x, p.y);
                curr = currPolyline;
            }

            sketch.getShapes().put(drawId, curr); // replace the shape in the local sketch
            repaint();

        } else if (mode.equals(Mode.MOVE)) { // if in MOVE mode
            int dx = p.x - moveFrom.x; // grab the delta
            int dy = p.y - moveFrom.y;
            moveFrom.setLocation(p.x, p.y); // set current points as last points
            if (movingId != -1) {
                comm.send("move " + movingId + " " + dx + " " + dy); // if something is moving, request to move to server
            }

            repaint();
        }
    }

    /**
     * Helper method for release
     * In drawing mode, pass the add new object request on to the server;
     * in moving mode, release it
     */
    private void handleRelease() {
        if (mode.equals(Mode.DRAW)) { // if in DRAW mode
            String message = ("add " + drawId + " " + curr.toString()); // request to server to add shape
            comm.send(message);
            curr = null; // reset variables
            drawId = -1;
        } else if (mode.equals(Mode.MOVE)) {
            moveFrom = null; // reset variables
            movingId = -1;
        }
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Editor();
            }
        });
    }
}
