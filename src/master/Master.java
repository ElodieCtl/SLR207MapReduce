package src.master;

import src.Client;
import src.CommunicationException;
import src.SynchronizationMessage;

public class Master {

    private static final int FIRST_PORT = 9999;
    private static final String[] HOSTNAMES = {"tp-3b07-13", "tp-3b07-14", "tp-3b07-15"} ;


    private final Client[] clients = new Client[HOSTNAMES.length];
    private final long[] chronos = new long[4];

    public static void main(String[] args) {
        System.out.println("Master program started !");
        try {
            new Master().run();
        } catch (CommunicationException e) {
            System.exit(1);
        }
    }

    public void run() throws CommunicationException {
        System.out.println("Master awake!");

        // Connect to all slaves

        for (int i = 0; i < HOSTNAMES.length; i++) {
            clients[i] = new Client(HOSTNAMES[i], FIRST_PORT+i) ;
            clients[i].openConnection();
        }
        System.out.println("Master connected to all slaves!");

        //////////////////// COUNTING WORDS WITH MAPREDUCE ////////////////////

        // Send START to all slaves and wait for READY_TO_SHUFFLE
        completeStep(0, SynchronizationMessage.MASTER_AWAKE, SynchronizationMessage.READY_TO_MAP);

        // Send START to all slaves and wait for READY_TO_SHUFFLE
        completeStep(1, SynchronizationMessage.START, SynchronizationMessage.READY_TO_SHUFFLE);

        // Send SHUFFLE to all slaves and wait for READY_TO_REDUCE
        completeStep(2, SynchronizationMessage.SHUFFLE, SynchronizationMessage.READY_TO_REDUCE);

        // Send REDUCE to all slaves and wait for the REDUCE_END
        completeStep(3, SynchronizationMessage.REDUCE, SynchronizationMessage.REDUCE_END);

        // Print the duration of each step
        printChronos();

    }

    /**
     * Send a message to all slaves and wait for a response and store the duration of the step.
     * @param stepIndex the index of the step in the chronos array
     * @param launchMessage the message to send to all slaves
     * @param response the message expected from all slaves
     * @throws CommunicationException if an error occurs during the communication
     */
    private void completeStep(int stepIndex, SynchronizationMessage launchMessage, SynchronizationMessage response)
        throws CommunicationException {
            
        MasterWaitingThread[] threads = new MasterWaitingThread[HOSTNAMES.length];
        long start = System.currentTimeMillis();
        for (int i = 0; i < HOSTNAMES.length; i++) {
            clients[i].sendObject(launchMessage);
            threads[i] = new MasterWaitingThread(clients[i], response);
            threads[i].start();
        }
        System.out.println("Master sent "+launchMessage+" to all slaves and waits for them to be ready !");
        try {
            for (MasterWaitingThread t : threads) {
                t.join();
                if (!t.isReady()) {
                    System.err.println("A slave is not ready !");
                    System.exit(1);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Master interrupted while waiting for slaves to be ready !");
            e.printStackTrace();
            System.exit(1);
        }
        long end = System.currentTimeMillis();
        chronos[stepIndex] = end - start;
        System.out.println("Master received "+response+" from all slaves !");
    }

    private void printChronos() {
        System.out.println("Load the splits : " + chronos[0] + "ms");
        System.out.println("Map : " + chronos[1] + "ms");
        System.out.println("Shuffle : " + chronos[2] + "ms");
        System.out.println("Reduce : " + chronos[3] + "ms");
    }
    
}
