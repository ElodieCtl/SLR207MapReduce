package project.slr207;
import project.slr207.sequential.SequentialCounter;

/**
 * Main and utility functions.
 *
 */
public class Main 
{
    static final String DATA_DIRECTORY = "./data/";
    public static void main( String[] args )
    {
        System.out.println( "Sequential counting of input.txt ..." );

        SequentialCounter.countWordsOfAFile(DATA_DIRECTORY + "input.txt");

        SequentialCounter.countWordsOfAFile(DATA_DIRECTORY + "forestier_mayotte.txt");

        SequentialCounter.countWordsOfAFile(DATA_DIRECTORY + "deontologie_police_nationale.txt");

        SequentialCounter.countWordsOfAFile(DATA_DIRECTORY + "domaine_public_fluvial.txt");

        SequentialCounter.countWordsOfAFile(DATA_DIRECTORY + "sante_publique.txt");
    }

}
