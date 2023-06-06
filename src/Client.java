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

    public void openConnection() {
        socket = null;
        os = null;
        is = null;

       try {
           
            // Send a request to connect to the server is listening
            // on machine 'localhost' port <port>.
            socket = new Socket(hostname, port);

            // Create output stream at the client (to send data to the server)
            os = new ObjectOutputStream(socket.getOutputStream());

            // Create input stream at client (to receive data from the server).
            is = new ObjectInputStream(socket.getInputStream());


        } catch (UnknownHostException e) {
           System.err.println("Don't know about host " + hostname);
           e.printStackTrace();
           closeConnection();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostname);
            e.printStackTrace();
            closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Couldn't close socket.");
        }
    }

    public void sendObject(Serializable object) {
        try {
            os.writeObject(object);
            os.flush();
        } catch (IOException e) {
            System.err.println("Couldn't send object + " + object.toString());
        }
    }

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

    public String getAddress() {
        return hostname + ":" + port;
    }
    
}
