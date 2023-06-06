package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * SlaveServerThread.java
 * 
 * A Thread for a slave node to receive the HashMap from the map phase.
 */
public class SlaveServerThread extends Thread {

    private final int port ;
    private HashMap<String, Integer> data;

    /**
     * Constructor.
     * @param is the input stream of the socket
     */
    public SlaveServerThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("Thread " + this.getId() + " corresponds to port " + port + " !");

        Socket socketOfServer = null;
        ServerSocket listener = null;

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        ObjectInputStream is = null;
        // ObjectOutputStream os = null;
        
        try {
            System.out.println("Slave is waiting for incoming connection on port "+ port +"...");
            
            // Accept client connection request
            // Get new Socket at Server.    
            socketOfServer = listener.accept();
            System.out.println("Slave has accepted a client on port "+ port +" !");
            
            // Open input and output streams
            is = new ObjectInputStream(socketOfServer.getInputStream());
            // os = new ObjectOutputStream(socketOfServer.getOutputStream());
            
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        
        while (true) {
            try {
                Object received = is.readObject();
                System.out.println("Thread " + this.getId() + " received " + received);
                if (received.equals(SynchronizationMessage.END)) {
                    break;
                } else if (received instanceof HashMap) {
                    data = (HashMap<String, Integer>) received;
                    System.out.println("Thread " + this.getId() + " received " + data.size() + " elements.");
                    Utils.printHashmap(data);
                    break;
                } else {
                    System.err.println("Thread " + this.getId() + " received an unknown object: " + received);
                }
            } catch (Exception e) {
                System.err.println("Thread " + this.getId() + " error: " + e.getMessage());
                break;
            }
        }
        
        System.out.println("Thread " + this.getId() + " finished.");

        try {
            is.close();
            socketOfServer.close();
            listener.close();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * Get the HashMap.
     * 
     * This method should be called after the thread is finished !
     * @return the HashMap
     */
    public HashMap<String, Integer> getData() {
        return data;
    }
    
}
