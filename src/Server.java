package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server.java
 * 
 * A simple server that can accept one connection from a client and exchange Serializable objects.
 */
public class Server {

    private final int port;

    private ServerSocket listener;
    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    /**
     * Constructor.
     * @param port the port of the server
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * <strong>Blocking</strong> method to create a ServerSocket and wait for a connection.
     * Open input and output streams for serializable objects, when a client is accepted.
     */
    public void openConnection() {
        listener = null;
        socket = null;
        os = null;
        is = null;

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Couldn't listen on port " + port);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Server is waiting for incoming connection on port "+ port +"...");
        
        try {            
            // Accept client connection request  
            socket = listener.accept();
            System.out.println("Server has accepted a client on port "+ port +" !");
            
            // Open input and output streams
            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
            
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
     * Close all the sockets and streams.
     */
    public void closeConnection() {
        try {
            if (listener != null) {
                listener.close();
            }
        } catch (IOException e) {
            System.err.println("Couldn't close socket.");
        }
    }

    /**
     * Send a serializable object to the client.
     * @param object the object to send
     */
    public void sendObject(Serializable object) {
        try {
            os.writeObject(object);
            os.flush();
        } catch (IOException e) {
            System.err.println("Couldn't send object + " + object.toString());
        }
    }

    /**
     * <strong>Blocking</strong> method to receive a serializable object from the client.
     * @return the object received
     */
    public Object receiveObject() {
        try {
            return is.readObject();
        } catch (IOException e) {
            System.err.println("Couldn't receive object.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Couldn't receive object.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the port of the server.
     * @return the port as an int
     */
    public int getPort() {
        return port;
    }
    
}
