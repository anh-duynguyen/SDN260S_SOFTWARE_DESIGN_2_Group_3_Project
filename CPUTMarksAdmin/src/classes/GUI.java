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
import javax.swing.JComboBox;
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


public class GUI extends JFrame {

    private final CourseSessionCoordinator session;

    private Lecturer lecturer;
    private HeadOfDepartment hod;
    private User currentUser;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JTextArea outputArea;

    public GUI(CourseSessionCoordinator session) {
        this.session = session;

        setTitle("CPUT Marks Administration System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.hod = new HeadOfDepartment("Head of Department", session.getCourses());

        showLoginScreen();
    }

    private void showLoginScreen() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 250, 30, 250));

        JLabel logoLabel = new JLabel(loadLogo(), SwingConstants.CENTER);
        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel title = new JLabel("Marks Administration System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton lecturerButton = new JButton("Login as Lecturer");
        JButton hodButton = new JButton("Login as Head of Department");

        lecturerButton.addActionListener(e -> selectCourseAndLaunchLecturer());
        hodButton.addActionListener(e -> showHodScreen());

        center.add(title);
        center.add(lecturerButton);
        center.add(hodButton);

        panel.add(logoLabel, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        setScreen(panel);
    }

    private ImageIcon loadLogo() {
        ImageIcon icon = new ImageIcon("src/images/cput-logo.png");
        if (icon.getIconWidth() <= 0) {
            return null;
        }
        Image scaled = icon.getImage().getScaledInstance(170, 170, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void setScreen(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        revalidate();
        repaint();
    }

    private void selectCourseAndLaunchLecturer() {
        if (session.getCourses().isEmpty()) {
            showError("System State Failure: No courses initialized inside systemic registries.");
            return;
        }

        String[] courseOptions = new String[session.getCourses().size()];
        for (int i = 0;
                i < session.getCourses().size();
                i++) {
            courseOptions[i] = session.getCourses().get(i).getCode() + " - " + session.getCourses().get(i).getName();
        }

        JComboBox<String> selectionBox = new JComboBox<>(courseOptions);
        int choice = JOptionPane.showConfirmDialog(this, selectionBox, "Select Active Course Context",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.OK_OPTION && selectionBox.getSelectedIndex() != -1) {
            Course chosen = session.getCourses().get(selectionBox.getSelectedIndex());
            session.switchCourseContext(chosen);

            this.lecturer = new Lecturer("Lecturer", session.getCurrentCourse(), session.getCurrentStudentManager(), session.getCurrentDatabase());
            showLecturerScreen();
        }
    }

    private void showLecturerScreen() {
        currentUser = lecturer;

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JLabel heading = new JLabel(currentUser.getRole() + " - Course Profile Context: " + session.getCurrentCourse().getCode());
        heading.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(10);
        JButton searchButton = new JButton("Search Student No");
        searchButton.addActionListener(e -> searchStudent());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        top.add(heading, BorderLayout.WEST);
        top.add(searchPanel, BorderLayout.EAST);

        String[] columns = {"Student No", "Name", "Test", "Practical", "Exam", "Final %", "Symbol", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (t.getValueAt(row, 7) != null && !isSelected) {
                    String status = t.getValueAt(row, 7).toString();
                    c.setBackground(status.startsWith("Missing") ? new Color(255, 210, 210) : Color.WHITE);
                }
                return c;
            }
        });

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

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (session.getCurrentCourse() == null) {
            return;
        }

        for (Student s
                : session.getCurrentCourse().getStudents()) {
            IndividualMarksManager m = s.getMarks();
            String finalMark = "--", symbol = "--", status = "Complete";

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

        int result = JOptionPane.showConfirmDialog(this, form, "Register Student to " + session.getCurrentCourse().getCode(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Double t = readMark(testField.getText(), "Test");
            Double p = readMark(practicalField.getText(), "Practical");
            Double e = readMark(examField.getText(), "Exam");
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
        Student target = session.getCurrentStudentManager().findStudentByID(id);
        IndividualMarksManager m = target.getMarks();
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
        int result = JOptionPane.showConfirmDialog(this, form, "Modify Record Marks: " + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Double t = readMark(testField.getText(), "Test");
            Double p = readMark(practicalField.getText(), "Practical");
            Double e = readMark(examField.getText(), "Exam");
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
            showText("Student Profile Assessment Report", lecturer.viewStudentMarks(id));
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void searchStudent() {
        String id = searchField.getText().trim();
        Student s = session.getCurrentStudentManager().findStudentByID(id);
        if (s == null) {
            showError("No record found matching student sequence: " + id);
            return;
        }
        for (int i = 0;
                i < tableModel.getRowCount();
                i++) {
            if (tableModel.getValueAt(i, 0).equals(id)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
            }
        }
    }

    private void showMissingMarks() {
        // Ensure <Student> generic type is explicitly declared here:
        ArrayList<Student> missing = lecturer.getStudentsWithMissingMarks();

        if (missing.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No student profile entries contain missing values.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Student s : missing) {
            sb.append(s.getStudentID()).append("  ").append(s.getName())
              .append("  - missing items: ").append(CollectiveMarksManager.describeMissing(s))
              .append("\n");
        }
        showText("Outstanding Core Components Queue", sb.toString());
    }


    private void showLecturerSummary() {
        CollectiveMarksManager cmm = new CollectiveMarksManager(session.getCurrentCourse().getStudents());
        showText(session.getCurrentCourse().getCode() + " Statistical View Metrics Summary", cmm.buildSummary());
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            int count = lecturer.importMarks(chooser.getSelectedFile().getAbsolutePath());
            saveToFile();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Success: Imported " + count + " records cleanly.");
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
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
<<<<<<< HEAD
        session.getCurrentDatabase().exportCSVSem(session.getCurrentStudentManager(), file.toPath());
=======
        session.getCurrentDatabase().exportCSV(session.getCurrentStudentManager(), file.toPath());
>>>>>>> f1c27b15770d3fa365b1a2b07b09a48929e662e6
        JOptionPane.showMessageDialog(this, "Exported cleanly onto destination: " + file.getAbsolutePath());
    }

    private void showHodScreen() {
        currentUser = hod;
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String courseText = (session.getCurrentCourse() != null) ? " | Selected Module: " + session.getCurrentCourse().getCode() : " | No Module Selected";
        JLabel heading = new JLabel(currentUser.getRole() + " (Departmental Dashboard)" + courseText);
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JPanel buttons = new JPanel(new FlowLayout());
        JButton resultsButton = new JButton("Module Results");
        JButton summaryButton = new JButton("Class Summary");
        JButton allButton = new JButton("All Modules Summary");
        JButton addCourseButton = new JButton("Create Course");
        JButton removeCourseButton = new JButton("Remove Course");
        JTextField idField = new JTextField(10);
        JButton studentButton = new JButton("Find Student");
        JButton backButton = new JButton("Log out");
        resultsButton.addActionListener(e -> {
            if (session.getCurrentCourse() == null) {
                showError("Please choose a course first via Lecturer login context.");
                return;
            }
            outputArea.setText(hod.viewModuleResults(session.getCurrentCourse()));
        });
        summaryButton.addActionListener(e -> {
            if (session.getCurrentCourse() == null) {
                showError("No course context active. Choose a course profile via Lecturer mode first.");
                return;
            }
            outputArea.setText(hod.viewClassSummary(session.getCurrentCourse()));
        });
        allButton.addActionListener(e -> outputArea.setText(hod.viewAllModules(session.getCourses())));
        addCourseButton.addActionListener(e -> handleAddCourseByHod());
        removeCourseButton.addActionListener(e -> handleRemoveCourseByHod());
        studentButton.addActionListener(e -> {
            if (session.getCurrentCourse() == null) {
                showError("No active validation course workspace target defined.");
                return;
            }
            try {
                outputArea.setText(hod.viewStudentMarks(session.getCurrentCourse(), idField.getText().trim()));
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
        backButton.addActionListener(e -> showLoginScreen());
        buttons.add(resultsButton);
        buttons.add(summaryButton);
        buttons.add(allButton);
        buttons.add(addCourseButton);
        buttons.add(removeCourseButton);
        buttons.add(idField);
        buttons.add(studentButton);
        buttons.add(backButton);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        setScreen(panel);
        outputArea.setText(hod.viewAllModules(session.getCourses()));
    }

    private void handleAddCourseByHod() {
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JPanel dialogForm = new JPanel(new GridLayout(0, 2, 5, 5));
        dialogForm.add(new JLabel("Course Code (e.g. ITS210S):"));
        dialogForm.add(codeField);
        dialogForm.add(new JLabel("Course Name Description:"));
        dialogForm.add(nameField);
        int selectionResult = JOptionPane.showConfirmDialog(this, dialogForm, "Initialize New Module Record Context",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (selectionResult == JOptionPane.OK_OPTION) {
            String cleanCode = codeField.getText().trim().toUpperCase();
            String cleanName = nameField.getText().trim();
            if (cleanCode.isEmpty() || cleanName.isEmpty()) {
                showError("Validation Fault: Entries cannot be left blank.");
                return;
            }
            for (Course courseProfile
                    : session.getCourses()) {
                if (courseProfile.getCode().equalsIgnoreCase(cleanCode)) {
                    showError("Data Integrity Violation: Course Module code profile [" + cleanCode + "] is already mapped.");
                    return;
                }
            }
            Course customCourse = new Course(cleanCode, cleanName);
            customCourse.addComponent(new AssessmentComponent("test", "Test", 30, 100));
            customCourse.addComponent(new AssessmentComponent("practical", "Practical", 30, 100));
            customCourse.addComponent(new AssessmentComponent("exam", "Exam", 40, 100));
            session.getCourses().add(customCourse);
            String prospectivePath = "src/data/" + cleanCode + "_marks.csv";
            File preppedFile = new File(prospectivePath);
            try {
                preppedFile.getParentFile().mkdirs();
                if (!preppedFile.exists()) {
                    java.nio.file.Files.write(preppedFile.toPath(), java.util.List.of("StudentID, Name, T1, Practical, FISA"));
                }
            } catch (IOException ignored) {
            }
            JOptionPane.showMessageDialog(this, "Module operational registry mapping for [" + cleanCode + "] complete!");
            outputArea.setText(hod.viewAllModules(session.getCourses()));
        }
    }

    private void handleRemoveCourseByHod() {
        if (session.getCourses().isEmpty()) {
            showError("Operation Interrupted: No modules currently registered inside database pools.");
            return;
        }
        String[] options = new String[session.getCourses().size()];
        for (int i = 0;
                i < session.getCourses().size();
                i++) {
            options[i] = session.getCourses().get(i).getCode() + " - " + session.getCourses().get(i).getName();
        }
        JComboBox dropdownBox = new JComboBox<>(options);
        int feedbackResult = JOptionPane.showConfirmDialog(this, dropdownBox, "Select Course Module to Remove",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (feedbackResult == JOptionPane.OK_OPTION && dropdownBox.getSelectedIndex() != -1) {
            int selectedIdx = dropdownBox.getSelectedIndex();
            Course targetToRemove = session.getCourses().get(selectedIdx);
            int doubleCheck = JOptionPane.showConfirmDialog(this,
                    "Are you certain you want to remove " + targetToRemove.getCode() + "?\n"
                    + "This deletes the dynamic database file off the storage disk completely!",
                    "Confirm Destructive Administrative Removal Action",
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (doubleCheck == JOptionPane.YES_OPTION) {
                session.getCourses().remove(selectedIdx);
                File fileToDelete = new File("src/data/" + targetToRemove.getCode().toUpperCase() + "_marks.csv");
                if (fileToDelete.exists()) {
                    fileToDelete.delete();
                }
                if (session.getCurrentCourse() == targetToRemove) {
                    session.switchCourseContext(session.getCourses().isEmpty() ? null : session.getCourses().get(0));
                }
                JOptionPane.showMessageDialog(this, "Module profile entry and its corresponding file completely removed.");
                showHodScreen();
            }
        }
    }

    private String getSelectedStudentID() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Input Warning: You must pick and highlight a row item from the student list view layout.");
            return null;
        }
        return tableModel.getValueAt(row, 0).toString();
    }

    private Double readMark(String text, String componentName) {
        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(componentName + " metric values must parse to valid values.");
        }
    }

    private String showMark(Double mark) {
        return (mark == null) ? "--" : String.format("%.1f", mark);
    }

    private String markToText(Double mark) {
        return (mark == null) ? "" : String.valueOf(mark);
    }

    private void saveToFile() {
        if (session.getCurrentDatabase() != null) {
<<<<<<< HEAD
            session.getCurrentDatabase().saveSemData(session.getCurrentStudentManager());
=======
            session.getCurrentDatabase().saveData(session.getCurrentStudentManager());
>>>>>>> f1c27b15770d3fa365b1a2b07b09a48929e662e6
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Operational Exception Triggered", JOptionPane.ERROR_MESSAGE);
    }

    private void showText(String title, String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(450, 250));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.PLAIN_MESSAGE);
    }
}
