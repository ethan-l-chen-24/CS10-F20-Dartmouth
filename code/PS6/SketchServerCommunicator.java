/**
 * SketchServerCommunicator Class
 * A thread that sends and receives messages from EditorCommunicator
 *
 * @name -> Ethan Chen
 * @date -> November 14, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
    private Socket sock;                    // to talk with client
    private BufferedReader in;                // from client
    private PrintWriter out;                // to client
    private SketchServer server;            // handling communication for

    public SketchServerCommunicator(Socket sock, SketchServer server) {
        this.sock = sock;
        this.server = server;
    }

    /**
     * Sends a message to the client
     *
     * @param msg
     */
    public void send(String msg) {
        out.println(msg);
    }

    /**
     * Keeps listening for and handling (your code) messages from the client
     */
    public void run() {
        try {
            System.out.println("someone connected");

            // Communication channel
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream(), true);

            // Tell the client the current state of the world
            for (String message : server.getSketch().getShapeMessages()) {
                send(message); // when a new client connects, broadcasts them all of the instructions to match master sketch
            }

            // Keep getting and handling messages from the client
            String line;
            while ((line = in.readLine()) != null) {
                String message = line;
                System.out.println(message); // every message received is a request
                server.getSketch().processRequest(line); // send request to server
                server.broadcast(message); // broadcast request to everyone
            }

            // Clean up -- note that also remove self from server's list so it doesn't broadcast here
            server.removeCommunicator(this);
            out.close();
            in.close();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
