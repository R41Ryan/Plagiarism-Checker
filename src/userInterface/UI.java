package userInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UI implements ActionListener{

    private JFrame frame;
    
    public UI() {
        frame = new JFrame("Plagiarism Checker");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(new JButton("Button"));
        frame.pack();
        frame.validate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }
}
