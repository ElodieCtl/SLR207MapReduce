package src;

import java.io.Serializable;
import java.util.Set;

public class Range implements Serializable {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        Range r1 = new Range(3, 33);
        for (int i = 3; i < 34; i++) {
            System.out.println("-"+i+" : "+r1.attributeTo(i, 10));
        }
    }
    
    public final int start;
    public final int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    // private Range merge(Range other) {
    //     return new Range(Math.min(this.start, other.start), Math.max(this.end, other.end));
    // }

    public static Range mergeRanges(Range[] ranges) {
        if (ranges.length == 0) {
            return null;
        }
        int min = ranges[0].start;
        int max = ranges[0].end;
        for (int i = 1; i < ranges.length; i++) {
            min = Math.min(min, ranges[i].start);
            max = Math.max(max, ranges[i].end);
        }
        return new Range(min, max);
    }

    // machine m is responsible for the range ]start + m*(end-start)/n, start + (m+1)*(end-start)/n]
    // except for the first machine, which is responsible for the range [start, start + (end-start)/n]

    /**
     * Gives the id of the machine that should handle the value.
     * @param value the value to attribute
     * @param n the number of machines
     * @return the id of the machine that should handle the value, between 0 and n-1
     */
    public int attributeTo(int value, int n) {
        for (int i = 0; i < n; i++) {
            if (value <= start + (i + 1) * (end - start) / n) {
                return i;
            }
        }
        return n - 1;
    }

    public Range computeMachineRange(int id, int n) {
        int f = (end - start) / n ;
        if (id == 0) {
            return new Range(start, start + f);
        } else if (id == n - 1) {
            return new Range(start + id * f + 1, end);
        } else {
            return new Range(start + id * f + 1, start + (id + 1) * f);
        }
    }

    public static Range computeFromSet(Set<Integer> set) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer i : set) {
            min = Math.min(min, i);
            max = Math.max(max, i);
        }
        return new Range(min, max);
    }

    public String toString() {
        return "[" + start + ", " + end + "]";
    }
}
