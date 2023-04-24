package sequential;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 * MyReader.java
 * A simple reader to read a file.
 * 
 * @author Elodie Chatelin
 */
public class MyReader {
    
    /**
     * Get a BufferedReader from a file.
     * 
     * @param filename The name of the file to read.
     * @return A BufferedReader to read the file.
     */
    public static BufferedReader getReader(String filename) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            System.exit(1);
        }
        return reader;
    }
}
