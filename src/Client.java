package src;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private final String hostname;
    private final int port;

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    /**
     * Constructor.
     * @param hostname the hostname of the server
     * @param port the port of the server
     */
    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Connect to the server and open input and output streams
     * for serializable objects, when a server is connected.
     */
    public void openConnection() throws CommunicationException {
        socket = null;
        os = null;
        is = null;

        System.out.println("Connecting to " + hostname + ":" + port + "...");

        try {
           
            // Send a request to connect to the server is listening
            // on machine 'localhost' port <port>.
            socket = new Socket(hostname, port);

            System.out.println("Connected to " + hostname + ":" + port);

            // Create output and input streams at the client
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());


        } catch (UnknownHostException e) {
            handleError("Don't know about host " + hostname, e);
        } catch (IOException e) {
            handleError("Couldn't get I/O for the connection to "+ getAddress(), e);
        } catch (Exception e) {
            handleError("Unknown error", e);
        }
    }

    /**
     * Close the connection to the server.
     */
    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Couldn't close socket.");
            e.printStackTrace();
        }
    }

    /**
     * Send a serializable object to the server.
     * @param Serializable object to send
     */
    public void sendObject(Serializable object) throws CommunicationException {
        // System.out.println("Sending " + object.toString());
        try {
            os.writeObject(object);
            os.flush();
        } catch (IOException e) {
            handleError("Couldn't send object + " + object.toString(), e);
        }
    }

    /**
     * <strong>Blocking</strong> method to receive a serializable object from the server.
     * @return Serializable object received
     */
    public Object receiveObject() throws CommunicationException {
        try {
            return is.readObject();
        } catch (IOException e) {
            handleError("Couldn't receive object.", e);
        } catch (ClassNotFoundException e) {
            handleError("Couldn't match received object to a class.", e);
        }
        return null;
    }

    /**
     * Get the address of the server for debug purpose.
     * @return the address of the server with the port
     */
    public String getAddress() {
        return hostname + ":" + port;
    }

    private void handleError(String message, Exception e) throws CommunicationException {
        System.err.println(message);
        e.printStackTrace();
        this.closeConnection();
        throw new CommunicationException(message);
    }
    
}
