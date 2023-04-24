package sequential;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.HashMap;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;

/* SequentialCounter.java
 * A simple counter to count the occurences of a word in a file, in a sequential manner.
 * 
 * @author Elodie Chatelin
 */ 
public class SequentialCounter {

    public static void main(String[] args) {
        // Check the number of arguments.
        if (args.length != 1) {
            System.err.println("Usage: java SequentialCounter <filename>");
            System.exit(1);
        }
        // Get the filename from the arguments.
        String filename = args[0];
        // Create a new SequentialCounter.
        SequentialCounter counter = new SequentialCounter();
        // Count the occurences of words in the file.
        counter.count(filename);
        // Print the result.
        System.out.println(counter.map);
        System.out.println(counter.sorted);
    }

    // The number of occurences of the words in the file.
    private final HashMap<String, Integer> map = new HashMap<String, Integer>();
    // The sorted list of words and their occurences.
    private final List<Pair<String, Integer>> sorted = new ArrayList<Pair<String, Integer>>();

    public SequentialCounter() {
    }

    public static int compare(Pair<String, Integer> p1, Pair<String, Integer> p2) {
        if (p1.getValue() > p2.getValue()) {
            return -2;
        } else if (p1.getValue() < p2.getValue()) {
            return 2;
        } else {
            return p1.getKey().compareTo(p2.getKey());
        }
    }

    /**
     * Count the occurences of words in a file.
     * 
     * @param filename The name of the file to read.
     */
    public void count(String filename) {
        // Get a BufferedReader to read the file.
        BufferedReader buffer = MyReader.getReader(filename);
        String line = null;
        try {
            // Read the file line by line.
            while ((line = buffer.readLine()) != null) {
                // Split the line into words.
                String[] words = line.split(" ");
                // Count occurences in the line and update the map.
                for (String w : words) {
                    if (map.containsKey(w)) {
                        map.put(w, map.get(w) + 1);
                    } else {
                        map.put(w, 1);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error while reading the file: " + filename);
            System.exit(1);
        }
    }

    /**
     * Sort the map by occurences (values) and then by alphabetical order (keys).
     */
    public void sort() {
        this.map.forEach(new BiConsumer<String,Integer>() {
            @Override
            public void accept(String t, Integer u) {
                sorted.add(new Pair<String, Integer>(t, u));
            }
        });
        sorted.sort(SequentialCounter::compare);
    }
}