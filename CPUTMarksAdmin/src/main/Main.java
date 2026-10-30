package main;

import classes.CourseSessionCoordinator;
import classes.GUI; 
import javax.swing.SwingUtilities;

/**
 * Starts the Marks Administration System.
 */
public class Main {

    public static void main(String[] args) {
        CourseSessionCoordinator sessionCoordinator = new CourseSessionCoordinator();

        SwingUtilities.invokeLater(() -> {
            GUI window = new GUI(sessionCoordinator);
            window.setVisible(true);
        });
    }
}
