package src.master;

import java.util.HashMap;

import src.Client;
import src.CommunicationException;
import src.SortedMap;

/**
 * MasterCollectingThread.java
 * 
 * A Thread for the master to collect the reduced maps from the slaves.
 */
public class MasterCollectingThread extends Thread{

    private final Client client;
    private final SortedMap collectedData ;

    /**
     * Constructor.
     * @param Client the client which is connected to the slave
     */
    public MasterCollectingThread(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null.");
        }
        this.client = client;
        this.collectedData = new SortedMap().reduce();
    }

    @Override
    public void run() {

        // Wait for the slave to send the reduced map

        try {
            Object message = client.receiveObject();
            System.out.println("Received an Object from " + client.getAddress());
            if (message instanceof HashMap) {
                this.collectedData.insertHashMap((HashMap<String,Integer>) message);
            } else {
                throw new CommunicationException("Received an unexpected message instead of a HashMap" + message);
            }
        } catch (CommunicationException e) {
            System.err.println("Error in thread " + this.getId() + ": " + e.getMessage());
            System.exit(1);
        }

    }

    public SortedMap getSortedMap() {
        return this.collectedData;
    }
    
}
