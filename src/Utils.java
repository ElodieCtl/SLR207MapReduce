package src;

public class Utils {

    public static void prettyPrintTable(Object[] table) {
        String printed = "[" ;
        for (Object row : table) {
            printed += row + ", ";
        }
        System.out.println(printed +"]");
    }
    
}
