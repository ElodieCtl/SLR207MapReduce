package src.slave;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import src.CommunicationException;
import src.Range;
import src.Server;
import src.SynchronizationMessage;
import src.Utils;

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
public class Slave extends Thread {
    
    
    private static final int FIRST_PORT = 9999;


    private static String SPLITFILE_PREFIX = "/cal/commoncrawl/CC-MAIN-20230320083513-20230320113513-000" ;
    // private static final String SPLITFILE_SUFFIX = ".warc.wet" ;
    private static final String SPLITFILE_SUFFIX = ".txt" ;

    // private static final String SPLITFILE_PREFIX = "/cal/exterieurs/echatelin-21/dataSLR207/ex1/split-" ;
    // private static final String SPLITFILE_SUFFIX = ".txt" ;

    private final String[] MACHINE_NAMES ;
    private final int NB_SLAVES ;
    private final int NB_SPLITS ;
    // private static final String MASTER_NAME = "tp-3b07-12" ;

    ///////////////////////////// MAIN /////////////////////////////

    /**
     * Starts a slave node.
     * 
     * <h2>Arguments</h2>
     * <ul>
     * <li>args[1] : filename where there is the list of available computers</li>
     * <li>args[0] : NB_SLAVES</li>
     * <li>args[2] : NB_SPLITS</li>
     * <li>args[3] : SPLITFILE_PREFIX</li>
     * </ul>
     * 
     * @param args the arguments : filename NB_SLAVES NB_SPLITS
     */
    public static void main(String[] args) {

        System.out.println("Slave program started.");
        // Utils.printMemorySizes();
        
        if (args.length != 4) {
            System.err.println("Usage: java Slave <NB_SLAVES> <filename> <NB_SPLITS> <SPLITFILE_PREFIX>");
            System.out.println("Arguments were :");
            Utils.prettyPrintTable(args);
            System.exit(1);
        }
        String filename = args[1];
        SPLITFILE_PREFIX = args[3] ;
        int nbSlaves = 0 ;
        int nbSplits = 0;
        try {
            nbSlaves = Integer.parseInt(args[0]);
            nbSplits = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Usage: java Slave <filename> <NB_SLAVES> <NB_SPLITS>");
            System.exit(1);
        }
        String[] machineNames = Utils.readComputersFromFile(filename, nbSlaves);

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
        for (int i = 0; i < nbSlaves; i++) {
            if (hostname.equals(machineNames[i])) {
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
            Slave s = new Slave(id, machineNames, nbSlaves, nbSplits) ;
            s.start() ;
            s.join() ;
            System.out.println("Slave " + id + " finished.");
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Slave program finished.");
    }

    ///////////////////////////// CONSTRUCTOR, RUN AND OTHERS /////////////////////////////

    private final int id ;
    private final Server serverForMaster ;

    public Slave(int id, String[] machineNames, int nbSlaves, int nbSplits) {
        this.id = id ;
        this.MACHINE_NAMES = machineNames ;
        this.NB_SLAVES = nbSlaves ;
        this.NB_SPLITS = nbSplits ;
        this.serverForMaster = new Server(FIRST_PORT + this.id) ;
    }

    @Override
    public void run() {
        System.out.println("Slave " + id + " started.");

        // Open connection to the master and wait for the start message

        try {
        
        serverForMaster.openConnection();
        Object message = serverForMaster.receiveObject() ;

        // Change the message to START if run on the simple example
        if (message != SynchronizationMessage.MASTER_AWAKE) {
            System.err.println("Slave " + id + " received " + message + " instead of START.") ;
            System.exit(1) ;
        }

        // Prepare to map
        // String[] portion = getSplitString() ;
        String portion = getSplitString() ;

        System.out.println("Slave " + id + " is ready to map.") ;
        nextStep(SynchronizationMessage.READY_TO_MAP, SynchronizationMessage.START) ;

        // Map
        HashMap<String, Integer> mapResult = map(portion) ;

        // Shuffle
        HashMap<String, Integer>[] shuffleResult = shuffle(mapResult) ;

        // Reduce
        HashMap<String, Integer> reduceResult = reduce(shuffleResult) ;

        // Map for sorting
        HashMap<Integer, List<String>> mapResult2 = map2(reduceResult) ;

        // Shuffle for sorting
        HashMap<Integer, List<String>>[] shuffleResult2 = shuffle2(mapResult2) ;

        // Reduce for sorting
        HashMap<Integer, List<String>> reduceResult2 = reduce2(shuffleResult2) ;
        sort(reduceResult2, "/tmp/echatelin-21/result-" + id + ".txt") ;
        // System.out.println("Slave " + id + " reduceResult : " + reduceResult) ;

        } catch (CommunicationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Read a file and return its content as a String
     * @param filename the name (whole path) of the file to read
     * @param sb the StringBuilder to use
     * @return the content of the file as a String
     */
    // public static List<String> readFile(String filename) {
    public static String readFile(String filename, StringBuilder sb) {
        // For better performance, we use StringBuilder instead of String
        BufferedReader br = null;
        // List<String> stringList = new ArrayList<String>() ;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                // stringList.add(line) ;
                sb.append(line).append("\n") ;
            }
            // stringList = br.lines().collect(Collectors.toList()) ;
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
        // return stringList ;
        return sb.toString() ;
    }

    /**
     * Retrieve the String to process for this slave, from the split file
     * @return the String to map for this slave
     */
    public String getSplitString() {
        StringBuilder sb = new StringBuilder() ;
        for (int i = 0; i < NB_SPLITS ; i++) {
            int id = i + NB_SPLITS * this.id ;
            if (id < 10) {
                readFile(SPLITFILE_PREFIX + "0" + id + SPLITFILE_SUFFIX, sb) ;
            } else {
                readFile(SPLITFILE_PREFIX + id + SPLITFILE_SUFFIX, sb) ;
            }
        }
        return sb.toString() ;
    }

    // Version with List<String>
    ///////////////////////////////////////////////////////////////////////////////////////
    // public String[] getSplitString() {
    //     List<String> result = new ArrayList<String>() ;
    //     for (int i = 0; i < NB_SPLITS ; i++) {
    //         int id = i + NB_SPLITS * this.id ;
    //         if (id < 10) {
    //             result.addAll(readFile(SPLITFILE_PREFIX + "0" + id + SPLITFILE_SUFFIX)) ;
    //         } else {
    //             result.addAll(readFile(SPLITFILE_PREFIX + id + SPLITFILE_SUFFIX)) ;
    //         }
    //     }
    //     return result.toArray(new String[0]) ;
    // }

    
    /**
     * Computes the index of the machine to attribute a word to
     * (hashcode of the word modulo the number of machines)
     * @param key the word to attribute to a machine
     * @return the index of the machine to attribute the word to
     */
    private final int attributeMachine(String key) {
        return Math.abs(key.hashCode()) % NB_SLAVES;
    }

    /**
     * Computes the index of the machine to attribute an occurence to
     * @param key the occurence to attribute to a machine
     * @param range the global range of the occurences
     * @return the index of the machine to attribute the occurence to
     */
    private final int attributeMachine2(int key, Range range) {
        return range.attributeTo(key, NB_SLAVES);
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

    public HashMap<String, Integer> map(String portion) {
        System.out.println("Slave " + id + " is mapping all portions.") ;
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        String[] words = portion.split("[\\p{Punct}|\\s]+");
        for (String word : words) {
            word = word.toLowerCase();
            Integer value = result.getOrDefault(word, 0);
            result.put(word, value + 1);
        }
        return result;
    }

    // Version with List<String>
    ///////////////////////////////////////////////////////////////////////////////////////
    // public HashMap<String, Integer> map(String[] portions) {
    //     System.out.println("Slave " + id + " is mapping all portions.") ;
    //     HashMap<String, Integer> result = new HashMap<String, Integer>();
    //     for (String portion : portions) {
    //         // HashMap<String, Integer> mapResult = map(portion) ;
    //         // result.putAll(mapResult) ;
    //         String[] words = portion.split("[\\p{Punct}|\\s]+");
    //         for (String word : words) {
    //             word = word.toLowerCase();
    //             Integer value = result.getOrDefault(word, 0);
    //             result.put(word, value + 1);
    //         }
    //     }
    //     return result;
    // }

    @SuppressWarnings("unchecked")
    /**
     * Prepare the packets to send to each machine
     * @param pairs the pairs to send
     * @return an array of list of hashmaps to send to the machines
     */
    public HashMap<String, Integer>[] prepareForShuffle(HashMap<String, Integer> pairs) {
        HashMap<String, Integer>[] result = new HashMap[NB_SLAVES];
        for (int i = 0; i < NB_SLAVES; i++) {
            result[i] = new HashMap<String, Integer>();
        }
        for (Entry<String,Integer> pair : pairs.entrySet()) {
            int machineIndex = attributeMachine(pair.getKey());
            result[machineIndex].put(pair.getKey(), pair.getValue());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    /**
     * Send the packets to each machine to reduce them
     * @param packets the packets to shuffle
     */
    public HashMap<String,Integer>[] shuffle(HashMap<String, Integer> pairs) throws CommunicationException{

        // listen to other slaves to receive the packets to reduce

        SlaveServerThread<String, Integer>[] serverThreads = new SlaveServerThread[NB_SLAVES];
        for (int i = 0  ; i < NB_SLAVES ; i++) {
            if (i != this.id) {
                serverThreads[i] = new SlaveServerThread<>(FIRST_PORT + i);
                serverThreads[i].start();
            }
        }

        // Synchronization : wait for the master to collect
        // all READY_TO_SHUFFLE messages to launch the shuffle phase

        nextStep(SynchronizationMessage.READY_TO_SHUFFLE, SynchronizationMessage.SHUFFLE);

        HashMap<String, Integer>[] packets = prepareForShuffle(pairs) ;

        // Send the packets to the machines using client threads

        Thread[] threads = new Thread[NB_SLAVES];
        for (int i = 0; i < NB_SLAVES; i++) {
            if (i != this.id) {
                threads[i] = new SlaveClientThread<String, Integer>(MACHINE_NAMES[i], FIRST_PORT+this.id, packets[i]);
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

        HashMap<String,Integer>[] result = new HashMap[NB_SLAVES];

        for (int i = 0; i < NB_SLAVES; i++) {
            try {
                if (i == this.id) {
                    result[i] = packets[i];
                } else {
                    serverThreads[i].join();
                    result[i] = (HashMap<String,Integer>) serverThreads[i].getData();
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
        
        return result;
    }

    ///////////////////////////// STEPS OF SECOND MAPREDUCE /////////////////////////////

    /**
     * Map function : reverse keys and values of the reduced result
     * @param map HashMap<String, Integer>, the reduced result
     * @return a HashMap containing occurences as keys and words as values
     * @throws CommunicationException
     */
    public HashMap<Integer, List<String>> map2(HashMap<String, Integer> map) throws CommunicationException {

        nextStep(SynchronizationMessage.REDUCE_END, SynchronizationMessage.MAP);

        HashMap<Integer, List<String>> result = new HashMap<Integer, List<String>>();
        for (Entry<String,Integer> pair : map.entrySet()) {
            List<String> value = result.getOrDefault(pair.getValue(), new ArrayList<String>());
            value.add(pair.getKey());
            result.put(pair.getValue(), value);
            // OR : result.computeIfAbsent(pair.getValue(), k -> new ArrayList<String>()).add(pair.getKey());
        }
        return result;
    }

    /**
     * <strong>Blocking</strong> method to send this slave's range to the master
     * and receive the global range
     * @param range the range of this slave
     * @return the global range
     * @throws CommunicationException
     */
    public Range coordinateRanges(Range range) throws CommunicationException {
        
        serverForMaster.sendObject(range);
        Object o = serverForMaster.receiveObject();
        if (o instanceof Range) {
            return (Range) o;
        } else {
            throw new CommunicationException("Unexpected object received from master : "+o);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Prepare the packets to send to each machine
     * @param pairs the pairs to send
     * @return an array of list of hashmaps to send to the machines
     */
    public HashMap<Integer, List<String>>[] prepareForShuffle2(HashMap<Integer, List<String>> pairs, Range range) {
        HashMap<Integer, List<String>>[] result = new HashMap[NB_SLAVES];
        for (int i = 0; i < NB_SLAVES; i++) {
            result[i] = new HashMap<Integer, List<String>>();
        }
        for (Entry<Integer,List<String>> pair : pairs.entrySet()) {
            int machineIndex = attributeMachine2(pair.getKey(), range);
            result[machineIndex].put(pair.getKey(), pair.getValue());
        }
        return result;
    }

    private Range globalRange;

    @SuppressWarnings("unchecked")
    /**
     * Send the packets to each machine to reduce them
     * @param packets the packets to shuffle
     */
    public HashMap<Integer,List<String>>[] shuffle2(HashMap<Integer, List<String>> pairs) throws CommunicationException{

        nextStep(SynchronizationMessage.READY_TO_COORDINATE, SynchronizationMessage.COORDINATE);

        // listen to other slaves to receive the packets to reduce

        SlaveServerThread<Integer,List<String>>[] serverThreads = new SlaveServerThread[NB_SLAVES];
        for (int i = 0  ; i < NB_SLAVES ; i++) {
            if (i != this.id) {
                serverThreads[i] = new SlaveServerThread<>(FIRST_PORT + i);
                serverThreads[i].start();
            }
        }

        // Synchronization : wait for the master to collect
        // all the ranges and send the global range

        Range myMapRange = Range.computeFromSet(pairs.keySet());
        System.out.println("My range : "+myMapRange);
        globalRange = coordinateRanges(myMapRange);

        HashMap<Integer, List<String>>[] packets = prepareForShuffle2(pairs, globalRange) ;

        // Send the packets to the machines using client threads

        Thread[] threads = new Thread[NB_SLAVES];
        for (int i = 0; i < NB_SLAVES; i++) {
            if (i != this.id) {
                threads[i] = new SlaveClientThread<Integer, List<String>>(MACHINE_NAMES[i], FIRST_PORT+this.id, packets[i]);
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

        HashMap<Integer,List<String>>[] result = new HashMap[NB_SLAVES];

        for (int i = 0; i < NB_SLAVES; i++) {
            try {
                if (i == this.id) {
                    result[i] = packets[i];
                } else {
                    serverThreads[i].join();
                    result[i] = (HashMap<Integer,List<String>>) serverThreads[i].getData();
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
    public HashMap<Integer, List<String>> reduce2(HashMap<Integer, List<String>>[] shuffledMaps) throws CommunicationException{

        nextStep(SynchronizationMessage.READY_TO_REDUCE_2, SynchronizationMessage.REDUCE_2);

        HashMap<Integer, List<String>> result = new HashMap<Integer, List<String>>();
        for (HashMap<Integer, List<String>> map : shuffledMaps) {
            for (Entry<Integer, List<String>> pair : map.entrySet()) {
                if (pair.getValue() == null) {
                    System.out.println("Null value for "+pair.getKey());
                    continue;
                } 
                List<String> value = result.getOrDefault(pair.getKey(), new ArrayList<String>());
                value.addAll(pair.getValue());
                result.put(pair.getKey(), value);
            }
        }
        
        return result;
    }

    public void sort(HashMap<Integer, List<String>> reducedResult, String filename) throws CommunicationException {

        // Open the file to write the result

        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter(filename));
            List<Integer> keys = reducedResult.keySet()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList()) ;

            for (Integer i : keys) {
                List<String> value = reducedResult.get(i);
                StringBuilder sb = new StringBuilder("" + i + " : [");
                for (String s : value) {
                    sb.append(s).append(", ");
                }
                sb.delete(sb.length()-2, sb.length());
                sb.append("]\n");
                br.write(sb.toString());
                br.flush();
            }

            // Range myRange = globalRange.computeMachineRange(this.id, NB_SLAVES);
            // System.out.println("Machine "+this.id+" has range "+myRange);
            // for (int i = myRange.start ; i <= myRange.end ; i++) {
            //     List<String> value = reducedResult.get(i);
            //     if (value != null && !value.isEmpty()) {
            //         StringBuilder sb = new StringBuilder("" + i + " : [");
            //         for (String s : value) {
            //             sb.append(s).append(", ");
            //         }
            //         sb.delete(sb.length()-2, sb.length());
            //         sb.append("]\n");
            //         br.write(sb.toString());
            //         br.flush();
            //     }
            // }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            serverForMaster.sendObject(SynchronizationMessage.END);
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

    }

}
