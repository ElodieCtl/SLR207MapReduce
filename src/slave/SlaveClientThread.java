package src.slave;

import java.io.PrintStream;
import java.io.Serializable;

import src.Client;
import src.CommunicationException;

/**
 * SlaveClientThread.java
 * 
 * A Thread for a slave node to send data.
 */
public class SlaveClientThread extends Thread {

    private final Client client;
    private final Serializable object;
    
    /**
     * Constructor.
     * @param serverHost the hostname of the server
     * @param serverPort the port of the server
     * @param object Serializable object to send
     */
    public SlaveClientThread(String serverHost, int serverPort, Serializable object) {
        this.client = new Client(serverHost, serverPort);
        this.object = object;
        // printOut("-> client thread for " + serverHost + ":" + serverPort
        //        + " to send " + object);
    }
    
    @Override
    public void run() {
        try {
            this.client.openConnection();
            printOut("connected to " + this.client.getAddress());
            this.client.sendObject(this.object);
            // printOut("sent data" + this.object);
            this.client.closeConnection();
        } catch (CommunicationException e) {
            printErr("error during communication : " + e.getMessage());
            System.exit(1);
        }
    }

    private void identifiedPrint(String message, PrintStream stream) {
        stream.println("[" + this.getId() + "] " + message);
    }

    /**
     * Print a message to the standard output.
     * @param message the message to print
     */

    private void printOut(String message) {
        identifiedPrint(message, System.out);
    }

    /**
     * Print a message to the standard error.
     * @param message the message to print
     */
    private void printErr(String message) {
        identifiedPrint(message, System.err);
    }
    
}
