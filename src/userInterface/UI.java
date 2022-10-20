package userInterface;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;

import plagiarismAlgorithm.*;

public class UI implements ActionListener{

    // Data members used to return information
    private List<File> selectedFiles;
    private double[][] fileSimilarities;

    // variables for various calculations
    boolean isInFileSelection;
    boolean usingThreshold;
    float thresholdValue;

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

        // Initialize Buttons *******************************************************************************

        // Buttons for mainPage
        buttons.put("openFile", new JButton("Open File(s)"));
        buttons.get("openFile").addActionListener(this);
        buttons.put("openFolder", new JButton("Open Folder"));
        buttons.get("openFolder").addActionListener(this);
        buttons.put("checkPlagiarism", new JButton("Check Plagiarism"));
        buttons.get("checkPlagiarism").addActionListener(this);
        buttons.put("fileSelectionBack", new JButton("Back"));
        buttons.get("fileSelectionBack").addActionListener(this);
        buttons.put("fileSimilarityBack", new JButton("Back"));
        buttons.get("fileSimilarityBack").addActionListener(this);

        // Initialize textAreas *****************************************************************************

        // text areas for mainPage
        textAreas.put("selectedFiles", new JTextArea(25, 25));
        textAreas.get("selectedFiles").setEditable(false);
        textAreas.get("selectedFiles").setLineWrap(true);

        // test areas for file similarity page
        textAreas.put("fileSimilarity", new JTextArea(25, 25));
        textAreas.get("fileSimilarity").setEditable(false);
        textAreas.get("fileSimilarity").setLineWrap(true);

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

        // scroll pane for file similarity page
        scrollPanes.put("fileSimilarity", new JScrollPane(textAreas.get("fileSimilarity")));
        scrollPanes.get("fileSimilarity").setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Initialize Labels ***************************************************************************************************

        // Initialize checkboxes ***********************************************************************************************
        checkboxes.put("thresholdValue", new JCheckBox("Plagiarism Threshold:"));

        loadMainPage();
        isInFileSelection = false;
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
        panels.get("fileSelection").add(buttons.get("fileSelectionBack"), c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        for (int i = 0; i < fileButtons.length; i++)
        {
            panels.get("fileSelectionInner").add(fileButtons[i]);
        }
        panels.get("fileSelection").add(panels.get("fileSelectionInner"), c);

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
                if (fileSimilarities[i][j] >= thresholdValue)
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

    // Calculate the similarities for all files
    void calculateSimilarities()
    {
        fileSimilarities = new double[selectedFiles.size()][selectedFiles.size()];
        Checker checker = new Checker();
        for (int i = 0; i < selectedFiles.size(); i++)
        {
            for (int j = 0; j < selectedFiles.size(); j++)
            {
                try {
                    fileSimilarities[i][j] = checker.getSimilarityPDF(selectedFiles.get(i), selectedFiles.get(j));
                } catch (IOException e) {
                    System.out.println("Error while calculating similarity\n");
                    e.printStackTrace();
                    return;
                } 
            }
        }
    }

    // Create the buttons for the selected files
    void createFileButtons()
    {
        fileButtons = new JButton[selectedFiles.size()];
        for (int i = 0; i < selectedFiles.size(); i++)
        {
            fileButtons[i] = new JButton(selectedFiles.get(i).getName());
            fileButtons[i].addActionListener(this);
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
                System.out.println(currentFile.getName() + " is a folder. Calling recursively. *************************");
                addToSelectedPDF(currentFile);
            }
            else if (currentFile.getName().endsWith(".pdf"))
            {
                System.out.println(currentFile.getName() + " is a file. Adding");
                selectedFiles.add(currentFile);
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
            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());

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

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected Files:\n");

                while (i < selectedFiles.size())
                {
                    textAreas.get("selectedFiles").append("\n     " + selectedFiles.get(i).getName());
                    i++;
                }
            }
        }
        else if (e.getSource() == buttons.get("openFolder"))
        {
            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());

            fileChooser.setDialogTitle("Select a folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int selectionReturn = fileChooser.showOpenDialog(null);

            if (selectionReturn == JFileChooser.APPROVE_OPTION)
            {
                selectedFiles.clear();
                File directory = new File(fileChooser.getSelectedFile().getAbsolutePath());
                addToSelectedPDF(directory);;

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected Files:\n");

                while (i < selectedFiles.size())
                {
                    textAreas.get("selectedFiles").append("\n     " + selectedFiles.get(i).getName());
                    i++;
                }
            }
        }
        else if (e.getSource() == buttons.get("checkPlagiarism")) // User pressed check plagiarism button
        {
            calculateSimilarities();
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
                usingThreshold = checkboxes.get("thresholdValue").isSelected();
                if (usingThreshold)
                {
                    thresholdValue = Float.valueOf(textFields.get("thresholdField").getText());
                }
                createFileButtons();
                loadFileSelectionPage();
                isInFileSelection = true;
            }
        }
        else if (e.getSource() == buttons.get("fileSelectionBack")) // user clicked back button on file selection screen
        {
            loadMainPage();
            isInFileSelection = false;
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
        
        frame.pack();
        frame.validate();
    }
}
