package src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * <h1>SortedMap.java</h1>
 * 
 * <p>
 * A map of pairs of a String (key) and an Integer (value),
 * sorted either by their keys or their values and than the keys after reduce.
 * </p>
 * <p> 
 * This class is used to store the result of a map-reduce operation,
 * where the keys are the words and the values are the number of occurences.
 * </p>
 * <p>
 * The typical scenario is to create a SortedMap, insert pairs into it,
 * individually or from another map which gives a sorted map
 * according to the alphabetical order and then reduce it,
 * to get a sorted map according to the values and then keys.
 * </p>
 * <p>
 * At any time, feel free to merge two SortedMap together,
 * if they are sorted according to the same order (both before or after reduce),
 * and print them into the console.
 */
public class SortedMap implements Serializable {

    /**
     * Main method to test the SortedMap class.
     * @param args ignored.
     */
    public static void main(String[] args) {
        SortedMap map = new SortedMap();
        map.insert("a", 3);
        map.insert("c", 1);
        map.insert("b", 2);
        // System.out.println("Map 1 :");
        // map.print();

        map.insert("b", 4);
        // System.out.println("After inserting (b,4) into Map 1:");
        // map.print();

        SortedMap map2 = new SortedMap();
        map2.insert("t", 3);
        map2.insert("c", 1);
        map2.insert("b", 2);
        // System.out.println("Map 2 :");
        // map2.print();

        SortedMap merged = map.mergeWith(map2);
        System.out.println("After merging Map 1 and Map 2 :");
        merged.print();

        SortedMap reduced = merged.reduce();
        System.out.println("After reducing the merged map :");
        reduced.print();

        SortedMap reduced2 = new SortedMap();
        reduced2.insert("c", 3);
        reduced2.insert("d", 1);
        reduced2.insert("c", 2);
        reduced2 = reduced2.reduce();
        System.out.println("After reducing another map :");
        reduced2.print();

        SortedMap reduced3 = reduced.mergeWith(reduced2) ;
        System.out.println("After merging the two reduced maps :");
        reduced3.print();

        /* Pair pair = new Pair("a", 2);
        Pair pair2 = new Pair("b", 3);
        System.out.println("a greater than b ? " + pair.isGreaterOrEqual(pair2)); */
    }

    ////////////////////////// COMPARISON FUNCTIONS //////////////////////////

    /**
     * Interface for a comparison function between two Pair objects.
     * 
     * This interface is used to compare two pairs of a SortedMap.
     * 
     * @see SortedMap
     * @see Pair
     */
    private interface ComparisonFunction {

        /**
         * Compare two pairs.
         * @param a The first pair.
         * @param b The second pair.
         * @return true if the first pair is greater (or equal) than the second, false otherwise.
         */
        public boolean isGreaterOrEqual(Pair a, Pair b);
    }

    public static ComparisonFunction VALUES_THEN_KEYS_COMPARISON = new ComparisonFunction() {
        @Override
        public boolean isGreaterOrEqual(Pair a, Pair b) {
            return a.isGreaterOrEqual(b) ;
        }
    };

    public static ComparisonFunction ALPHABETICAL_COMPARISON = new ComparisonFunction() {
        @Override
        public boolean isGreaterOrEqual(Pair a, Pair b) {
            return a.key.compareTo(b.key) >= 0;
        }
    };

    ////////////////////////// PAIR CLASS //////////////////////////

    /**
     * A pair of a key and a value.
     * 
     * This class is used to store a key and a value in a SortedMap.
     * 
     * @see SortedMap
     */
    static class Pair {
        public final String key;
        public final int value;

        public Pair(String key, Integer value) {
            
            if (value == null || value < 0) {
                throw new IllegalArgumentException("Value must be non-null and positive.");
            }

            this.key = key;
            this.value = value;
        }

        /**
         * Check if two pairs have the same key.
         * @param other The other pair to compare with.
         * @return true if the keys are equal, false otherwise.
         */
        public boolean equalKeys(Pair other) {
            return this.key.equals(other.key) ;
        }

        /**
         * Compare two pairs, according to their values and then their keys.
         * @param other The other pair to compare with.
         * @return true if the first pair is greater than (or equal to) the second pair.
         */
        public boolean isGreaterOrEqual(Pair other) {
            if (this.value == other.value) {
                return this.key.compareTo(other.key) >= 0;
            } else {
                return (other.value > this.value);
            }
        }
    }

