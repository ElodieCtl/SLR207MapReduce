package src;

import java.io.*;

import java.net.Socket;
import java.net.UnknownHostException;

import src.Client.ClientException;

/**
 * SlaveClientThread.java
 * 
 * A Thread for a slave node to send data.
 */
public class SlaveClientThread extends Thread {

    private final Client client;
    
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
        this.client = new Client(serverHost, serverPort);
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.object = object;
        System.out.println("Client thread " + this.getId() + " created for " + serverHost + ":" + serverPort);
        System.out.println("Client thread " + this.getId() + " created for " + object);
    }
    
    @Override
    public void run() {

        /*System.out.println("Thread " + this.getId() + " started.");

        try {
            this.client.openConnection();
            System.out.println("Thread " + this.getId() + " connected to " + this.client.getAddress());
            this.client.sendObject(this.object);
            System.out.println("Thread " + this.getId() + " sent data " + this.object);
            this.client.closeConnection();
        } catch (ClientException e) {
            System.err.println("Error in thread " + this.getId() + ": " + e.getMessage());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.exit(0);
        } */
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
            this.printError("Don't know about host " + serverHost, e);
            return;
        } catch (IOException e) {
            this.printError("Couldn't get I/O for the connection to " + serverHost + ":" + serverPort, e);
            return;
        }
        
        try {
            System.out.println("Thread " + this.getId() + " is sending data " + object);
            // Write data to the output stream of the Client Socket.
            os.writeObject(object);
            // Flush data.
            os.flush();
            
        } catch (UnknownHostException e) {
            this.printError("Trying to connect to unknown host", e);
        } catch (IOException e) {
            this.printError("IOException while sending data", e);
        } finally {
            try {
                if (socketOfClient != null) {
                    socketOfClient.close();
                }
            } catch (IOException e) {
                this.printError("IOException while closing the socket", e);
            }
        }
    }

    /**
     * Print an error message.
     * @param message the error message
     * @param e the exception
     */
    private void printError(String message, Exception e) {
        System.err.println("Thread " + this.getId() + ": " + message);
        e.printStackTrace();
    }
    
}
