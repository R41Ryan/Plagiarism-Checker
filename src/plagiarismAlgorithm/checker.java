package plagiarismAlgorithm;

import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.lang.Math;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

public class Checker {

    public HashMap<String, Integer> getFrequencyMapPDF(File file)
    {
        System.out.println("Reading " + file.getName());
        HashMap<String, Integer> toReturn = new HashMap<String, Integer>();
        PDDocument document = new PDDocument();
        try {
            document = Loader.loadPDF(file);
        } catch (IOException e)
        {
            System.out.println("Unable to load pdf file!");
            e.printStackTrace();
            return null;
        }

        PDFTextStripper stripper = new PDFTextStripper();
        String pageText;
        try {
            pageText = stripper.getText(document);
            document.close();
        } catch (IOException e1) {
            System.out.println("Unable to get text from pdf!");
            e1.printStackTrace();
            return null;
        }

        Scanner scanner = new Scanner(pageText);

        while (scanner.hasNext())
        {
            String word = scanner.next();
            if (toReturn.containsKey(word))
            {
                toReturn.put(word, toReturn.get(word) + 1);
            }
            else
            {
                toReturn.put(word, 1);
            }
        }

        scanner.close();

        try {
            document.close();
        } catch (IOException e) {
            System.out.println("Could not close PDDocument object!");
            e.printStackTrace();
        }

        /*
        Set<String> words = toReturn.keySet();
        for (String string : words) {
            System.out.printf("%s : %d\n", string, toReturn.get(string));
        }
        System.out.println("*");
        */

        return toReturn;
    }

    public HashMap<String, Integer>[] getFrequencyMapPDF(List<File> files)
    {
        HashMap<String, Integer> toReturn[] = new HashMap[files.size()];
        for (int i = 0; i < files.size(); i++)
        {
            toReturn[i] = getFrequencyMapPDF(files.get(i));
        }
        
        return toReturn;
    }

    public HashMap<String, Integer> getFrequencyMap(File file)
    {
        HashMap<String, Integer> toReturn = new HashMap<String, Integer>();
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file");
            e.printStackTrace();
            return null;
        }

        while (scanner.hasNext())
        {
            String word = scanner.next();
            if (toReturn.containsKey(word))
            {
                toReturn.put(word, toReturn.get(word) + 1);
            }
            else
            {
                toReturn.put(word, 1);
            }
        }

        scanner.close();

        /*
        Set<String> words = toReturn.keySet();
        for (String string : words) {
            System.out.printf("%s : %d\n", string, toReturn.get(string));
        }
        */

        return toReturn;
    }

    public int dotProduct(HashMap<String, Integer> wordFrequency1, HashMap<String, Integer> wordFrequency2)
    {
        int sum = 0;

        Set<String> keySet = wordFrequency1.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            //System.out.println(key);
            if (wordFrequency1.containsKey(key) && wordFrequency2.containsKey(key))
            {
                // System.out.println("Both documents have this word");
                sum += wordFrequency1.get(key) * wordFrequency2.get(key);
            }
            else
            {
                /*
                System.out.println("Both documents do NOT have this word.");
                */
            }
        }

        return sum;
    }

    public double vectorAngle(HashMap<String, Integer> wordFrequency1, HashMap<String, Integer> wordFrequency2)
    {
        int numerator = dotProduct(wordFrequency1, wordFrequency2);
        double denominator = Math.sqrt(dotProduct(wordFrequency1, wordFrequency1)) * Math.sqrt(dotProduct(wordFrequency2, wordFrequency2));
        return 1f - Math.acos(numerator/denominator) / (3.1416f / 2f);
    }

    public double getSimilarityPDF(File file1, File file2) throws IOException
    {
        HashMap<String, Integer> wordFrequencyMap1 = getFrequencyMapPDF(file1);
        HashMap<String, Integer> wordFrequencyMap2 = getFrequencyMapPDF(file2);
        return vectorAngle(wordFrequencyMap1, wordFrequencyMap2);
    }

    public double getSimilarity(File file1, File file2) throws IOException
    {
        HashMap<String, Integer> wordFrequencyMap1 = getFrequencyMap(file1);
        HashMap<String, Integer> wordFrequencyMap2 = getFrequencyMap(file2);
        return vectorAngle(wordFrequencyMap1, wordFrequencyMap2);
    }

    public double getSimilarity(HashMap<String, Integer> wordFrequencyMap1, HashMap<String, Integer> wordFrequencyMap2)
    {
        return vectorAngle(wordFrequencyMap1, wordFrequencyMap2);
    }
}