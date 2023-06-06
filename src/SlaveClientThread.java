package src;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * SlaveClientThread.java
 * 
 * A Thread for a slave node to send data.
 */
public class SlaveClientThread extends Thread {
    
    private final String serverHost;
    private final int serverPort;
    private final Serializable object;
    
    /**
     * Constructor.
     * @param serverHost the hostname of the server
     * @param serverPort the port of the server
     * @param object Serializable object to send
     */
    public SlaveClientThread(String serverHost, int serverPort, Serializable object) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.object = object;
    }

    private void printError(String message) {
        System.err.println("["+ this.getId()+"] " + message);
    }
    
    @Override
    public void run() {
        System.out.println("Thread " + this.getId() + " started.");
        
        Socket socketOfClient = null;
        ObjectOutputStream os = null;
        
        try {
            
            // Send a request to connect to the server is listening
            // on machine 'localhost' port 9999.
            socketOfClient = new Socket(serverHost, serverPort);
            System.out.println("Thread " + this.getId() + " connected to " + serverHost + ":" + serverPort);
            
            // Create output stream at the client (to send data to the server)
            os = new ObjectOutputStream(socketOfClient.getOutputStream());
            
        } catch (UnknownHostException e) {
            this.printError("Don't know about host " + serverHost);
            return;
        } catch (IOException e) {
            this.printError("Couldn't get I/O for the connection to " + serverHost + ":" + serverPort);
            return;
        }
        
        try {
            System.out.println("Thread " + this.getId() + " is sending data " + object);
            // Write data to the output stream of the Client Socket.
            os.writeObject(object);
            // Flush data.
            os.flush();
            
        } catch (UnknownHostException e) {
            this.printError("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            this.printError("IOException:  " + e);
        } finally {
            try {
                if (socketOfClient != null) {
                    socketOfClient.close();
                }
            } catch (IOException e) {
                this.printError("IOException:  " + e);
            }
        }
    }
    
}
