package src.slave;

import java.io.PrintStream;
import java.util.HashMap;

import src.CommunicationException;
import src.Server;
// import src.SynchronizationMessage;

/**
 * SlaveServerThread.java
 * 
 * A Thread for a slave node to receive the HashMap from the map phase.
 */
public class SlaveServerThread<K,V> extends Thread {

    private final Server server ;
    private HashMap<K,V> data;

    /**
     * Constructor.
     * @param is the input stream of the socket
     */
    public SlaveServerThread(int port) {
        this.server = new Server(port);
        printOut("-> server thread for port " + port);
        // this.data = new HashMap<>();
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
                data = (HashMap<K,V>) received;
            } else {
                printErr("received an unknown object: " + received);
            }
            // Version with Pair to extend the maximum size to receive
            // Object received = null ;
            // while (true) {
            //     received = server.receiveObject();
            //     if (received.equals(SynchronizationMessage.COMMUNICATION_END)) {
            //         break;
            //     } else if (received instanceof Pair) {
            //         Pair<K,V> pair = (Pair<K,V>) received;
            //         data.put(pair.key, pair.value);
            //         // printOut("received data of " + data.size() + " elements");
            //     } else {
            //         printErr("received an unknown object: " + received);
            //     }
            // }
            server.closeConnection();
        } catch (CommunicationException e) {
            printErr("error occurs during communication: " + e.getMessage());
            System.exit(1);
        } catch (Throwable e) {
            printErr("error : " + e.getMessage());
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
    public HashMap<K,V> getData() {
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
