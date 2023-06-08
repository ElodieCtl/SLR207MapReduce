package src;

/**
 * MasterThread.java
 * 
 * A Thread for the master to wait for the slave to send a message.
 */
public class MasterWaitingThread extends Thread{

    private final Client client;
    private final SynchronizationMessage expectedMessage;
    private boolean ready = false;

    /**
     * Constructor.
     * @param Client the client which is connected to the slave
     * @param SynchronizationMessage the message expected from the slave
     */
    public MasterWaitingThread(Client client, SynchronizationMessage expectedMessage) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null.");
        }
        this.client = client;
        this.expectedMessage = expectedMessage;
    }

    @Override
    public void run() {

        // Wait for the slave to send the expected message

        try {
            Object message = client.receiveObject();
            System.out.println("Received " + message + " from " + client.getAddress());
            if (message == this.expectedMessage) {
                this.ready = true;
            } else {
                throw new CommunicationException("Received an unexpected message instead of " + this.expectedMessage);
            }
        } catch (CommunicationException e) {
            System.err.println("Error in thread " + this.getId() + ": " + e.getMessage());
            System.exit(1);
        }

    }

    public boolean isReady() {
        return this.ready;
    }
    
}
