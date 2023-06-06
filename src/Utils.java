package src;

import java.util.HashMap;

public class Utils {

    public static void printHashmap(HashMap<String, Integer> map) {
        for (String key : map.keySet()) {
            System.out.println(key + " : " + map.get(key));
        }
    }
    
}
