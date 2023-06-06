package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

    public void closeConnection() {
        try {
            if (listener != null) {
                listener.close();
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

    public int getPort() {
        return port;
    }
    
}
