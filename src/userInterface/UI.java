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
import java.sql.Struct;
import java.util.regex.*;

import plagiarismAlgorithm.*;

// Sort method for a Collection of Files
class SortByFilePath implements Comparator<File> {
    @Override
    public int compare(File o1, File o2) {
        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
    }
}

// Class for containing the character-to-page ratio for a given file as indicated by their index in selectedFiles
class FileRatio {
    public double ratio;
    public int fileIndex;

    FileRatio() {}
    FileRatio(double r, int i) {
        ratio = r;
        fileIndex = i;
    }
}

// Class for containing the multiple files with similar IDs (For use with files downloaded from Desire2Learn)
class FileId {
    public String id; // The id that the files share
    public List<Integer> indices; // the indices in "selectedFiles" that share the id above

    FileId() {}
    FileId(String i, int index) {
        id = i;
        indices = new ArrayList<Integer>();
        indices.add(index);
    }

    void add(int index) {
        indices.add(index);
    }

    String getId() {
        return id;
    }
}

public class UI implements ActionListener {

    // Data members used information and calculations between functions
    private List<File> selectedFiles;
    private List<Integer> ignoredFiles; // This is a list of indices of selected files which are to be ignored
                                        // This is for files downloaded from D2L, where each submission is placed
                                        // in a unique folder.
    private String[] fileTexts;
    private double[][] fileSimilarities;
    private HashMap<String, Integer>[] fileWordFrequencies;
    private int numOfOverThreshold;
    private boolean isInFileSelection;
    private boolean usingThreshold;
    private boolean usingD2LDuplicates;
    private float thresholdValue;
    private int[][] overThresholdSimilarities; // It is an array of length numOfOverThreshold,
                                               // where each element is itself another array of length 2,
                                               // where each refers to an index in fileSimilarities
    private List<File> filesWithImages;
    private List<FileRatio> characterToPageRatios;
    private boolean needToCalculate;
    private boolean isReadingFiles;
    private boolean helpMode;   // When true, then when pressing any buttons, it will open a dialog box
                                // describing its function, rather than performing the function itself.

    // Components that are graphically displayed
    private HashMap<String, JPanel> panels;
    private HashMap<String, JButton> buttons;
    private HashMap<String, JTextArea> textAreas;
    private HashMap<String, JTextField> textFields;
    private HashMap<String, JScrollPane> scrollPanes;
    private HashMap<String, JLabel> labels;
    private HashMap<String, JCheckBox> checkboxes;
    private HashMap<String, JProgressBar> progressBars;
    private JButton[] fileButtons;
    private JLabel[] fileLabels;
    private JPanel[] fileSelectionPanels;

    private JFrame frame;

