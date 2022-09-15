package userInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class UI implements ActionListener{

    private HashMap<String, JPanel> panels;
    private HashMap<String, JButton> buttons;
    private HashMap<String, JTextArea> textAreas;

    private JFrame frame;
    
    public UI() {
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
        buttons.put("checkPlagiarism", new JButton("Check Plagiarism"));

        // Initialize textAreas *****************************************************************************

        // text areas for mainPage
        textAreas.put("selectedFiles", new JTextArea(25, 100));


        loadMainPage();
        frame.pack();
        frame.validate();
    }

    // Functions

    // Load main page
    void loadMainPage()
    {
        panels.get("mainPage").add(buttons.get("openFile"), BorderLayout.NORTH);
        textAreas.get("selectedFiles").setText("Selected Files:\n\n");
        textAreas.get("selectedFiles").append("None");
        panels.get("mainPage").add(textAreas.get("selectedFiles"), BorderLayout.CENTER);
        panels.get("mainPage").add(buttons.get("checkPlagiarism"), BorderLayout.SOUTH);
        frame.setContentPane(panels.get("mainPage"));
        frame.validate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }
}