    ////////////////////////// SORTED MAP CLASS //////////////////////////

    private static final long serialVersionUID = 1L;

    private final ArrayList<Pair> list;
    private final ComparisonFunction comparisonFunction ;

    public SortedMap() {
        this.list = new ArrayList<Pair>();
        this.comparisonFunction = ALPHABETICAL_COMPARISON;
    }

    private SortedMap(ComparisonFunction comparisonFunction) {
        this.list = new ArrayList<Pair>();
        this.comparisonFunction = comparisonFunction;
    }

    /**
     * Insert all the pairs of a HashMap in the sorted map, keeping the order.
     * @param map The HashMap to insert.
     */
    public void insertHashMap(HashMap<String,Integer> map) {
        map.entrySet()
            .stream()
            .forEach(new Consumer<Entry<String,Integer>>() {
                @Override
                public void accept(Entry<String,Integer> t) {
                    insert(t.getKey(), t.getValue());
                }
            });
    }

    /**
     * Insert a new pair in the sorted map, keeping the order by using a binary search.
     * @param key The key of the pair.
     * @param value The value of the pair.
     */
    public void insert(String key, Integer value) {
        this.insert(new Pair(key, value));
    }

    private void insert(Pair pair) {
        // TODO : use a binary search
        for (int i = 0; i < this.list.size(); i++) {
            Pair current = this.list.get(i);
            if (this.comparisonFunction.isGreaterOrEqual(current, pair)) {
                this.list.add(i, pair);
                return;
            }
        }
        // pair is greater than all the elements of the list
        this.list.add(pair);
    }

    /**
     * Merge two sorted maps, keeping the order by using a merge sort.
     * 
     * @param other The other sorted map to merge with.
     * @return A new sorted map containing the merged elements.
     * @throws UnsupportedOperationException If the two maps have different comparison functions.
     */
    public SortedMap mergeWith(SortedMap other) {
        if (this.comparisonFunction != other.comparisonFunction) {
            throw new UnsupportedOperationException("Can only merge two maps with the same comparison function.");
        }
        SortedMap merged = new SortedMap(this.comparisonFunction);
        int i = 0;
        int j = 0;
        int n = this.list.size();
        int m = other.list.size();
        while (i < n && j < m) {
            Pair a = this.list.get(i);
            Pair b = other.list.get(j);
            if (this.comparisonFunction.isGreaterOrEqual(a,b)) {
                merged.list.add(b);
                j++;
            } else {
                merged.list.add(a);
                i++;
            }
        }
        while (i < n) {
            merged.list.add(this.list.get(i));
            i++;
        }
        while (j < m) {
            merged.list.add(other.list.get(j));
            j++;
        }
        return merged;
    }

    /**
     * Reducing a map sorted by keys by summing the values of the same keys.
     * 
     * @return A new map with the same keys but summed values, sorted by values (descending) and than by keys.
     * @throws UnsupportedOperationException If the map is not sorted by keys.
     */
    public SortedMap reduce() {
        if (this.comparisonFunction != ALPHABETICAL_COMPARISON) {
            throw new UnsupportedOperationException("Can only reduce a map sorted by keys.");
        }
        SortedMap reduced = new SortedMap(VALUES_THEN_KEYS_COMPARISON);
        int n = this.list.size();
        if (n == 0) {
            return reduced;
        }
        Pair a = this.list.get(0);
        for (int i = 1; i < n; i++) {
            Pair b = this.list.get(i);
            if (a.equalKeys(b)) {
                a = new Pair(a.key, a.value + b.value);
            } else {
                reduced.insert(a);
                a = b;
            }
            //reduced.print();
        }
        reduced.insert(a);
        return reduced;
    }

    /**
     * Print the map to the console.
     */
    public void print() {
        System.out.println("SortedMap of " + this.list.size() + " elements.");
        int i = 0;
        for (Pair pair : this.list) {
            System.out.println("["+i+"] "+pair.key + " : " + pair.value);
            i++;
            if (i > 50) {
                System.out.println("...");
                return;
            }
        }
    }
}
