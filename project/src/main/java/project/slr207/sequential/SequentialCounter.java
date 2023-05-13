package project.slr207.sequential;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * SequentialCounter.java
 * A simple counter to count the occurences of words in a file, in a sequential manner.
 */
public class SequentialCounter {

    // The number of occurences of the words in the file.
    private final HashMap<String, Integer> map = new HashMap<String, Integer>();
    private ArrayList<Entry<String, Integer>> sortedMap = new ArrayList<Entry<String, Integer>>();

    /**
     * Count the occurences of words in a file.
     * 
     * @param filename The name of the text file to count the words of.
     */
    public static void countWordsOfAFile(String fileName, PrintStream out) {
        System.out.println("--- \nCounting words of " + fileName + " ...\n---");
        SequentialCounter counter = new SequentialCounter();
        BufferedReader reader = getReader(fileName);
        long start = System.currentTimeMillis();
        counter.count(reader);
        long countingEnd = System.currentTimeMillis();
        counter.sort();
        long end = System.currentTimeMillis();
        out.println("\n-> Time elapsed for counting : " + (countingEnd - start) + " ms");
        out.println("-> Time elapsed for sorting : " + (end - countingEnd) + " ms");
        out.println("-> Total time elapsed : " + (end - start) + " ms");
        out.println("\n//////////\n") ;
        out.println("50 most frequent words (or less) :\n");
        counter.printResult(out);
        out.println("\n//////////\n") ;
    }

    public static void countWordsOfAFile(String fileName) {
        countWordsOfAFile(fileName, System.out);
    }
        
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
            System.exit(0); // TODO: change to 1
        }
        return reader;
    }

    public SequentialCounter() {
    }

    /**
     * Compare two entries by their values (occurence) and then by their keys (word).
     */
    private static final Comparator<Entry<String, Integer>> MAP_COMPARATOR = new Comparator<Entry<String,Integer>>() {
        @Override
        public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
            // reverse order to get descending order for values
            int valueComparison = e2.getValue().compareTo(e1.getValue()) ;
            if (valueComparison != 0) {
                return valueComparison;
            } else {
                return e1.getKey().compareTo(e2.getKey()) ;
            }
        }
    };

    /**
     * Count the occurences of words in a file.
     * 
     * @param buffer The text file to read.
     */
    public void count(BufferedReader buffer) {
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
            System.err.println("Error while reading the file");
            System.exit(1);
        }
    }

    /**
     * Sort the map by occurences (values) and then by alphabetical order (keys).
     */
    public void sort() {
        this.map.entrySet()
            .stream()
            .sorted(MAP_COMPARATOR)
            .forEach(new Consumer<Entry<String,Integer>>() {
                @Override
                public void accept(Entry<String,Integer> t) {
                    sortedMap.add(t);
                }
            });
    }

    private void printResult(PrintStream out) {
        for (int i = 0; i < Math.min(50,this.sortedMap.size()); i++) {
            Entry<String, Integer> e = this.sortedMap.get(i);
            out.println(e.getKey() + " : " + e.getValue());
        }
    }

    private static final Collector<Entry<String, Integer>, ?, Map<String, Integer>> MAP_COLLECTOR
        = Collectors.toMap(new Function<Entry<String, Integer>,String>() {
            @Override
            public String apply(Entry<String, Integer> t) {
                return t.getKey();
            }
        }, new Function<Entry<String, Integer>,Integer>() {
            @Override
            public Integer apply(Entry<String, Integer> t) {
                return t.getValue();
            }
        });
    
}
