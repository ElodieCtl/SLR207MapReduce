package src.slave;

import java.io.PrintStream;
import java.util.HashMap;
// import java.util.stream.Collectors;

import src.Client;
import src.CommunicationException;
import src.SynchronizationMessage;

/**
 * SlaveClientThread.java
 * 
 * A Thread for a slave node to send data.
 */
public class SlaveClientThread<K, V> extends Thread {

    // private static final int MAX_ELEMENTS = 1_000_000;

    private final Client client;
    private final HashMap<K,V> object;
    
    /**
     * Constructor.
     * @param serverHost the hostname of the server
     * @param serverPort the port of the server
     * @param object Serializable object to send
     */
    public SlaveClientThread(String serverHost, int serverPort, HashMap<K,V> object) {
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

            // Version with pairs
            // for (K key : this.object.keySet()) {
            //     this.client.sendObject(new Pair<K,V>(key, this.object.get(key)));
            // }

            // Version with multiple HashMap
            // int nbPackets = this.object.size() / MAX_ELEMENTS + 1;
            // for (int i = 0; i < nbPackets; i++) {
            //     int start = i * MAX_ELEMENTS;
            //     int end = Math.min((i + 1) * MAX_ELEMENTS, this.object.size());
            //     HashMap<K,V> subObject = new HashMap<K,V>(this.object);
            //     subObject.keySet().retainAll(this.object.keySet().stream()
            //      .skip(start).limit(end - start).collect(Collectors.toList()));
            //     this.client.sendObject(subObject);
            //     printOut("sent data of " + subObject.size() + " elements");
            // }
            printOut("sent " + this.object.size() + " elements");
            // this.client.sendObject(SynchronizationMessage.COMMUNICATION_END);
            this.client.closeConnection();
        } catch (CommunicationException e) {
            printErr("error during communication : " + e.getMessage());
            System.exit(1);
        } catch (Throwable e) {
            printErr("error : " + e.getMessage());
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
