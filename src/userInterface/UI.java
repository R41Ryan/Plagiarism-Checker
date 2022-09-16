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

    // Components that are graphically displayed
    private HashMap<String, JPanel> panels;
    private HashMap<String, JButton> buttons;
    private HashMap<String, JTextArea> textAreas;

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

        // Initialize Buttons *******************************************************************************

        // Buttons for mainPage
        buttons.put("openFile", new JButton("Open File(s)"));
        buttons.get("openFile").addActionListener(this);
        buttons.put("checkPlagiarism", new JButton("Check Plagiarism"));
        buttons.get("checkPlagiarism").addActionListener(this);

        // Initialize textAreas *****************************************************************************

        // text areas for mainPage
        textAreas.put("selectedFiles", new JTextArea(25, 100));
        textAreas.get("selectedFiles").setEditable(false);


        loadMainPage();
        frame.pack();
        frame.validate();
    }

    // Functions

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
        frame.validate();
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
        
        frame.validate();
    }
}
