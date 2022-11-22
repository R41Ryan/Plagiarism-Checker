package userInterface;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.nio.file.Path;

import plagiarismAlgorithm.*;

// Sort method for a Collection of Files
class SortByFileName implements Comparator<File>
{
    @Override
    public int compare(File o1, File o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

public class UI implements ActionListener{

    // Data members used information and calculations between functions
    private List<File> selectedFiles;
    private String[] fileTexts;
    private double[][] fileSimilarities;
    private HashMap<String, Integer>[] fileWordFrequencies;
    private int numOfOverThreshold;
    private boolean isInFileSelection;
    private boolean usingThreshold;
    private float thresholdValue;
    private int[][] overThresholdSimilarities;  // It is an array of length numOfOverThreshold, 
                                                // where each element is itself another array of length 2, 
                                                // where each refers to an index in fileSimilarities
    private boolean needToCalculate;
    private List<File> filesWithImages;
    private List<Double> characterToWordRatios;

    // Components that are graphically displayed
    private HashMap<String, JPanel> panels;
    private HashMap<String, JButton> buttons;
    private HashMap<String, JTextArea> textAreas;
    private HashMap<String, JTextField> textFields;
    private HashMap<String, JScrollPane> scrollPanes;
    private HashMap<String, JLabel> labels;
    private HashMap<String, JCheckBox> checkboxes;
    private JButton[] fileButtons;

    private JFrame frame;
    
    public UI() {
        // Initialize information data members
        selectedFiles = new ArrayList<File>();
        filesWithImages = new ArrayList<File>();

        frame = new JFrame("Plagiarism Checker");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        panels = new HashMap<String, JPanel>();
        buttons = new HashMap<String, JButton>();
        textAreas = new HashMap<String, JTextArea>();
        textFields = new HashMap<String, JTextField>();
        scrollPanes = new HashMap<String, JScrollPane>();
        labels = new HashMap<String, JLabel>();
        checkboxes = new HashMap<String, JCheckBox>();

        // Initialize JPanels *******************************************************************************
        panels.put("mainPage", new JPanel(new GridBagLayout()));
        panels.put("fileSelectionInner", new JPanel(new GridLayout(0, 2)));
        panels.put("fileSelection", new JPanel(new GridBagLayout()));
        panels.put("fileSimilarity", new JPanel(new FlowLayout()));
        panels.put("overThreshold", new JPanel(new FlowLayout()));
        panels.put("withImages", new JPanel(new FlowLayout()));

        // Initialize Buttons *******************************************************************************

        // Buttons for mainPage
        buttons.put("openFile", new JButton("Open File(s)"));
        buttons.get("openFile").addActionListener(this);
        buttons.put("openFolder", new JButton("Open Folder"));
        buttons.get("openFolder").addActionListener(this);
        buttons.put("checkPlagiarism", new JButton("Check Plagiarism"));
        buttons.get("checkPlagiarism").addActionListener(this);

        // Buttons for file selection page
        buttons.put("fileSelectionBack", new JButton("Back"));
        buttons.get("fileSelectionBack").addActionListener(this);
        buttons.put("filesOverThreshold", new JButton("Files over Threshold"));
        buttons.get("filesOverThreshold").addActionListener(this);
        buttons.put("withImages", new JButton("Files with images"));
        buttons.get("withImages").addActionListener(this);
        
        // buttons for files over threshold page
        buttons.put("overThresholdBack", new JButton("Back"));
        buttons.get("overThresholdBack").addActionListener(this);

        // buttons for file similarity page
        buttons.put("fileSimilarityBack", new JButton("Back"));
        buttons.get("fileSimilarityBack").addActionListener(this);

        // buttons for files with images page
        buttons.put("withImagesBack", new JButton("Back"));
        buttons.get("withImagesBack").addActionListener(this);

        // Initialize textAreas *****************************************************************************

        // text areas for mainPage
        textAreas.put("selectedFiles", new JTextArea(25, 50));
        textAreas.get("selectedFiles").setEditable(false);
        textAreas.get("selectedFiles").setLineWrap(true);

        // text areas for file similarity page
        textAreas.put("fileSimilarity", new JTextArea(25, 50));
        textAreas.get("fileSimilarity").setEditable(false);
        textAreas.get("fileSimilarity").setLineWrap(true);

        // text areas for over threshold panel
        textAreas.put("overThreshold", new JTextArea(25, 50));
        textAreas.get("overThreshold").setEditable(false);
        textAreas.get("overThreshold").setLineWrap(true);

        // text areas for with images panel
        textAreas.put("withImages", new JTextArea(25, 50));
        textAreas.get("withImages").setEditable(false);
        textAreas.get("withImages").setLineWrap(true);

        // Initialize textFields ********************************************************************************
        
        // text fields for mainPage
        textFields.put("thresholdField", new JTextField());

        // Initialize scrollPanes **********************************************************************************************
        
        // scroll pane for selected files
        scrollPanes.put("selectedFiles", new JScrollPane(textAreas.get("selectedFiles")));
        scrollPanes.get("selectedFiles").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // scroll pane for file selection page
        scrollPanes.put("fileSelection", new JScrollPane(panels.get("fileSelectionInner")));
        scrollPanes.get("fileSelection").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanes.get("fileSelection").setPreferredSize((new Dimension(650, 400)));

        // scroll pane for file similarity page
        scrollPanes.put("fileSimilarity", new JScrollPane(textAreas.get("fileSimilarity")));
        scrollPanes.get("fileSimilarity").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // scroll pane for over threshold page
        scrollPanes.put("overThreshold", new JScrollPane(textAreas.get("overThreshold")));
        scrollPanes.get("overThreshold").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // scroll pane for with images page
        scrollPanes.put("withImages", new JScrollPane(textAreas.get("withImages")));
        scrollPanes.get("withImages").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Initialize Labels ***************************************************************************************************

        // Initialize checkboxes ***********************************************************************************************
        checkboxes.put("thresholdValue", new JCheckBox("Plagiarism Threshold:"));

        loadMainPage();
        isInFileSelection = false;
        needToCalculate = true;
        frame.pack();
        frame.validate();
    }

    // Functions ********************************************************************************************************************

    // Load main page
    void loadMainPage()
    {
        panels.get("mainPage").removeAll();

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panels.get("mainPage").add(buttons.get("openFile"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        panels.get("mainPage").add(buttons.get("openFolder"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        panels.get("mainPage").add(buttons.get("checkPlagiarism"), c);
        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 1;
        panels.get("mainPage").add(checkboxes.get("thresholdValue"), c);

        c.gridx = 1;
        c.gridy = 1;
        panels.get("mainPage").add(textFields.get("thresholdField"), c);

        if (selectedFiles.size() <= 0)
        {
            textAreas.get("selectedFiles").setText(" Selected Files:\n");
        }
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        panels.get("mainPage").add(scrollPanes.get("selectedFiles"), c);

        frame.setContentPane(panels.get("mainPage"));
    }

    // Load the file selection page
    void loadFileSelectionPage()
    {
        panels.get("fileSelection").removeAll();
        panels.get("fileSelectionInner").removeAll();
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        panels.get("fileSelection").add(buttons.get("fileSelectionBack"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        panels.get("fileSelection").add(buttons.get("filesOverThreshold"), c);
        String thresholdString = "Files over Threshold (None)";
        if (usingThreshold && numOfOverThreshold > 0)
        {
            thresholdString = "Files over Threshold (" + numOfOverThreshold + ")";
        }
        buttons.get("filesOverThreshold").setText(thresholdString);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        panels.get("fileSelection").add(buttons.get("withImages"), c);
        String withImagesString = "Files over Threshold (None)";
        if (filesWithImages.size() > 0)
        {
            withImagesString = "Files with images (" + filesWithImages.size() + ")";
        }
        buttons.get("withImages").setText(withImagesString);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        for (int i = 0; i < fileButtons.length; i++)
        {
            panels.get("fileSelectionInner").add(fileButtons[i]);
        }
        panels.get("fileSelection").add(scrollPanes.get("fileSelection"), c);

        frame.setContentPane(panels.get("fileSelection"));
    }

    // Load the file similarity page for a specific file as indicated by the index
    void loadFileSimilarity(int i)
    {
        panels.get("fileSimilarity").removeAll();
        textAreas.get("fileSimilarity").setText("");
        for (int j = 0; j < selectedFiles.size(); j++)
        {
            String toPrint = "Similarity with " + selectedFiles.get(j).getName() + ":\n" + String.format("%.3f", fileSimilarities[i][j]) + "\n\n";
            if (usingThreshold)
            {
                if (fileSimilarities[i][j] >= thresholdValue && i != j)
                {
                    toPrint = "*** Similarity with " + selectedFiles.get(j).getName() + ": ***\n" + String.format("%.3f", fileSimilarities[i][j]) + "\n\n";
                }
            }
            textAreas.get("fileSimilarity").append(toPrint);
        }
        panels.get("fileSimilarity").add(buttons.get("fileSimilarityBack"));
        panels.get("fileSimilarity").add(scrollPanes.get("fileSimilarity"));
        frame.setContentPane(panels.get("fileSimilarity"));
    }

    // Loads the panel that displays the file pairs with similarities over the thesholdValue.
    void loadOverThresholdPanel()
    {
        panels.get("overThreshold").removeAll();
        panels.get("overThreshold").add(buttons.get("overThresholdBack"));
        textAreas.get("overThreshold").setText("");
        for (int i = 0; i < numOfOverThreshold; i++)
        {
            File file1 = selectedFiles.get(overThresholdSimilarities[i][0]);
            File file2 = selectedFiles.get(overThresholdSimilarities[i][1]);
            textAreas.get("overThreshold").append(file1.getName() + " vs. " + file2.getName()
             + "\n" + String.format("%.3f", fileSimilarities[overThresholdSimilarities[i][0]][overThresholdSimilarities[i][1]])
             + "\n\n");
        }
        panels.get("overThreshold").add(scrollPanes.get("overThreshold"));
        frame.setContentPane(panels.get("overThreshold"));
    }

    // Loads the panel that displays the files that have images
    void loadWithImagesPanel()
    {
        panels.get("withImages").removeAll();
        panels.get("withImages").add(buttons.get("withImagesBack"));
        textAreas.get("withImages").setText("");
        for (int i = 0; i < filesWithImages.size(); i++)
        {
            textAreas.get("withImages").append(filesWithImages.get(i).getName() + "\n\n");
        }
        panels.get("withImages").add(scrollPanes.get("withImages"));
        frame.setContentPane(panels.get("withImages"));
    }

    // Calculate the similarities for all files
    void calculateSimilarities()
    {
        numOfOverThreshold = 0;
        fileSimilarities = new double[selectedFiles.size()][selectedFiles.size()];
        Checker checker = new Checker();
        if (needToCalculate)
        {
            fileTexts = new String[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                fileTexts[i] = checker.readPDFDocument(selectedFiles.get(i));
            }

            HashMap<String, Integer> newWordFrequencies[] = new HashMap[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                newWordFrequencies[i] = checker.getFrequencyMapPDF(fileTexts[i]);
            }

            fileWordFrequencies = newWordFrequencies;
        }

        for (int i = 0; i < selectedFiles.size(); i++)
        {
            for (int j = 0; j < selectedFiles.size(); j++)
            {
                fileSimilarities[i][j] = checker.getSimilarity(fileWordFrequencies[i], fileWordFrequencies[j]);
            }
        }

        if (usingThreshold)
        {
            fillOverThresholdSimilarities();
            sortOverThresholdSimilarities();
        }
    }

    // Create the buttons for the selected files
    void createFileButtons()
    {
        if (needToCalculate)
        {
            fileButtons = new JButton[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                fileButtons[i] = new JButton(selectedFiles.get(i).getName());
                fileButtons[i].addActionListener(this);
                fileButtons[i].setPreferredSize(new Dimension(300, 20));
            }
        }
    }

    // Retrieves the similarities that are over the threshold in fileSimilarities, and put the pairs of indices into
    // overThresholdSimilarities
    void fillOverThresholdSimilarities()
    {
        if (usingThreshold)
        {
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                for (int j = i; j < selectedFiles.size(); j++)
                {
                    if (fileSimilarities[i][j] >= thresholdValue && i != j)
                    {
                        numOfOverThreshold++;
                    }
                }
            }
            overThresholdSimilarities = new int[numOfOverThreshold][2];

            int overThresholdIndex = 0;
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                for (int j = i; j < selectedFiles.size(); j++)
                {
                    if (fileSimilarities[i][j] >= thresholdValue && i != j)
                    {
                        overThresholdSimilarities[overThresholdIndex][0] = i;
                        overThresholdSimilarities[overThresholdIndex][1] = j;
                        overThresholdIndex++;
                    }
                }
            }
        }
    }

    // Switch the 2 file index pairs in overThresholdSimilarities.
    void switchOverThresholdSimilarities(int a, int b)
    {
        if (a < overThresholdSimilarities.length && b < overThresholdSimilarities.length)
        {
            int[] temp = overThresholdSimilarities[a];
            overThresholdSimilarities[a] = overThresholdSimilarities[b];
            overThresholdSimilarities[b] = temp;
        }
    }

    // Sort the overthresholdSimilarities array such that the similarities of the two file indices are in descending order.
    void sortOverThresholdSimilarities()
    {
        if (overThresholdSimilarities.length > 0)
        {
            for (int i = 1; i < overThresholdSimilarities.length; i++)
            {
                int currentIndex = i;
                double similarityValue = fileSimilarities[overThresholdSimilarities[i][0]][overThresholdSimilarities[i][1]];
                boolean isGreaterThanPrev = true;
                while (currentIndex > 0 && isGreaterThanPrev)
                {
                    double prevSimilarity = fileSimilarities[overThresholdSimilarities[currentIndex - 1][0]][overThresholdSimilarities[currentIndex - 1][1]];
                    if (prevSimilarity < similarityValue)
                    {
                        switchOverThresholdSimilarities(currentIndex - 1, currentIndex);
                        currentIndex--;
                    }
                    else
                    {
                        isGreaterThanPrev = false;
                    }
                }
            }
        }
    }

    // Recursive function where it adds pdf files to selected files. If the file it's looking at is a directory, it calls itself
    // Otherwise, it will add the file to the selected files
    void addToSelectedPDF(File file)
    {
        for (File currentFile : file.listFiles())
        {
            if (currentFile.isDirectory())
            {
                addToSelectedPDF(currentFile);
            }
            else if (currentFile.getName().endsWith(".pdf"))
            {
                selectedFiles.add(currentFile);
            }
        }
    }

    // Check the selectedFiles to see which has images. If it does, add them to the filesWithImages List
    void checkForImages() throws IOException
    {
        if (needToCalculate)
        {
            filesWithImages.clear();
            ImageChecker checker;
            try {
                checker = new ImageChecker();
            } catch (IOException e) {
                System.out.printf("Unable to open Image Checker\n");
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                checker.loadFile(selectedFiles.get(i));
                checker.processFile();
                if (checker.getNumOfImages() > 0)
                {
                    filesWithImages.add(selectedFiles.get(i));
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int fileButtonSelected = -1;
        if (isInFileSelection)
        {
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                if (e.getSource() == fileButtons[i])
                {
                    fileButtonSelected = i;
                    break;
                }
            } 
        }
        

        if (e.getSource() == buttons.get("openFile")) // User pressed "Open File(s) button on main page"
        {
            JFileChooser fileChooser = new JFileChooser(Path.of("").toAbsolutePath().toString(), FileSystemView.getFileSystemView());

            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setDialogTitle("Select a .pdf file");

            FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .pdf files", "pdf");
            fileChooser.addChoosableFileFilter(restrict);

            int selectionReturn = fileChooser.showOpenDialog(null);

            if (selectionReturn == JFileChooser.APPROVE_OPTION)
            {
                selectedFiles.clear();
                for (File file : fileChooser.getSelectedFiles())
                {
                    selectedFiles.add(file);
                }

                Collections.sort(selectedFiles, new SortByFileName());

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected Files:\n");

                while (i < selectedFiles.size())
                {
                    textAreas.get("selectedFiles").append("\n     " + selectedFiles.get(i).getName());
                    i++;
                }

                needToCalculate = true;
            }
            System.out.printf("%d files\n", selectedFiles.size());

            needToCalculate = true;
        }
        else if (e.getSource() == buttons.get("openFolder"))
        {
            JFileChooser fileChooser = new JFileChooser(Path.of("").toAbsolutePath().toString(), FileSystemView.getFileSystemView());

            fileChooser.setDialogTitle("Select a folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int selectionReturn = fileChooser.showOpenDialog(null);

            if (selectionReturn == JFileChooser.APPROVE_OPTION)
            {
                selectedFiles.clear();
                File directory = new File(fileChooser.getSelectedFile().getAbsolutePath());
                addToSelectedPDF(directory);
                
                Collections.sort(selectedFiles, new SortByFileName());

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected Files:\n");

                while (i < selectedFiles.size())
                {
                    textAreas.get("selectedFiles").append("\n     " + selectedFiles.get(i).getName());
                    i++;
                }
            }
            System.out.printf("%d files\n", selectedFiles.size());

            needToCalculate = true;
        }
        else if (e.getSource() == buttons.get("checkPlagiarism")) // User pressed check plagiarism button
        {
            usingThreshold = checkboxes.get("thresholdValue").isSelected();
            if (usingThreshold)
            {
                try {
                    thresholdValue = Float.valueOf(textFields.get("thresholdField").getText());
                }
                catch (NumberFormatException n)
                {
                    JOptionPane.showMessageDialog(frame, "Enter a valid number for threshold", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            /*
            for (int i = 0; i < selectedFiles.length; i++)
            {
                for (int j = 0; j < selectedFiles.length; j++)
                {
                    System.out.print(fileSimilarities[i][j] + " ");
                }
                System.out.println();
            }
            */
            if (selectedFiles.size() > 1)
            {
                calculateSimilarities();
                try {
                    checkForImages();
                } catch (IOException e1) {
                    System.out.printf("Error while checking images\n");
                    e1.printStackTrace();
                }
                createFileButtons();
                needToCalculate = false;
                loadFileSelectionPage();
                isInFileSelection = true;
            }
        }
        else if (e.getSource() == buttons.get("fileSelectionBack")) // user clicked back button on file selection screen
        {
            loadMainPage();
            isInFileSelection = false;
        }
        else if (e.getSource() == buttons.get("filesOverThreshold"))
        {
            if (usingThreshold)
            {
                loadOverThresholdPanel();
            }
        }
        else if (e.getSource() == buttons.get("overThresholdBack"))
        {
            loadFileSelectionPage();
        }
        else if (fileButtonSelected >= 0)
        {
            loadFileSimilarity(fileButtonSelected);
            isInFileSelection = false;
        }
        else if (e.getSource() == buttons.get("fileSimilarityBack"))
        {
            loadFileSelectionPage();
            isInFileSelection = true;
        }
        else if (e.getSource() == buttons.get("withImages"))
        {
            loadWithImagesPanel();
            isInFileSelection = false;
        }
        else if (e.getSource() == buttons.get("withImagesBack"))
        {
            loadFileSelectionPage();
            isInFileSelection = true;
        }
        
        frame.pack();
        frame.validate();
    }
}
