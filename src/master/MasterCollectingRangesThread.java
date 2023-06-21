package src.master;

import src.Range ;

import src.Client;
import src.CommunicationException;

public class MasterCollectingRangesThread extends Thread {
    
    private final Client client;
    private Range range ;

    /**
     * Constructor.
     * @param Client the client which is connected to the slave
     */
    public MasterCollectingRangesThread(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null.");
        }
        this.client = client;
    }

    @Override
    public void run() {

        // Wait for the slave to send the reduced map

        try {
            Object message = client.receiveObject();
            System.out.println("Received an Object from " + client.getAddress());
            if (message instanceof Range) {
                this.range = (Range) message ;
            } else {
                throw new CommunicationException("Received an unexpected message instead of a Range : " + message);
            }
        } catch (CommunicationException e) {
            System.err.println("Error in thread " + this.getId() + ": " + e.getMessage());
            System.exit(1);
        }

    }

    public Range getRange() {
        return this.range;
    }
}
