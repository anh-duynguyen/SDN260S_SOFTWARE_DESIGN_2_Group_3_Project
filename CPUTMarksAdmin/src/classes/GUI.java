package classes;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Simple Swing GUI for the Marks Administration System.
 * Uses the same classes as the terminal Main, so all the marks logic stays in
 * those classes and this file only handles the screens and buttons.
 */
public class GUI extends JFrame {

    // data objects (same setup as Main.java)
    private Course course;
    private AssessmentComponent test;
    private AssessmentComponent practical;
    private AssessmentComponent exam;
    private StudentManager studentManager;
    private Database database;
    private Lecturer lecturer;
    private HeadOfDepartment hod;
    private ArrayList<Course> courses;

    // whoever picked a role on the first screen
    private User currentUser;

    // GUI parts that more than one method needs to reach
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JTextArea outputArea;

    public GUI() {
        setTitle("CPUT Marks Administration System");
        setSize(950, 520);
        setLocationRelativeTo(null); // opens in the middle of the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setupData();
        showLoginScreen();
    }

    // ---------------------------------------------------------------
    // setting up the data
    // ---------------------------------------------------------------

    private void setupData() {
        course = new Course("SDN260S", "Software Design 2");

        // weights add up to 100 so the final mark comes out as a percentage
        test = new AssessmentComponent("test", "Test", 30, 100);
        practical = new AssessmentComponent("practical", "Practical", 30, 100);
        exam = new AssessmentComponent("exam", "Exam", 40, 100);
        course.addComponent(test);
        course.addComponent(practical);
        course.addComponent(exam);

        studentManager = new StudentManager();
        database = new Database("src/data/marks.csv");

        try {
            studentManager = database.loadData(test, practical, exam);
        } catch (Exception ex) {
            // if the file is broken we just start with an empty class
            JOptionPane.showMessageDialog(this, "Could not load the marks file: " + ex.getMessage());
        }

        // loadData only fills the StudentManager. The lecturer and HoD reports
        // use the Course's student list, so I add the same students to the Course
        // as well, otherwise those reports would be empty.
        for (Student s : studentManager.getStudents()) {
            course.addStudent(s);
        }

        lecturer = new Lecturer("Lecturer", course, studentManager, database);

        courses = new ArrayList<>();
        courses.add(course);
        hod = new HeadOfDepartment("Head of Department", courses);
    }

    // ---------------------------------------------------------------
    // screen 1: choose a role
    // ---------------------------------------------------------------

