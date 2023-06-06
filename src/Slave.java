package src;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

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
    
    private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    private static final String LOGIN = "echatelin-21" ;
    private static final int PORT = 9999;
    private static final String STORAGE_FILENAME = "storage.txt" ;
    private static final String SPLIT_DIRECTORY = "../data/" ;
    private static final String[] SPLIT_PORTIONS = {"Deer Beer River", "Car Car River", "Deer Car Beer"} ;
    private static final String[] MACHINE_NAMES = {"tp-3b07-13", "tp-3b07-14", "tp-3b07-15"} ;
    private static final String MASTER_NAME = "tp-3b07-16" ;

    ///////////////////////////// MAIN /////////////////////////////

    public static void main(String[] args) {

        System.out.println("Slave program started.");

        String hostname = null;

        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {
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

        // if (args.length != 1) {
        //     help();
        // }
        // try {
        //     int id = Integer.parseInt(args[0]);
        //     if (id < 0 || id > 2) {
        //         help();
        //     }
        // } catch (NumberFormatException e) {
        //     help();
        // }

        new Slave(id).run() ;

        System.out.println("Slave program finished.");
    }

    public static void help() {
        System.out.println("Usage: java Slave <id>");
        System.out.println("The id must be an integer between 0 and 2.");
        System.exit(1);
    }

    ///////////////////////////// CONSTRUCTOR AND RUN /////////////////////////////

    private final int id ;

    public Slave(int id) {
        this.id = id ;
    }

    public void run() {
        System.out.println("Slave " + id + " started.");

        // Open connection to the master and wait for the start message
        openConnection(id) ;
        SynchronizationMessage message = waitForMaster() ;
        if (message != SynchronizationMessage.START) {
            System.err.println("Slave " + id + " received " + message + " instead of START.") ;
            System.exit(1) ;
        }

        // Map
        String portion = SPLIT_PORTIONS[id] ;
        HashMap<String, Integer> mapResult = map(portion) ;

        // Shuffle
        HashMap<String, Integer>[] toShuffle = prepareForShuffle(mapResult) ;
        System.out.println("Slave " + id + " toShuffle:") ;
        for (int i = 0; i < toShuffle.length; i++) {
            System.out.println("Slave " + id + " toShuffle[" + i + "] = " + toShuffle[i]) ;
        }
        HashMap<String, Integer>[] shuffleResult = shuffle(toShuffle) ;

        // Reduce
        HashMap<String, Integer> reduceResult = reduce(shuffleResult) ;
        System.out.println("Slave " + id + " reduceResult : " + reduceResult) ;
    }

    public String getSplitFilename(int id) {
        return SPLIT_DIRECTORY + "split-" + id + ".txt";
    }
    
    public static File openFile(String filename) {
        File file = new File(TEMP_DIRECTORY, LOGIN + "/" + filename);
        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return file;
    }

    ///////////////////////////// SERVER /////////////////////////////
    
    private ObjectInputStream[] is = new ObjectInputStream[MACHINE_NAMES.length];
    private ObjectOutputStream[] os = new ObjectOutputStream[MACHINE_NAMES.length];
    private Socket[] socketOfServer = new Socket[MACHINE_NAMES.length];

    /**
     * Opens a server socket on port 9999 + machineIndex
     * @param machineIndex the index of the machine to open the connection to
     */
    public void openConnection(int machineIndex) {
        ServerSocket listener = null;
        int port = PORT + machineIndex ;
        
        // Try to open a server socket on port 9999
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)
        
        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        
        try {
            System.out.println("Slave "+ this.id + " is waiting for incoming connection on port "+ port +"...");
            
            // Accept client connection request
            // Get new Socket at Server.    
            socketOfServer[machineIndex] = listener.accept();
            System.out.println("Slave "+ this.id + " has accepted a client on port "+ port +" !");
            
            // Open input and output streams
            is[machineIndex] = new ObjectInputStream(socketOfServer[machineIndex].getInputStream());
            os[machineIndex] = new ObjectOutputStream(socketOfServer[machineIndex].getOutputStream());
            
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * Close the connection to the clients (input and output streams and socket)
     * on port 9999 + machineIndex
     * @param machineIndex the index of the machine to close the connection to
     */
    public void closeConnection(int machineIndex) {
        try {
            if (is != null) is[machineIndex].close();
            if (os != null) os[machineIndex].close();
            if (socketOfServer != null) socketOfServer[machineIndex].close();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void sendToMaster(SynchronizationMessage message) {
        try {
            os[this.id].writeObject(message);
            os[this.id].flush();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public SynchronizationMessage waitForMaster() {
        try {
            return (SynchronizationMessage) is[this.id].readObject();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return null;
    }

    public void sendToMachine(int index, Serializable object) {
        try {
            os[index].writeObject(object);
            os[index].flush();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    ///////////////////////////// STEPS OF MAPREDUCE /////////////////////////////
    
    /* public static void receiveSplit() {
        ServerSocket listener = null;
        BufferedReader is;
        BufferedWriter os;
        Socket socketOfServer = null;
        
        File storage = openFile(STORAGE_FILENAME);
        
        // Try to open a server socket on port 9999
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)
        
        
        try {
            listener = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        
        try {
            System.out.println("Slave is waiting for the files to map...");
            
            // Accept client connection request
            // Get new Socket at Server.    
            socketOfServer = listener.accept();
            System.out.println("Have accepted a client!");
            
            // Open input and output streams
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new FileWriter(storage));
            
            
            char[] buffer = new char[1024];
            int count ;
            // Read data to the server (sent from client).
            while((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
                os.flush();
            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        System.out.println("Server stopped!");
    } */

    /**
     * Map function : split a portion of text into words and count the number of occurences of each word
     * @param portion the portion of text to split
     * @return a HashMap containing the words and their number of occurences
     */
    public static HashMap<String, Integer> map(String portion) {
        String[] words = portion.split(" ");
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (String word : words) {
            Integer value = result.getOrDefault(word, 0);
            result.put(word, value + 1);
        }
        return result;
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
    public HashMap<String,Integer>[] shuffle(HashMap<String, Integer>[] packets) {

        // listen to other slaves to receive the packets to reduce

        SlaveServerThread[] serverThreads = new SlaveServerThread[MACHINE_NAMES.length];
        for (int i = 0  ; i < MACHINE_NAMES.length ; i++) {
            if (i != this.id) {
                serverThreads[i] = new SlaveServerThread(PORT + i);
                serverThreads[i].start();
            }
        }

        // Synchronization : wait for the master to collect
        // all READY_TO_SHUFFLE messages to launch the shuffle phase

        sendToMaster(SynchronizationMessage.READY_TO_SHUFFLE);
        SynchronizationMessage message = waitForMaster();
        if (message != SynchronizationMessage.SHUFFLE) {
            System.err.println("Error : expected SHUFFLE message, received " + message);
            System.exit(1);
        }

        // Send the packets to the machines using client threads

        Thread[] threads = new Thread[MACHINE_NAMES.length];
        for (int i = 0; i < MACHINE_NAMES.length; i++) {
            if (i != this.id) {
                threads[i] = new SlaveClientThread(getHostname(i), PORT+this.id, packets[i]);
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
                System.exit(1);
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

    private String getHostname(int i) {
        if (i == this.id) {
            return MASTER_NAME;
        }
        return MACHINE_NAMES[i];
    }

    /**
     * Reduce the packets received from the other machines
     * @param shuffledMaps the packets to reduce
     * @return Hashmap of the words with their occurence according to all the packets received
     */
    public static HashMap<String,Integer> reduce(HashMap<String,Integer>[] shuffledMaps) {
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
}
