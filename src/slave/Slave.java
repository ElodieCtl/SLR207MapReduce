package src.slave;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

import src.CommunicationException;
import src.Server;
import src.SynchronizationMessage;

/**
 * Slave.java
 * 
 * A slave node for the MapReduce framework.
 * 
 * <h2>Attribution of ports</h2>
 * 
 * The port 9999 + i is attributed to the slave i,
 * except for the id of this slave which corresponds to the port for the master.
 */
public class Slave {
    
    
    private static final int FIRST_PORT = 9999;


    private static final String SPLITFILE_PREFIX = "/cal/commoncrawl/CC-MAIN-20230320083513-20230320113513-0007" ;
    private static final String SPLITFILE_SUFFIX = ".warc.wet" ;

    // private static final String SPLITFILE_PREFIX = "/cal/exterieurs/echatelin-21/dataSLR207/ex1/split-" ;
    // private static final String SPLITFILE_SUFFIX = ".txt" ;

    private static final String[] MACHINE_NAMES = {"tp-3b07-13", "tp-3b07-14", "tp-3b07-15"} ;
    // private static final String MASTER_NAME = "tp-3b07-12" ;

    ///////////////////////////// MAIN /////////////////////////////

    public static void main(String[] args) {

        System.out.println("Slave program started.");

        // Get the id of the slave

        String hostname = null;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.err.println("Hostname can't be resolved");
            System.exit(1);
        }
        System.out.println("Slave on " + hostname);
        int id = -1;
        for (int i = 0; i < MACHINE_NAMES.length; i++) {
            if (hostname.equals(MACHINE_NAMES[i])) {
                id = i;
                break;
            }
        }
        if (id == -1) {
            System.err.println("Hostname " + hostname + " is not a slave.");
            System.exit(1);
        }
        System.out.println("Slave id = " + id);

        // launch the slave

        try {
            new Slave(id).run() ;
        } catch (CommunicationException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Slave program finished.");
    }

    ///////////////////////////// CONSTRUCTOR, RUN AND OTHERS /////////////////////////////

    private final int id ;
    private final Server serverForMaster ;

    public Slave(int id) {
        this.id = id ;
        this.serverForMaster = new Server(FIRST_PORT + this.id) ;
    }

    public void run() throws CommunicationException {
        System.out.println("Slave " + id + " started.");

        // Open connection to the master and wait for the start message
        
        serverForMaster.openConnection();
        Object message = serverForMaster.receiveObject() ;

        // Change the message to START if run on the simple example
        if (message != SynchronizationMessage.MASTER_AWAKE) {
            System.err.println("Slave " + id + " received " + message + " instead of START.") ;
            System.exit(1) ;
        }

        // Prepare to map
        String portion = getSplitString() ;
        nextStep(SynchronizationMessage.READY_TO_MAP, SynchronizationMessage.START) ;

        // Map
        HashMap<String, Integer> mapResult = map(portion) ;

        // Shuffle
        HashMap<String, Integer>[] shuffleResult = shuffle(mapResult) ;

        // Reduce
        HashMap<String, Integer> reduceResult = reduce(shuffleResult) ;
        // System.out.println("Slave " + id + " reduceResult : " + reduceResult) ;
    }

