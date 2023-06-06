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
    public void openConnection() throws CommunicationException {
        listener = null;
        socket = null;
        os = null;
        is = null;

        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            handleError("Couldn't listen on port " + port, e);
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
            handleError("IOException when accepting the client or open streams", e);
        }
    }

    /**
     * Close all the sockets and streams.
     */
    public void closeConnection() throws CommunicationException {
        try {
            if (listener != null) {
                listener.close();
            }
        } catch (IOException e) {
            handleError("Couldn't close socket.", e);
        }
    }

    /**
     * Send a serializable object to the client.
     * @param object the object to send
     */
    public void sendObject(Serializable object) throws CommunicationException {
        try {
            os.writeObject(object);
            os.flush();
        } catch (IOException e) {
            handleError("Couldn't send object + " + object.toString(), e);
        }
    }

    /**
     * <strong>Blocking</strong> method to receive a serializable object from the client.
     * @return the object received
     */
    public Object receiveObject() throws CommunicationException {
        try {
            return is.readObject();
        } catch (IOException e) {
            handleError("Couldn't receive object.", e);
        } catch (ClassNotFoundException e) {
            handleError("The received object doesn't match any class.", e);
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

    private void handleError(String message, Exception e) throws CommunicationException {
        System.err.println(message);
        e.printStackTrace();
        this.closeConnection();
        throw new CommunicationException(message);
    }
    
}
