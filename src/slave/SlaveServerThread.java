package src.slave;

import java.io.PrintStream;
import java.util.HashMap;

import src.CommunicationException;
import src.Server;

/**
 * SlaveServerThread.java
 * 
 * A Thread for a slave node to receive the HashMap from the map phase.
 */
public class SlaveServerThread extends Thread {

    private final Server server ;
    private Object data;

    /**
     * Constructor.
     * @param is the input stream of the socket
     */
    public SlaveServerThread(int port) {
        this.server = new Server(port);
        printOut("-> server thread for port " + port);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() { 
        try {
            server.openConnection();
            printOut("accepted connection");
            Object received = server.receiveObject();
            // printOut("Thread " + this.getId() + " received " + received);
            if (received instanceof HashMap) {
                data = received;
            } else {
                printErr("received an unknown object: " + received);
            }
            server.closeConnection();
        } catch (CommunicationException e) {
            printErr("error occurs during communication: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Get the HashMap.
     * 
     * <strong>This method should be called after the thread is finished !</strong>,
     * otherwise it may return null.
     * @return the HashMap as an Object
     */
    public Object getData() {
        return data;
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