    /**
     * Read a file and return its content as a String
     * @param filename the name (whole path) of the file to read
     * @return the content of the file as a String
     */
    public static String readFile(String filename) {
        // For better performance, we use StringBuilder instead of String
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n") ;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Retrieve the String to process for this slave, from the split file
     * @return the String to map for this slave
     */
    public String getSplitString() {
        return readFile(SPLITFILE_PREFIX + id + SPLITFILE_SUFFIX);
    }

    
    /**
     * Computes the index of the machine to attribute a word to
     * (hashcode of the word modulo the number of machines)
     * @param key the word to attribute to a machine
     * @return the index of the machine to attribute the word to
     */
    private static final int attributeMachine(String key) {
        return Math.abs(key.hashCode()) % MACHINE_NAMES.length;
    }

    ///////////////////////////// STEPS OF MAPREDUCE /////////////////////////////

    /**
     * <strong>Blocking</strong> method to send the message to master to signal the end of the step
     * and wait for the message to continue
     * @param endMessage the message to send to master to signal the end of the step
     * @param continueMessage the message to receive from master to signal the continuation of the step
     * @throws CommunicationException
     */
    private void nextStep(SynchronizationMessage endMessage, SynchronizationMessage continueMessage) throws CommunicationException {
        serverForMaster.sendObject(endMessage);
        Object message = serverForMaster.receiveObject();
        if (message != continueMessage) {
            System.err.println("Error : expected "+continueMessage+" message, received " + message);
            System.exit(1);
        }
    }

    /**
     * Map function : split a portion of text into words and count the number of occurences of each word
     * @param portion the portion of text to split
     * @return a HashMap containing the words and their number of occurences
     */
    public static HashMap<String, Integer> map(String portion) {
        String[] words = portion.split("[\\p{Punct}|\\s]+");
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (String word : words) {
            Integer value = result.getOrDefault(word, 0);
            result.put(word, value + 1);
        }
        return result;
    }

    /**
     * Prepare the packets to send to each machine
     * @param pairs the pairs to send
     * @return an array of list of hashmaps to send to the machines
     */
    public static HashMap<String, Integer>[] prepareForShuffle(HashMap<String, Integer> pairs) {
        HashMap<String, Integer>[] result = new HashMap[MACHINE_NAMES.length];
        for (int i = 0; i < MACHINE_NAMES.length; i++) {
            result[i] = new HashMap<String, Integer>();
        }
        for (Entry<String,Integer> pair : pairs.entrySet()) {
            int machineIndex = attributeMachine(pair.getKey());
            result[machineIndex].put(pair.getKey(), pair.getValue());
        }
        return result;
    }

    /**
     * Send the packets to each machine to reduce them
     * @param packets the packets to shuffle
     */
    public HashMap<String,Integer>[] shuffle(HashMap<String, Integer> pairs) throws CommunicationException{

        // listen to other slaves to receive the packets to reduce

        SlaveServerThread[] serverThreads = new SlaveServerThread[MACHINE_NAMES.length];
        for (int i = 0  ; i < MACHINE_NAMES.length ; i++) {
            if (i != this.id) {
                serverThreads[i] = new SlaveServerThread(FIRST_PORT + i);
                serverThreads[i].start();
            }
        }

        // Synchronization : wait for the master to collect
        // all READY_TO_SHUFFLE messages to launch the shuffle phase

        nextStep(SynchronizationMessage.READY_TO_SHUFFLE, SynchronizationMessage.SHUFFLE);

        HashMap<String, Integer>[] packets = prepareForShuffle(pairs) ;

        // Send the packets to the machines using client threads

        Thread[] threads = new Thread[MACHINE_NAMES.length];
        for (int i = 0; i < MACHINE_NAMES.length; i++) {
            if (i != this.id) {
                threads[i] = new SlaveClientThread(MACHINE_NAMES[i], FIRST_PORT+this.id, packets[i]);
                threads[i].start();
            }
        }

        // Wait for all the client threads to finish before continuing
        
        for (Thread t : threads) {
            try {
                if (t != null) t.join();
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for a client thread to join");
                e.printStackTrace();
                System.exit(0);
            }
        }
        System.out.println("All client threads finished");

        // Wait for all the server threads to finish before continuing
        // and gather the packets received from the other machines

        HashMap<String,Integer>[] result = new HashMap[MACHINE_NAMES.length];

        for (int i = 0; i < MACHINE_NAMES.length; i++) {
            try {
                if (i == this.id) {
                    result[i] = packets[i];
                } else {
                    serverThreads[i].join();
                    result[i] = serverThreads[i].getData();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for a server thread to join");
                e.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("All server threads finished");

        return result;
    }

    /**
     * Reduce the packets received from the other machines
     * @param shuffledMaps the packets to reduce
     * @return Hashmap of the words with their occurence according to all the packets received
     */
    public HashMap<String,Integer> reduce(HashMap<String,Integer>[] shuffledMaps) throws CommunicationException{

        nextStep(SynchronizationMessage.READY_TO_REDUCE, SynchronizationMessage.REDUCE);

        HashMap<String,Integer> result = new HashMap<String,Integer>();
        for (HashMap<String,Integer> map : shuffledMaps) {
            for (Entry<String,Integer> pair : map.entrySet()) {
                if (pair.getValue() == null) {
                    System.out.println("Null value for "+pair.getKey());
                    continue;
                } 
                Integer value = result.getOrDefault(pair.getKey(), 0);
                result.put(pair.getKey(), value + pair.getValue());
            }
        }

        serverForMaster.sendObject(SynchronizationMessage.REDUCE_END);
        
        return result;
    }
}
