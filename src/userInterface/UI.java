package userInterface;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import plagiarismAlgorithm.*;

public class UI implements ActionListener{

    // Data members used to return information
    private File[] selectedFiles;
    private double[][] fileSimilarities;

    // Components that are graphically displayed
    private HashMap<String, JPanel> panels;
    private HashMap<String, JButton> buttons;
    private HashMap<String, JTextArea> textAreas;
    private JButton[] fileButtons;

    private JFrame frame;
    
    public UI() {
        // Initialize information data members
        selectedFiles = new File[0];

        frame = new JFrame("Plagiarism Checker");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panels = new HashMap<String, JPanel>();
        buttons = new HashMap<String, JButton>();
        textAreas = new HashMap<String, JTextArea>();

        // Initialize JPanels *******************************************************************************
        panels.put("mainPage", new JPanel(new BorderLayout()));
        panels.put("fileSelection", new JPanel(new FlowLayout()));

        // Initialize Buttons *******************************************************************************

        // Buttons for mainPage
        buttons.put("openFile", new JButton("Open File(s)"));
        buttons.get("openFile").addActionListener(this);
        buttons.put("checkPlagiarism", new JButton("Check Plagiarism"));
        buttons.get("checkPlagiarism").addActionListener(this);
        buttons.put("fileSelectionBack", new JButton("Back"));
        buttons.get("fileSelectionBack").addActionListener(this);

        // Initialize textAreas *****************************************************************************

        // text areas for mainPage
        textAreas.put("selectedFiles", new JTextArea(25, 100));
        textAreas.get("selectedFiles").setEditable(false);


        loadMainPage();
        frame.pack();
        frame.validate();
    }

    // Functions ********************************************************************************************************************

    // Load main page
    void loadMainPage()
    {
        panels.get("mainPage").add(buttons.get("openFile"), BorderLayout.NORTH);
        if (selectedFiles.length <= 0)
        {
            textAreas.get("selectedFiles").setText(" Selected Files:\n");
        }
        panels.get("mainPage").add(textAreas.get("selectedFiles"), BorderLayout.CENTER);
        panels.get("mainPage").add(buttons.get("checkPlagiarism"), BorderLayout.SOUTH);
        frame.setContentPane(panels.get("mainPage"));
    }

    // Load the file selection page
    void loadFileSelectionPage()
    {
        for (int i = 0; i < fileButtons.length; i++)
        {
            panels.get("fileSelection").add(fileButtons[i]);
        }
        panels.get("fileSelection").add(buttons.get("fileSelectionBack"));
        frame.setContentPane(panels.get("fileSelection"));
    }

    // Calculate the similarities for all files
    void calculateSimilarities()
    {
        fileSimilarities = new double[selectedFiles.length][selectedFiles.length];
        Checker checker = new Checker();
        for (int i = 0; i < selectedFiles.length; i++)
        {
            for (int j = 0; j < selectedFiles.length; j++)
            {
                try {
                    fileSimilarities[i][j] = checker.getSimilarityPDF(selectedFiles[i], selectedFiles[j]);
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
        fileButtons = new JButton[selectedFiles.length];
        for (int i = 0; i < selectedFiles.length; i++)
        {
            fileButtons[i] = new JButton(selectedFiles[i].getName());
            fileButtons[i].addActionListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
                selectedFiles = fileChooser.getSelectedFiles();

                int i = 0;
                textAreas.get("selectedFiles").setText(" Selected Files:\n");

                while (i < selectedFiles.length)
                {
                    textAreas.get("selectedFiles").append("\n     " + selectedFiles[i].getName());
                    i++;
                }
            }
        }
        else if (e.getSource() == buttons.get("checkPlagiarism"))
        {
            calculateSimilarities();
            for (int i = 0; i < selectedFiles.length; i++)
            {
                for (int j = 0; j < selectedFiles.length; j++)
                {
                    System.out.print(fileSimilarities[i][j] + " ");
                }
                System.out.println();
            }
            if (selectedFiles.length > 1)
            {
                createFileButtons();
                loadFileSelectionPage();
            }
        }
        else if (e.getSource() == buttons.get("fileSelectionBack"))
        {
            panels.get("fileSelection").removeAll();
            loadMainPage();
        }
        
        frame.validate();
    }
}
