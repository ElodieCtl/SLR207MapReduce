package src;

import java.util.HashMap;

public class Utils {

    public static void printHashmap(HashMap<String, Integer> map) {
        for (String key : map.keySet()) {
            System.out.println(key + " : " + map.get(key));
        }
    }

    public static void prettyPrintTable(Object[] table) {
        System.out.print("{");
        for (Object row : table) {
            System.out.print(row + ", ");
        }
        System.out.println("}");
    }
    
}
