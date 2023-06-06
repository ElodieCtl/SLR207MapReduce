package src;

import java.io.* ;
import java.net.Socket;
import java.net.UnknownHostException;

public class Master {

    private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    private static final String LOGIN = "echatelin-21" ;
    private static final int PORT = 9999;
    private static final String[] HOSTNAMES = {"tp-3b07-13.enst.fr", "tp-3b07-14.enst.fr", "tp-3b07-15.enst.fr"} ;
    private final Client[] clients = new Client[HOSTNAMES.length];

    public static void main(String[] args) {
        System.out.println("Master program started !");
        new Master().run();
    }

    public void run() {
        System.out.println("Master awake!");

        // Connect to all slaves

        for (int i = 0; i < HOSTNAMES.length; i++) {
            clients[i] = new Client(HOSTNAMES[i], PORT+i) ;
            clients[i].openConnection();
        }
        System.out.println("Master connected to all slaves!");

        // Send START to all slaves and wait for READY_TO_SHUFFLE

        MasterWaitingThread[] threads = new MasterWaitingThread[HOSTNAMES.length];
        for (int i = 0; i < HOSTNAMES.length; i++) {
            clients[i].sendObject(SynchronizationMessage.START);
            threads[i] = new MasterWaitingThread(clients[i]);
            threads[i].start();
        }
        System.out.println("Master sent START to all slaves and waits for them to be ready to shuffle !");

        // Wait for all slaves to be ready to shuffle and then send SHUFFLE

        try {
            for (Thread t : threads) {
                t.join();
                if (!((MasterWaitingThread)t).isReadyToShuffle()) {
                    System.err.println("A slave is not ready to shuffle !");
                    return;
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Master interrupted while waiting for slaves to be ready to shuffle !");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Master received READY_TO_SHUFFLE from all slaves !");

        for (Client client : clients) {
            client.sendObject(SynchronizationMessage.SHUFFLE);
        }

    }

    /*
     * Create a directory in the temporary directory of the system to store the splited files
     */
    public static void prepareSplit() {
        File newDirectory = new File(TEMP_DIRECTORY, LOGIN + "/splits");
        if (!newDirectory.exists()) {
            newDirectory.mkdirs();
        }
    }

    /*
     * Send the files to all servers to perform shuffle
     */
    public static void sendSplit() {
        Thread[] threads = new Thread[HOSTNAMES.length];
        for (int i = 0; i < HOSTNAMES.length; i++) {
            File file = new File(TEMP_DIRECTORY, LOGIN + "/splits/" + i + ".txt");
            threads[i] = new MapThread(HOSTNAMES[i], file);
            threads[i].start();
        } ;
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class MapThread extends Thread {
        private final String hostname ;
        private final File file ;

        public MapThread(String hostname, File file) {
            this.hostname = hostname ;
            this.file = file ;
        }

        public void run() {
            sendFile(hostname, file);
        }
    }


    /*
     * Send a file through a socket (client side).
     */
    public static void sendFile(String hostname, File file) {
        
        Socket socket = null;
        BufferedWriter os = null;
        BufferedReader fileStream = null;

       try {
           
           // Send a request to connect to the server is listening
           // on machine 'localhost' port 9999.
           socket = new Socket(hostname, PORT);

           // Create output stream at the client (to send data to the server)
           os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

       } catch (UnknownHostException e) {
           System.err.println("Don't know about host " + hostname);
           return;
       } catch (IOException e) {
           System.err.println("Couldn't get I/O for the connection to " + hostname);
           return;
       }

        char[] chars = new char[16 * 1024];

       try {

            // Read the file and copy the content into the socket
            fileStream = new BufferedReader(new FileReader(file));
            int count;
            while ((count = fileStream.read(chars)) > 0) {
                os.write(chars, 0, count);
                os.flush();
            }

            os.close();
            fileStream.close();
            socket.close();
       } catch (FileNotFoundException e) {
              System.err.println("File not found: " + e);
       } catch (UnknownHostException e) {
           System.err.println("Trying to connect to unknown host: " + e);
       } catch (IOException e) {
           System.err.println("IOException:  " + e);
       }
   }

    
}