    public UI() {
        // Initialize information data members
        selectedFiles = new ArrayList<File>();
        ignoredFiles = new ArrayList<Integer>();
        filesWithImages = new ArrayList<File>();
        characterToPageRatios = new ArrayList<FileRatio>();

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
        progressBars = new HashMap<String, JProgressBar>();

        // Initialize JPanels
        // *******************************************************************************
        panels.put("mainPage", new JPanel(new GridBagLayout()));
        panels.put("fileSelectionInner", new JPanel(new GridLayout(0, 1)));
        panels.put("fileSelection", new JPanel(new GridBagLayout()));
        panels.put("fileSimilarity", new JPanel(new GridBagLayout()));
        panels.put("overThreshold", new JPanel(new FlowLayout()));
        panels.put("withImages", new JPanel(new FlowLayout()));
        panels.put("loading", new JPanel(new FlowLayout()));
        panels.put("fileRatios", new JPanel(new FlowLayout()));

        // Initialize Buttons
        // *******************************************************************************

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
        buttons.put("fileRatios", new JButton("Character to Page Ratios"));
        buttons.get("fileRatios").addActionListener(this);

        // buttons for files over threshold page
        buttons.put("overThresholdBack", new JButton("Back"));
        buttons.get("overThresholdBack").addActionListener(this);

        // buttons for file similarity page
        buttons.put("fileSimilarityBack", new JButton("Back"));
        buttons.get("fileSimilarityBack").addActionListener(this);

        // buttons for files with images page
        buttons.put("withImagesBack", new JButton("Back"));
        buttons.get("withImagesBack").addActionListener(this);

        // buttons for file ratios
        buttons.put("fileRatiosBack", new JButton("Back"));
        buttons.get("fileRatiosBack").addActionListener(this);

        // Common buttons used in multiple panels
        buttons.put("helpToggle", new JButton("Help Mode: Off"));
        buttons.get("helpToggle").addActionListener(this);

        // Initialize textAreas
        // *****************************************************************************

        // text areas for mainPage
        textAreas.put("selectedFiles", new JTextArea(25, 50));
        textAreas.get("selectedFiles").setEditable(false);
        textAreas.get("selectedFiles").setLineWrap(false);

        // text areas for file similarity page
        textAreas.put("fileSimilarity", new JTextArea(25, 100));
        textAreas.get("fileSimilarity").setEditable(false);
        textAreas.get("fileSimilarity").setLineWrap(false);

        // text areas for over threshold panel
        textAreas.put("overThreshold", new JTextArea(25, 50));
        textAreas.get("overThreshold").setEditable(false);
        textAreas.get("overThreshold").setLineWrap(false);

        // text areas for with images panel
        textAreas.put("withImages", new JTextArea(25, 50));
        textAreas.get("withImages").setEditable(false);
        textAreas.get("withImages").setLineWrap(false);

        // text areas for loading screen
        textAreas.put("loading", new JTextArea(25, 50));
        textAreas.get("loading").setEditable(false);
        textAreas.get("loading").setLineWrap(true);

        // text areas for file ratios panel
        textAreas.put("fileRatios", new JTextArea(25, 50));
        textAreas.get("fileRatios").setEditable(false);

        // Initialize textFields
        // ********************************************************************************

        // text fields for mainPage
        textFields.put("thresholdField", new JTextField());

        // Initialize scrollPanes
        // **********************************************************************************************

        // scroll pane for selected files
        scrollPanes.put("selectedFiles", new JScrollPane(textAreas.get("selectedFiles")));
        scrollPanes.get("selectedFiles").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanes.get("selectedFiles").setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // scroll pane for file selection page
        scrollPanes.put("fileSelection", new JScrollPane(panels.get("fileSelectionInner")));
        scrollPanes.get("fileSelection").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanes.get("fileSelection").setPreferredSize((new Dimension(650, 400)));

        // scroll pane for file similarity page
        scrollPanes.put("fileSimilarity", new JScrollPane(textAreas.get("fileSimilarity")));
        scrollPanes.get("fileSimilarity").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPanes.get("fileSimilarity").setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // scroll pane for over threshold page
        scrollPanes.put("overThreshold", new JScrollPane(textAreas.get("overThreshold")));
        scrollPanes.get("overThreshold").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanes.get("overThreshold").setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // scroll pane for with images page
        scrollPanes.put("withImages", new JScrollPane(textAreas.get("withImages")));
        scrollPanes.get("withImages").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanes.get("withImages").setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // scroll pane for loading screen
        scrollPanes.put("loading", new JScrollPane(textAreas.get("loading")));
        scrollPanes.get("loading").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPanes.get("loading").getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });

        // scroll pane for file ratio page
        scrollPanes.put("fileRatios", new JScrollPane(textAreas.get("fileRatios")));
        scrollPanes.get("fileRatios").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanes.get("fileRatios").setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Initialize Labels
        // ***************************************************************************************************

        // Labels for file similarity
        labels.put("fileSimilarity", new JLabel());

        // Initialize checkboxes
        // ***********************************************************************************************
        checkboxes.put("thresholdValue", new JCheckBox("Plagiarism Threshold:"));
        checkboxes.put("duplicates", new JCheckBox("Remove Duplicates (Desire2Learn)"));

        // Initialize progressBars
        // *********************************************************************************************
        progressBars.put("loading", new JProgressBar());
        progressBars.get("loading").setStringPainted(true);

        loadMainPage();
        isInFileSelection = false;
        needToCalculate = true;
        isReadingFiles = false;
        helpMode = false;
        frame.pack();
        frame.validate();
    }

    // Functions
    // ********************************************************************************************************************

    // Load main page
    void loadMainPage() {
        panels.get("mainPage").removeAll();

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        panels.get("mainPage").add(buttons.get("openFile"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        panels.get("mainPage").add(buttons.get("openFolder"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panels.get("mainPage").add(checkboxes.get("duplicates"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        panels.get("mainPage").add(buttons.get("checkPlagiarism"), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        panels.get("mainPage").add(checkboxes.get("thresholdValue"), c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        panels.get("mainPage").add(textFields.get("thresholdField"), c);

        if (selectedFiles.size() <= 0) {
            textAreas.get("selectedFiles").setText(" Selected Files:\n");
        }
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        panels.get("mainPage").add(scrollPanes.get("selectedFiles"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        panels.get("mainPage").add(buttons.get("helpToggle"), c);

        frame.setContentPane(panels.get("mainPage"));
    }

    // Load the file selection page
    void loadFileSelectionPage() {
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
        if (usingThreshold && numOfOverThreshold > 0) {
            thresholdString = "Files over Threshold (" + numOfOverThreshold + ")";
        }
        buttons.get("filesOverThreshold").setText(thresholdString);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        panels.get("fileSelection").add(buttons.get("withImages"), c);
        String withImagesString = "Files with images (None)";
        if (filesWithImages.size() > 0) {
            withImagesString = "Files with images (" + filesWithImages.size() + ")";
        }
        buttons.get("withImages").setText(withImagesString);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        panels.get("fileSelection").add(buttons.get("fileRatios"), c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        for (int i = 0; i < fileSelectionPanels.length; i++) {
            panels.get("fileSelectionInner").add(fileSelectionPanels[i]);
        }
        panels.get("fileSelection").add(scrollPanes.get("fileSelection"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        panels.get("fileSelection").add(buttons.get("helpToggle"), c);

        frame.setContentPane(panels.get("fileSelection"));
    }

    // Load the file similarity page for a specific file as indicated by the index
    void loadFileSimilarity(int i) {
        panels.get("fileSimilarity").removeAll();

        textAreas.get("fileSimilarity").setText("");
        for (int j = 0; j < selectedFiles.size(); j++) {
            if (!ignoredFiles.contains(j)) {
                String toPrint = "";
                if (i != j)
                {
                    toPrint = "Similarity with " + selectedFiles.get(j).getName() + ":\n"
                        + "Path: " + selectedFiles.get(j).getAbsolutePath() + "\n"
                        + String.format("%.3f", fileSimilarities[i][j]) + "\n\n";
                }
                if (usingThreshold) {
                    if (fileSimilarities[i][j] >= thresholdValue && i != j) {
                        toPrint = "*** Similarity with " + selectedFiles.get(j).getName() + ": ***\n"
                                + "Path: " + selectedFiles.get(j).getAbsolutePath() + "\n"
                                + String.format("%.3f", fileSimilarities[i][j]) + "\n\n";
                    }
                }
                textAreas.get("fileSimilarity").append(toPrint);
            }
        }
        labels.get("fileSimilarity").setText("Path: " + selectedFiles.get(i));

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.ipadx = 10;
        panels.get("fileSimilarity").add(buttons.get("fileSimilarityBack"), c);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        panels.get("fileSimilarity").add(labels.get("fileSimilarity"), c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        panels.get("fileSimilarity").add(scrollPanes.get("fileSimilarity"), c);
        textAreas.get("fileSimilarity").setSelectionStart(0);
        textAreas.get("fileSimilarity").setSelectionEnd(0);
        frame.setContentPane(panels.get("fileSimilarity"));
    }

    // Loads the panel that displays the file pairs with similarities over the
    // thesholdValue.
    void loadOverThresholdPanel() {
        panels.get("overThreshold").removeAll();
        panels.get("overThreshold").add(buttons.get("overThresholdBack"));
        textAreas.get("overThreshold").setText("");
        for (int i = 0; i < numOfOverThreshold; i++) {
            if (!ignoredFiles.contains(overThresholdSimilarities[i][0]) && !ignoredFiles.contains(overThresholdSimilarities[i][1])) {
                File file1 = selectedFiles.get(overThresholdSimilarities[i][0]);
                File file2 = selectedFiles.get(overThresholdSimilarities[i][1]);
                textAreas.get("overThreshold").append(file1.getName() + " vs. " + file2.getName()
                        + "\n" + file1.getName() + " path: " + file1.getAbsolutePath()
                        + "\n" + file2.getName() + " path: " + file2.getAbsolutePath()
                        + "\n"
                        + String.format("%.3f",
                                fileSimilarities[overThresholdSimilarities[i][0]][overThresholdSimilarities[i][1]])
                        + "\n\n");
            }
        }
        panels.get("overThreshold").add(scrollPanes.get("overThreshold"));
        textAreas.get("overThreshold").setSelectionStart(0);
        textAreas.get("overThreshold").setSelectionEnd(0);
        frame.setContentPane(panels.get("overThreshold"));
    }

    // Loads the panel that displays the files that have images
    void loadWithImagesPanel() {
        panels.get("withImages").removeAll();
        panels.get("withImages").add(buttons.get("withImagesBack"));
        textAreas.get("withImages").setText("");
        for (int i = 0; i < filesWithImages.size(); i++) {
            boolean canPrint = true;
            for (int j = 0; j < ignoredFiles.size(); j++) {
                int ignoredFileIndex = ignoredFiles.get(j);
                if (selectedFiles.get(ignoredFileIndex) == filesWithImages.get(i)) {
                    canPrint = false;
                }
            }
            if (canPrint) {
                textAreas.get("withImages").append(filesWithImages.get(i).getName() + "\n"
                + "Path: " + filesWithImages.get(i).getAbsolutePath() + "\n\n");
            }
        }
        panels.get("withImages").add(scrollPanes.get("withImages"));
        textAreas.get("withImages").setSelectionStart(0);
        textAreas.get("withImages").setSelectionEnd(0);
        frame.setContentPane(panels.get("withImages"));
    }

    // Loads the loading screen panel that indicates what the application is doing
    // while reading the files
    void loadLoadingPanel() {
        panels.get("loading").removeAll();
        
        progressBars.get("loading").setMaximum(selectedFiles.size());
        progressBars.get("loading").setIndeterminate(false);
        progressBars.get("loading").setValue(0);
        panels.get("loading").add(progressBars.get("loading"));
        
        textAreas.get("loading").setText("");
        panels.get("loading").add(scrollPanes.get("loading"));

        frame.setContentPane(panels.get("loading"));
    }

    // loads the file ratios panel that shows the character to word ratio of each file
    void loadFileRatios() {
        panels.get("fileRatios").removeAll();

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        panels.get("fileRatios").add(buttons.get("fileRatiosBack"), c);

        textAreas.get("fileRatios").setText("");
        for (int i = 0; i < characterToPageRatios.size(); i++) {
            if (!ignoredFiles.contains(characterToPageRatios.get(i).fileIndex)) {
                textAreas.get("fileRatios").append(selectedFiles.get(characterToPageRatios.get(i).fileIndex).getName()
                    + "\n" + selectedFiles.get(characterToPageRatios.get(i).fileIndex).getAbsolutePath()
                    + "\n" + String.format("%.2f", characterToPageRatios.get(i).ratio) + "\n\n");
            }
        }
        c.gridx = 1;
        c.gridy = 0;
        panels.get("fileRatios").add(scrollPanes.get("fileRatios"), c);
        textAreas.get("fileRatios").setSelectionStart(0);
        textAreas.get("fileRatios").setSelectionEnd(0);

        frame.setContentPane(panels.get("fileRatios"));
    }

    // Calculate the similarities for all files
    void calculateSimilarities() {
        numOfOverThreshold = 0;
        Checker checker = new Checker();
        if (needToCalculate) {
            characterToPageRatios.clear();
            fileSimilarities = new double[selectedFiles.size()][selectedFiles.size()];
            textAreas.get("loading").append(String.format("Reading %d files:\n\n", selectedFiles.size()));
            fileTexts = new String[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++) {
                textAreas.get("loading").append(String.format("Reading %s\n", selectedFiles.get(i).getName()));
                fileTexts[i] = checker.readPDFDocument(selectedFiles.get(i));
                progressBars.get("loading").setValue(i);
            }

            textAreas.get("loading").append("\nGetting Word Frequency Maps.\n");
            progressBars.get("loading").setIndeterminate(true);

            HashMap<String, Integer> newWordFrequencies[] = new HashMap[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++) {
                newWordFrequencies[i] = checker.getFrequencyMapPDF(fileTexts[i]);
            }

            fileWordFrequencies = newWordFrequencies;

            textAreas.get("loading").append("Calculating Similarities\n");

            for (int i = 0; i < selectedFiles.size(); i++) {
                for (int j = 0; j < selectedFiles.size(); j++) {
                    fileSimilarities[i][j] = checker.getSimilarity(fileWordFrequencies[i], fileWordFrequencies[j]);
                }
            }

            textAreas.get("loading").append("Calculating character to page ratios.\n");

            for (int i = 0; i < selectedFiles.size(); i++) {
                Scanner scanner = new Scanner(fileTexts[i]);
                while (scanner.hasNext()) {
                    scanner.next();
                }
                scanner.close();
                FileRatio newRatio = new FileRatio(fileTexts[i].length() / checker.getPDFPageCount(selectedFiles.get(i)), i);
                characterToPageRatios.add(newRatio);
            }

            textAreas.get("loading").append("Sorting ratios.\n");

            sortRatios();
        }

        if (usingThreshold) {
            textAreas.get("loading").append("Finding file similarities over threshold.\n");
            fillOverThresholdSimilarities();
            textAreas.get("loading").append("Sorting file similarities over threshold.\n");
            sortOverThresholdSimilarities();
        }
    }

    // Create the buttons for the selected files
    void createFileButtons() {
        if (needToCalculate) {
            fileButtons = new JButton[selectedFiles.size()];
            fileLabels = new JLabel[selectedFiles.size()];
            fileSelectionPanels = new JPanel[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++) {
                fileButtons[i] = new JButton(selectedFiles.get(i).getName());
                fileButtons[i].addActionListener(this);
                fileButtons[i].setPreferredSize(new Dimension(300, 20));
                fileLabels[i] = new JLabel(selectedFiles.get(i).getAbsolutePath());
                fileSelectionPanels[i] = new JPanel(new GridLayout(0, 1));
                fileSelectionPanels[i].add(fileLabels[i]);
                fileSelectionPanels[i].add(fileButtons[i]);
            }
        }
    }

    // Retrieves the similarities that are over the threshold in fileSimilarities,
    // and put the pairs of indices into
    // overThresholdSimilarities
    void fillOverThresholdSimilarities() {
        if (usingThreshold) {
            for (int i = 0; i < selectedFiles.size(); i++) {
                for (int j = i; j < selectedFiles.size(); j++) {
                    if (fileSimilarities[i][j] >= thresholdValue && i != j) {
                        numOfOverThreshold++;
                    }
                }
            }
            overThresholdSimilarities = new int[numOfOverThreshold][2];

            int overThresholdIndex = 0;
            for (int i = 0; i < selectedFiles.size(); i++) {
                for (int j = i; j < selectedFiles.size(); j++) {
                    if (fileSimilarities[i][j] >= thresholdValue && i != j) {
                        overThresholdSimilarities[overThresholdIndex][0] = i;
                        overThresholdSimilarities[overThresholdIndex][1] = j;
                        overThresholdIndex++;
                    }
                }
            }
        }
    }

    // Switch the 2 file index pairs in overThresholdSimilarities.
    void switchOverThresholdSimilarities(int a, int b) {
        if (a < overThresholdSimilarities.length && b < overThresholdSimilarities.length) {
            int[] temp = overThresholdSimilarities[a];
            overThresholdSimilarities[a] = overThresholdSimilarities[b];
            overThresholdSimilarities[b] = temp;
        }
    }

    // Sort the overthresholdSimilarities array such that the similarities of the
    // two file indices are in descending order.
    void sortOverThresholdSimilarities() {
        if (overThresholdSimilarities.length > 0) {
            for (int i = 1; i < overThresholdSimilarities.length; i++) {
                int currentIndex = i;
                double similarityValue = fileSimilarities[overThresholdSimilarities[i][0]][overThresholdSimilarities[i][1]];
                boolean isGreaterThanPrev = true;
                while (currentIndex > 0 && isGreaterThanPrev) {
                    double prevSimilarity = fileSimilarities[overThresholdSimilarities[currentIndex
                            - 1][0]][overThresholdSimilarities[currentIndex - 1][1]];
                    if (prevSimilarity < similarityValue) {
                        switchOverThresholdSimilarities(currentIndex - 1, currentIndex);
                        currentIndex--;
                    } else {
                        isGreaterThanPrev = false;
                    }
                }
            }
        }
    }

    // switch the two fileRatio objects in the characterToPageRatio List, the indices of which are indicated by the parameters
    void switchRatio(int a, int b) {
        if (a < characterToPageRatios.size() && b < characterToPageRatios.size()) {
            FileRatio temp = characterToPageRatios.get(a);
            characterToPageRatios.set(a, characterToPageRatios.get(b));
            characterToPageRatios.set(b, temp);
        }
    }

    // Sort the characterToPageRatios list such that the ratios are in descending order.
    void sortRatios() {
        if (characterToPageRatios.size() > 0) {
            for (int i = 1; i < characterToPageRatios.size(); i++) {
                int currentIndex = i;
                double currentRatio = characterToPageRatios.get(i).ratio;
                boolean isGreaterThanPrev = true;
                while (currentIndex > 0 && isGreaterThanPrev) {
                    double prevRatio = characterToPageRatios.get(currentIndex - 1).ratio;
                    if (prevRatio < currentRatio) {
                        switchRatio(currentIndex, currentIndex - 1);
                        currentIndex--;
                    } else {
                        isGreaterThanPrev = false;
                    }
                }
            }
        }
    }

    // Recursive function where it adds pdf files to selected files. If the file
    // it's looking at is a directory, it calls itself
    // Otherwise, it will add the file to the selected files
    void addToSelectedPDF(File file) {
        for (File currentFile : file.listFiles()) {
            if (currentFile.isDirectory()) {
                addToSelectedPDF(currentFile);
            } else if (currentFile.getName().endsWith(".pdf")) {
                selectedFiles.add(currentFile);
            }
        }
    }

    // Check the selectedFiles to see which has images. If it does, add them to the
    // filesWithImages List
    void checkForImages() throws IOException {
        if (needToCalculate) {
            filesWithImages.clear();
            ImageChecker checker;
            try {
                checker = new ImageChecker();
            } catch (IOException e) {
                System.out.printf("Unable to open Image Checker\n");
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < selectedFiles.size(); i++) {
                checker.loadFile(selectedFiles.get(i));
                checker.processFile();
                if (checker.getNumOfImages() > 0) {
                    filesWithImages.add(selectedFiles.get(i));
                }
            }
        }
    }

    // Function to do things in loading screen
    void loadingScreen() {
        if (selectedFiles.size() > 1) {
            calculateSimilarities();
            try {
                textAreas.get("loading").append("Checking for images.\n");
                checkForImages();
            } catch (IOException e1) {
                System.out.printf("Error while checking images\n");
                e1.printStackTrace();
            }
            createFileButtons();
            needToCalculate = false;

            if (usingD2LDuplicates) {
                getDuplicates();
            }

            loadFileSelectionPage();
            frame.pack();
            frame.validate();
            isInFileSelection = true;
        }
        isReadingFiles = false;
    }

    // Function to go through the selected files and choose which are duplicates, using the latest submission
    void getDuplicates() {
        List<String> submissionIds = new ArrayList<String>();
        Pattern pattern = Pattern.compile("[0-9]+-[0-9]+ ", Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < selectedFiles.size(); i++) {
            Matcher matcher = pattern.matcher(selectedFiles.get(i).getAbsolutePath());
            boolean matchFound = matcher.find();
            if (matchFound) {
                submissionIds.add(selectedFiles.get(i).getAbsolutePath().substring(matcher.start(), matcher.end() - 1));
            }
            else {
                submissionIds.add("null");
            }
            // System.out.println(submissionIds.get(i));
        }

        List<FileId> duplicates = new ArrayList<FileId>();
        List<String> repeatedIds = new ArrayList<String>();
        for (int i = 0; i < submissionIds.size(); i++) {
            if (repeatedIds.contains(submissionIds.get(i))) {
                for (int j = 0; j < duplicates.size(); j++) {
                    if (duplicates.get(j).id.compareTo(submissionIds.get(i)) == 0) {
                        duplicates.get(j).add(i);
                    }
                }
            }
            else {
                duplicates.add(new FileId(submissionIds.get(i), i));
                repeatedIds.add(submissionIds.get(i));
            }
        }

        // The following code just prints the id along with the indices in selectedFiles that use it
        for (int i = 0; i < repeatedIds.size(); i++) {
            System.out.println(repeatedIds.get(i));
            FileId duplicate = null;
            for (int j = 0; j < duplicates.size(); j++) {
                if (duplicates.get(j).id.compareTo(repeatedIds.get(i)) == 0) {
                    duplicate = duplicates.get(j);
                }
            }

            if (duplicate != null) {
                for (int j = 0; j < duplicate.indices.size(); j++) {
                    System.out.printf("%d ", duplicate.indices.get(j));
                }
                System.out.println();
            }
        }

        ignoredFiles.clear();
        for (int i = 0; i < duplicates.size(); i++) {
            if (duplicates.get(i).id.compareTo("null") != 0) {
                List<Integer> currentIndices = duplicates.get(i).indices;
                if (currentIndices.size() > 1) {
                    ignoredFiles.add(currentIndices.get(currentIndices.size() - 1));
                }
            }
        }

        for (int i = 0; i < ignoredFiles.size(); i++) {
            System.out.printf("%d ", ignoredFiles.get(i));
        }
        System.out.println();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int fileButtonSelected = -1;
        if (isInFileSelection) {
            for (int i = 0; i < selectedFiles.size(); i++) {
                if (e.getSource() == fileButtons[i]) {
                    fileButtonSelected = i;
                    break;
                }
            }
        }

        if (e.getSource() == buttons.get("openFile")) // User pressed "Open File(s) button on main page"
        {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This is for selecting individual pdf files to compare.", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser(Path.of("").toAbsolutePath().toString(),
                    FileSystemView.getFileSystemView());

            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setDialogTitle("Select a .pdf file");

            FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .pdf files", "pdf");
            fileChooser.addChoosableFileFilter(restrict);

            int selectionReturn = fileChooser.showOpenDialog(null);

            if (selectionReturn == JFileChooser.APPROVE_OPTION) {
                selectedFiles.clear();
                for (File file : fileChooser.getSelectedFiles()) {
                    selectedFiles.add(file);
                }

                Collections.sort(selectedFiles, new SortByFilePath());

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected " + selectedFiles.size() + " Files:\n");

                while (i < selectedFiles.size()) {
                    textAreas.get("selectedFiles").append("\n   * " + selectedFiles.get(i).getName()
                    + "\n          Path: " + selectedFiles.get(i).getPath() + "\n");
                    i++;
                }

                textAreas.get("selectedFiles").setSelectionStart(0);
                textAreas.get("selectedFiles").setSelectionEnd(0);

                needToCalculate = true;
            }
            System.out.printf("%d files\n", selectedFiles.size());

            needToCalculate = true;
        } else if (e.getSource() == buttons.get("openFolder")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This is for selecting directories that contain the pdf files to compare.", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser(Path.of("").toAbsolutePath().toString(),
                    FileSystemView.getFileSystemView());

            fileChooser.setDialogTitle("Select a folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int selectionReturn = fileChooser.showOpenDialog(null);

            if (selectionReturn == JFileChooser.APPROVE_OPTION) {
                selectedFiles.clear();
                File directory = new File(fileChooser.getSelectedFile().getAbsolutePath());
                addToSelectedPDF(directory);

                Collections.sort(selectedFiles, new SortByFilePath());

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected " + selectedFiles.size() + " Files:\n");

                while (i < selectedFiles.size()) {
                    textAreas.get("selectedFiles").append("\n   * " + selectedFiles.get(i).getName()
                    + "\n          Path: " + selectedFiles.get(i).getPath() + "\n");
                    i++;
                }

                textAreas.get("selectedFiles").setSelectionStart(0);
                textAreas.get("selectedFiles").setSelectionEnd(0);
            }
            System.out.printf("%d files\n", selectedFiles.size());

            needToCalculate = true;
        } else if (e.getSource() == buttons.get("checkPlagiarism")) // User pressed check plagiarism button
        {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This initiates the reading of pdf files and calculations.", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            usingThreshold = checkboxes.get("thresholdValue").isSelected();
            if (usingThreshold) {
                try {
                    thresholdValue = Float.valueOf(textFields.get("thresholdField").getText());
                } catch (NumberFormatException n) {
                    JOptionPane.showMessageDialog(frame, "Enter a valid number for threshold", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    isReadingFiles = false;
                    return;
                }
            }

            if (selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select PDF files to check", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                isReadingFiles = false;
                return;
            }

            usingD2LDuplicates = checkboxes.get("duplicates").isSelected();
            isReadingFiles = true;
        } else if (e.getSource() == buttons.get("fileSelectionBack")) // user clicked back button on file selection
                                                                      // screen
        {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This returns to the main menu.", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadMainPage();
            isInFileSelection = false;
        } else if (e.getSource() == buttons.get("filesOverThreshold")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "If you had checked the \"Plagiarism Threshold\" box in the main menu, this will lists all the pdf files with a similarity score above the threshold value you inputted.", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (usingThreshold) {
                loadOverThresholdPanel();
            }
        } else if (e.getSource() == buttons.get("overThresholdBack")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This returns to the file selection screen", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadFileSelectionPage();
        } else if (fileButtonSelected >= 0) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "These buttons, listed alphabetically in descending order, will open a similarity profile for the file corresponding to the name shown on the button", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadFileSimilarity(fileButtonSelected);
            isInFileSelection = false;
        } else if (e.getSource() == buttons.get("fileSimilarityBack")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This returns to the file selection screen", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadFileSelectionPage();
            isInFileSelection = true;
        } else if (e.getSource() == buttons.get("withImages")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This lists the pdf files that contain images in alphabetical order.", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadWithImagesPanel();
            isInFileSelection = false;
        } else if (e.getSource() == buttons.get("withImagesBack")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This returns to the file selection screen", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadFileSelectionPage();
            isInFileSelection = true;
        } else if (e.getSource() == buttons.get("fileRatios")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This lists the character-to-page ratio for all pdf files in descending order", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadFileRatios();
            isInFileSelection = false;
        } else if (e.getSource() == buttons.get("fileRatiosBack")) {
            if (helpMode) {
                JOptionPane.showMessageDialog(frame, "This returns to the file selection screen", 
                "HELP", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadFileSelectionPage();
            isInFileSelection = true;
        } else if (e.getSource() == buttons.get("helpToggle")) {
            helpMode = helpMode == false;
            if (helpMode) {
                buttons.get("helpToggle").setText("Help Mode: On");
            } else {
                buttons.get("helpToggle").setText("Help Mode: Off");
            }
        }

        if (isReadingFiles) {
            loadLoadingPanel();
            Thread loading = new Thread() {
                public void run() {
                    loadingScreen();
                }
            };
            loading.start();
        }

        frame.pack();
        frame.validate();
    }
}
