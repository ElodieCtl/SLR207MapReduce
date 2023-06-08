package src;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map.Entry;
import java.util.function.Consumer;

public class Utils {

    public static void prettyPrintTable(Object[] table) {
        String printed = "[" ;
        for (Object row : table) {
            printed += row + ", ";
        }
        System.out.println(printed +"]");
    }
}
