package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static void prettyPrintTable(Object[] table) {
        StringBuilder printed = new StringBuilder("[") ;
        for (Object row : table) {
            printed.append(row).append(", ");
        }
        int n = printed.length();
        if (n > 2) {
            printed.delete(n -2, n);
        }
        System.out.println(printed.append("]").toString());
    }

    public static void main(String[] args) {
        Integer[] test = new Integer[0];
        prettyPrintTable(test);
        prettyPrintTable(readComputersFromFile("computers.txt", 3));
    }

    // Model of a config file
    // list of computers
    public static String[] readComputersFromFile(String filename, int nbComputers) {
        BufferedReader br = null;
        String[] computers = null ;
        try {
            br = new BufferedReader(new FileReader(filename));
            List<String> list = br.lines().collect(Collectors.toList()) ;
            computers = list.subList(0, nbComputers).toArray(new String[0]);
            // String line;
            // int i = 0 ;
            // while ((line = br.readLine()) != null && i < nbComputers) {
            //     computers[i] = line;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        Utils.prettyPrintTable(computers);
        return computers;
    }
}
