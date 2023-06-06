package src;

import src.Client.ClientException;

/**
 * MasterThread.java
 * 
 * A Thread for the master to connect to slaves, as a client.
 */
public class MasterWaitingThread extends Thread{

    private final Client client;
    private boolean readyToShuffle = false;

    /**
     * Constructor.
     * @param Client the client which is connected to the slave
     */
    public MasterWaitingThread(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null.");
        }
        this.client = client;
    }

    @Override
    public void run() {

        // Wait for READY_TO_SHUFFLE

        try {
            Object message = client.receiveObject();
            if (message == SynchronizationMessage.READY_TO_SHUFFLE) {
                this.readyToShuffle = true;
                System.out.println("Received READY_TO_SHUFFLE from " + client.getAddress());
            } else {
                System.err.println("Received " + message + " from " + client.getAddress());
            }
        } catch (ClientException e) {
            System.err.println("Error in thread " + this.getId() + ": " + e.getMessage());
            System.exit(1);
        }

    }

    public boolean isReadyToShuffle() {
        return this.readyToShuffle;
    }
    
}
