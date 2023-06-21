package src.master;

import src.Client;
import src.CommunicationException;
import src.Range;
import src.SynchronizationMessage;
import src.Utils;

public class Master {

    private static final int FIRST_PORT = 9999;
    private final String[] HOSTNAMES ;
    private final int NB_SLAVES ;


    private final Client[] clients ;
    private final long[] chronos = new long[7];

    public Master(String[] hostnames, int nbSlaves) {
        HOSTNAMES = hostnames;
        NB_SLAVES = nbSlaves;
        clients = new Client[NB_SLAVES];
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java Master <filename> <NB_SLAVES>");
            System.exit(1);
        }
        String filename = args[0];
        int nbSlaves = 0;
        try {
            nbSlaves = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Usage: java Slave <filename> <NB_SLAVES>");
            System.exit(1);
        }
        String[] hostnames = Utils.readComputersFromFile(filename, nbSlaves);

        System.out.println("Master program started !");
        try {
            new Master(hostnames, nbSlaves).run();
        } catch (CommunicationException e) {
            System.exit(1);
        }
    }

    public void run() throws CommunicationException {
        System.out.println("Master awake!");

        // Connect to all slaves

        for (int i = 0; i < NB_SLAVES; i++) {
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

        // Send MAP to all slaves and wait for the READY_TO_COORDINATE
        completeStep(4, SynchronizationMessage.MAP, SynchronizationMessage.READY_TO_COORDINATE);

        // Send COORDINATE to all slaves and wait for the READY_TO_REDUCE_2
        completeShuffle2();

        // Send REDUCE_2 to all slaves and wait for the END
        completeStep(6, SynchronizationMessage.REDUCE_2, SynchronizationMessage.END);

        // Print the duration of each step
        printChronos();

    }

    private void completeShuffle2() throws CommunicationException {

        MasterCollectingRangesThread[] threads = new MasterCollectingRangesThread[NB_SLAVES];

        long start = System.currentTimeMillis();

        // send COORDINATE to all slaves and wait for the ranges

        for (int i = 0; i < NB_SLAVES; i++) {
            clients[i].sendObject(SynchronizationMessage.COORDINATE);
            threads[i] = new MasterCollectingRangesThread(clients[i]);
            threads[i].start();
        }
        System.out.println("Master sent "+SynchronizationMessage.COORDINATE+
            " to all slaves and waits for them to send the ranges !");

        Range[] ranges = new Range[NB_SLAVES];
        try {
            for (int i = 0; i < NB_SLAVES; i++) {
                threads[i].join();
                ranges[i] = threads[i].getRange();
            }
        } catch (InterruptedException e) {
            System.err.println("Master interrupted while waiting for slaves to be ready !");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Master received the ranges from all slaves : ");
        Utils.prettyPrintTable(ranges);

        // merge the ranges and send the global range to all slaves

        Range globalRange = Range.mergeRanges(ranges);
        System.out.println("Master merged the ranges into a global range : " + globalRange);

        MasterWaitingThread[] waitingThreads = new MasterWaitingThread[NB_SLAVES];
        for (int i = 0; i < NB_SLAVES; i++) {
            clients[i].sendObject(globalRange);
            waitingThreads[i] = new MasterWaitingThread(clients[i], SynchronizationMessage.READY_TO_REDUCE_2);
            waitingThreads[i].start();
        }
        System.out.println("Master sent the global range to all slaves and waits for them to be ready !");
        try {
            for (MasterWaitingThread t : waitingThreads) {
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
        System.out.println("Master received "+SynchronizationMessage.READY_TO_REDUCE_2+" from all slaves !");

        long end = System.currentTimeMillis();

        chronos[5] = end - start;

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
            
        MasterWaitingThread[] threads = new MasterWaitingThread[NB_SLAVES];
        long start = System.currentTimeMillis();
        for (int i = 0; i < NB_SLAVES; i++) {
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
        System.out.println("/// MAPREDUCE TO COUNT ///");
        System.out.println("Map : " + chronos[1] + "ms");
        System.out.println("Shuffle : " + chronos[2] + "ms");
        System.out.println("Reduce : " + chronos[3] + "ms");
        System.out.println("/// MAPREDUCE TO SORT ///");
        System.out.println("Map : " + chronos[4] + "ms");
        System.out.println("Shuffle : " + chronos[5] + "ms");
        System.out.println("Reduce : " + chronos[6] + "ms");
    }
    
}
