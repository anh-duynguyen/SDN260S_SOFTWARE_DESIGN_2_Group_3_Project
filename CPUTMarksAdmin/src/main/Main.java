package main;

import classes.GUI;
import javax.swing.SwingUtilities;

/**
 * Starts the Marks Administration System. The screens live in classes/GUI.java,
 * so this file only has to open the window.
 */
public class Main {

    public static void main(String[] args) {
        // Swing windows should be built on the event dispatch thread, so I use
        // invokeLater here instead of calling new GUI() straight from main.
        // Doing it this way avoids odd freezing/painting bugs later on.
        SwingUtilities.invokeLater(() -> {
            GUI window = new GUI();
            window.setVisible(true);
        });
    }
}