    private void showLoginScreen() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 250, 30, 250));

        // logo goes at the top, centred
        JLabel logoLabel = new JLabel(loadLogo(), SwingConstants.CENTER);

        // everything under the logo stays in its own grid like before
        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel title = new JLabel("Marks Administration System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel(course.getCode() + " - " + course.getName(), SwingConstants.CENTER);

        JButton lecturerButton = new JButton("Login as Lecturer");
        JButton hodButton = new JButton("Login as Head of Department");

        lecturerButton.addActionListener(e -> showLecturerScreen());
        hodButton.addActionListener(e -> showHodScreen());

        center.add(title);
        center.add(subtitle);
        center.add(lecturerButton);
        center.add(hodButton);

        panel.add(logoLabel, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        setScreen(panel);
    }

    // loads the CPUT logo from the images folder (same level as the data folder).
    // The original picture is big, so I scale it down to fit the login screen.
    private ImageIcon loadLogo() {
        ImageIcon icon = new ImageIcon("src/images/cput-logo.png");

        // if the file is missing the icon has no width, so return null and the
        // login screen just shows without a logo instead of crashing
        if (icon.getIconWidth() <= 0) {
            return null;
        }

        Image scaled = icon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // swaps whatever is on the window for a new panel
    private void setScreen(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        revalidate();
        repaint();
    }

    // ---------------------------------------------------------------
    // screen 2: lecturer (can capture and change marks)
    // ---------------------------------------------------------------

    private void showLecturerScreen() {
        currentUser = lecturer;

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top part: heading on the left, search on the right
        JPanel top = new JPanel(new BorderLayout());
        JLabel heading = new JLabel(currentUser.getRole() + " - " + course.getCode()
                + " " + course.getName());
        heading.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(10);
        JButton searchButton = new JButton("Search Student No");
        searchButton.addActionListener(e -> searchStudent());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        top.add(heading, BorderLayout.WEST);
        top.add(searchPanel, BorderLayout.EAST);

        // middle part: the table of students
        String[] columns = {"Student No", "Name", "Test", "Practical", "Exam",
            "Final %", "Symbol", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            // lecturers change marks with the button, not by typing in the cells,
            // because the button checks the marks are valid first
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        // rows with missing marks get a light red background so they stand out
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String status = t.getValueAt(row, 7).toString();
                if (!isSelected) {
                    if (status.startsWith("Missing")) {
                        c.setBackground(new Color(255, 210, 210));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        // bottom part: the buttons
        JPanel buttons = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Student");
        JButton marksButton = new JButton("Capture / Update Marks");
        JButton viewButton = new JButton("View Student Marks");
        JButton missingButton = new JButton("Missing Marks");
        JButton summaryButton = new JButton("Class Summary");
        JButton importButton = new JButton("Import CSV");
        JButton exportButton = new JButton("Export CSV");
        JButton backButton = new JButton("Log out");

        addButton.addActionListener(e -> addStudent());
        marksButton.addActionListener(e -> updateMarks());
        viewButton.addActionListener(e -> viewStudentMarks());
        missingButton.addActionListener(e -> showMissingMarks());
        summaryButton.addActionListener(e -> showLecturerSummary());
        importButton.addActionListener(e -> importCsv());
        exportButton.addActionListener(e -> exportCsv());
        backButton.addActionListener(e -> showLoginScreen());

        buttons.add(addButton);
        buttons.add(marksButton);
        buttons.add(viewButton);
        buttons.add(missingButton);
        buttons.add(summaryButton);
        buttons.add(importButton);
        buttons.add(exportButton);
        buttons.add(backButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        setScreen(panel);
        refreshTable();
    }

    // clears the table and fills it again from the StudentManager.
    // I call this after every change so the table always matches the data.
    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Student s : studentManager.getStudents()) {
            IndividualMarksManager m = s.getMarks();

            String finalMark = "--";
            String symbol = "--";
            String status = "Complete";

            // calculateFinalMark throws an error when a mark is missing,
            // so only call it when nothing is missing
            if (m.hasMissingMarks()) {
                status = "Missing: " + CollectiveMarksManager.describeMissing(s);
            } else {
                double f = m.calculateFinalMark();
                finalMark = String.format("%.1f", f);
                symbol = CollectiveMarksManager.letterGrade(f);
            }

            tableModel.addRow(new Object[]{s.getStudentID(), s.getName(),
                showMark(m.getTestMark()), showMark(m.getPracticalMark()),
                showMark(m.getExamMark()), finalMark, symbol, status});
        }
    }

    // ---------------------------------------------------------------
    // lecturer button actions
    // ---------------------------------------------------------------

    private void addStudent() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField testField = new JTextField();
        JTextField practicalField = new JTextField();
        JTextField examField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Student number:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Test mark (optional):"));
        form.add(testField);
        form.add(new JLabel("Practical mark (optional):"));
        form.add(practicalField);
        form.add(new JLabel("Exam mark (optional):"));
        form.add(examField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return; // they pressed cancel
        }

        try {
            Double t = readMark(testField.getText(), "Test");
            Double p = readMark(practicalField.getText(), "Practical");
            Double e = readMark(examField.getText(), "Exam");

            // Lecturer checks the id, name and mark ranges and throws a
            // message if something is wrong, so I just show that message
            lecturer.addStudent(idField.getText(), nameField.getText(), t, p, e);
            saveToFile();
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateMarks() {
        String id = getSelectedStudentID();
        if (id == null) {
            return;
        }

        // fill the boxes with the marks the student already has
        IndividualMarksManager m = studentManager.findStudentByID(id).getMarks();
        JTextField testField = new JTextField(markToText(m.getTestMark()));
        JTextField practicalField = new JTextField(markToText(m.getPracticalMark()));
        JTextField examField = new JTextField(markToText(m.getExamMark()));

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Test mark:"));
        form.add(testField);
        form.add(new JLabel("Practical mark:"));
        form.add(practicalField);
        form.add(new JLabel("Exam mark:"));
        form.add(examField);
        form.add(new JLabel("(empty box = keep as is)"));

        int result = JOptionPane.showConfirmDialog(this, form, "Marks for " + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Double t = readMark(testField.getText(), "Test");
            Double p = readMark(practicalField.getText(), "Practical");
            Double e = readMark(examField.getText(), "Exam");

            // addMarks leaves a mark alone when it gets null, and it checks
            // all the marks before changing any of them
            lecturer.addMarks(id, t, p, e);
            saveToFile();
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void viewStudentMarks() {
        String id = getSelectedStudentID();
        if (id == null) {
            return;
        }
        try {
            showText("Student Marks", lecturer.viewStudentMarks(id));
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    // finds the student using the number typed in the search box
    private void searchStudent() {
        String id = searchField.getText().trim();
        Student s = studentManager.findStudentByID(id);

        if (s == null) {
            showError("No student found with number " + id);
            return;
        }

        // highlight that student's row in the table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(id)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
            }
        }
    }

    // lists every student who still has a mark missing
    private void showMissingMarks() {
        ArrayList<Student> missing = lecturer.getStudentsWithMissingMarks();

        if (missing.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students have missing marks.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Student s : missing) {
            sb.append(s.getStudentID()).append("  ").append(s.getName())
                    .append("  - missing: ").append(CollectiveMarksManager.describeMissing(s))
                    .append("\n");
        }
        showText("Students With Missing Marks", sb.toString());
    }

    // the CollectiveMarksManager already builds the summary text, so I reuse it
    private void showLecturerSummary() {
        CollectiveMarksManager cmm = new CollectiveMarksManager(course.getStudents());
        showText("Class Summary", cmm.buildSummary());
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            // importMarks adds new students and updates existing ones.
            // If one row is bad it cancels the whole import
            int count = lecturer.importMarks(chooser.getSelectedFile().getAbsolutePath());
            saveToFile();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Imported " + count + " row(s).");
        } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        // add .csv if the lecturer forgot to type it
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        database.exportCSV(studentManager, file.toPath());
        JOptionPane.showMessageDialog(this, "Exported to " + file.getAbsolutePath());
    }

    // ---------------------------------------------------------------
    // screen 3: head of department (view only)
    // ---------------------------------------------------------------

    private void showHodScreen() {
        currentUser = hod;

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // the HoD class says it cannot edit marks, so there are no edit buttons
        // on this screen, only buttons that show reports
        String note = "";
        if (!hod.canEditMarks()) {
            note = " (view only)";
        }
        JLabel heading = new JLabel(currentUser.getRole() + note);
        heading.setFont(new Font("Arial", Font.BOLD, 18));

        // reports are shown as plain text, monospaced so the columns line up
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JPanel buttons = new JPanel(new FlowLayout());
        JButton resultsButton = new JButton("Module Results");
        JButton summaryButton = new JButton("Class Summary");
        JButton allButton = new JButton("All Modules");
        JTextField idField = new JTextField(10);
        JButton studentButton = new JButton("View Student No");
        JButton backButton = new JButton("Log out");

        resultsButton.addActionListener(e -> outputArea.setText(hod.viewModuleResults(course)));
        summaryButton.addActionListener(e -> outputArea.setText(hod.viewClassSummary(course)));
        allButton.addActionListener(e -> outputArea.setText(hod.viewAllModules(courses)));
        studentButton.addActionListener(e -> {
            try {
                outputArea.setText(hod.viewStudentMarks(course, idField.getText().trim()));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
        backButton.addActionListener(e -> showLoginScreen());

        buttons.add(resultsButton);
        buttons.add(summaryButton);
        buttons.add(allButton);
        buttons.add(idField);
        buttons.add(studentButton);
        buttons.add(backButton);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        setScreen(panel);
    }

    // ---------------------------------------------------------------
    // small helper methods
    // ---------------------------------------------------------------

    // gets the student number of the row selected in the table
    // (shows a message and returns null if nothing is selected)
    private String getSelectedStudentID() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Please select a student in the table first.");
            return null;
        }
        return tableModel.getValueAt(row, 0).toString();
    }

    // blank text means "no mark" (null), anything else must be a number.
    // The range check (0 to max) is left to the Lecturer class.
    private Double readMark(String text, String componentName) {
        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(componentName
                    + " mark must be a number, or left empty.");
        }
    }

    // text for the table, "--" shows the mark is still missing
    private String showMark(Double mark) {
        if (mark == null) {
            return "--";
        }
        return String.format("%.1f", mark);
    }

    // text for the edit boxes, empty when there is no mark yet
    private String markToText(Double mark) {
        if (mark == null) {
            return "";
        }
        return String.valueOf(mark);
    }

    // saves to the csv after every change so nothing gets lost
    private void saveToFile() {
        database.saveData(studentManager);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // shows longer text in a scrollable box instead of a tiny message dialog
    private void showText(String title, String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(450, 250));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.PLAIN_MESSAGE);
    }

    // starts the GUI. Swing should be started on its own thread like this
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}