import userInterface.*;

import java.io.File;
import org.apache.pdfbox.text.*;
import plagiarismAlgorithm.*;

public class App {

    public static void main(String[] args) throws Exception {
        UI main = new UI();
        Checker checker = new Checker();

        File file1 = new File("testText1.txt");
        File file2 = new File("testText2.txt");

        System.out.println(checker.getSimilarity(file1, file2));
    }
}
