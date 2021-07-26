/**
 * EditorCommunicator Class
 * A thread that sends and receives messages from SketchServerCommunicator
 *
 * @name -> Ethan Chen
 * @date -> November 14, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
    private PrintWriter out;        // to server
    private BufferedReader in;        // from server
    protected Editor editor;        // handling communication for

    /**
     * Establishes connection and in/out pair
     */
    public EditorCommunicator(String serverIP, Editor editor) {
        this.editor = editor;
        System.out.println("connecting to " + serverIP + "...");
        try {
            Socket sock = new Socket(serverIP, 4242);
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            System.out.println("...connected");
        } catch (IOException e) {
            System.err.println("couldn't connect");
            System.exit(-1);
        }
    }

    /**
     * Sends message to the server
     */
    public void send(String msg) {
        out.println(msg);
    }

    /**
     * Keeps listening for and handling (your code) messages from the server
     */
    public void run() {
        try {
            // Handle messages
            String line;
            while ((line = in.readLine()) != null) {
                // Output what you read
                System.out.println(line); // print out the request
                editor.getSketch().processRequest(line); // send the request to the local sketch and process
                editor.getSketch().fixKey(); // reset key to lowest unique value > 0
                editor.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("server hung up");
        }
    }

}
