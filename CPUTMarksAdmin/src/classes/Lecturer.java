package classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Scanner;

// A lecturer can add assessments and students, capture/update/import mark and view a single student's marks.
// Every method checks its input first and throws an exception with a message.
 public class Lecturer extends User {

    private final Course course;
    private final StudentManager studentManager;
    private final Database database; 
    private Scanner input = new Scanner(System.in);

    public Lecturer(String name, Course course, StudentManager studentManager, Database database) {
        super(name);
        if (course == null || studentManager == null) {
            throw new IllegalArgumentException("Course and StudentManager are required.");
        }
        this.course = course;
        this.studentManager = studentManager;
        this.database = database;
    }

    // same as above but without a database
    public Lecturer(String name, Course course, StudentManager studentManager) {
        this(name, course, studentManager, null);
    }

    // lets Main share its Scanner so two Scanners don't fight over System.in
    public void setInput(Scanner input) {
        this.input = input;
=======

/* A lecturer can add assessments and students, capture/update/import marks
and view a single student's marks. They can't see reports across modules
(that's for the HeadOfDepartment).*/
public class Lecturer extends User {

    private final Course module;
    private final StudentManager studentManager;

    public Lecturer(String name, Course module, StudentManager studentManager) {
        super(name);
        if (module == null || studentManager == null) {
            throw new IllegalArgumentException("Module and StudentManager are required.");
        }
        this.module = module;
        this.studentManager = studentManager;
>>>>>>> origin/main
    }

    @Override
    public String getRole() {
        return "Lecturer";
    }

    @Override
    public boolean canEditMarks() {
        return true;
    }

    // adds a new assessment (key, label, weight in %, max mark)
    public void addAssessment(String key, String label, double weight, double maxMark) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Assessment key cannot be empty.");
        }
        if (weight <= 0 || weight > 100) {
            throw new IllegalArgumentException("Weight must be between 0 and 100.");
        }
        if (maxMark <= 0) {
            throw new IllegalArgumentException("Maximum mark must be greater than 0.");
        }

        // check for duplicates and add up the weights
        double totalWeight = weight;
<<<<<<< HEAD
        for (AssessmentComponent c : course.getAssessmentComponents()) {
=======
        for (AssessmentComponent c : module.getAssessmentComponents()) {
>>>>>>> origin/main
            if (c.getKey().equalsIgnoreCase(key.trim())) {
                throw new IllegalArgumentException("Assessment '" + key + "' already exists.");
            }
            totalWeight += c.getWeight();
        }
        if (totalWeight > 100) {
            throw new IllegalArgumentException(
                    "Total weight would be " + totalWeight + "%. It cannot exceed 100%.");
        }
<<<<<<< HEAD
        course.addComponent(new AssessmentComponent(key.trim(), label, weight, maxMark));
=======
        module.addComponent(new AssessmentComponent(key.trim(), label, weight, maxMark));
>>>>>>> origin/main
    }

    // add a student with no marks yet (all 3 count as missing)
    public void addStudent(String studentID, String studentName) {
        addStudent(studentID, studentName, null, null, null);
    }

    // add a student with the marks we have so far (null = not available yet)
    public void addStudent(String studentID, String studentName,
                           Double testMark, Double practicalMark, Double examMark) {
        if (studentID == null || studentID.trim().isEmpty()
                || studentName == null || studentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID and name cannot be empty.");
        }
        if (studentManager.findStudentByID(studentID.trim()) != null) {
            throw new IllegalArgumentException("Student " + studentID + " already exists.");
        }

        AssessmentComponent test = requireComponent("test");
        AssessmentComponent practical = requireComponent("practical");
        AssessmentComponent exam = requireComponent("exam");

        Double t = null;
        Double p = null;
        Double e = null;
        if (testMark != null) 
            t = checkRange("test", testMark);
<<<<<<< HEAD
        if (practicalMark != null)
=======
        if (practicalMark != null) 
>>>>>>> origin/main
            p = checkRange("practical", practicalMark);
        if (examMark != null) 
            e = checkRange("exam", examMark);

        Student s = new Student(studentName.trim(), studentID.trim(), t, p, e, test, practical, exam);
        studentManager.addStudent(s);
<<<<<<< HEAD
        course.addStudent(s);
=======
        module.addStudent(s);
>>>>>>> origin/main
    }

    // capture marks for a student, null means leave that mark as it is
    public void addMarks(String studentID, Double testMark, Double practicalMark, Double examMark) {
        IndividualMarksManager marks = findStudent(studentID).getMarks();

<<<<<<< HEAD
        // check everything before changing anything, so a bad mark
        // doesn't leave the student half updated
        Double t = null;
        Double p = null;
        Double e = null;
        if (testMark != null) t = checkRange("test", testMark);
        if (practicalMark != null) p = checkRange("practical", practicalMark);
        if (examMark != null) e = checkRange("exam", examMark);
=======
        Double t = null;
        Double p = null;
        Double e = null;
        if (testMark != null) 
            t = checkRange("test", testMark);
        if (practicalMark != null) 
            p = checkRange("practical", practicalMark);
        if (examMark != null) 
            e = checkRange("exam", examMark);
>>>>>>> origin/main

        if (t != null) 
            marks.setTestMark(t);
        if (p != null) 
            marks.setPracticalMark(p);
        if (e != null) 
            marks.setExamMark(e);
    }

    // change one mark (componentKey is test, practical or exam)
    public void updateMarks(String studentID, String componentKey, double newMark) {
        IndividualMarksManager marks = findStudent(studentID).getMarks();
        double valid = checkRange(componentKey, newMark);
        switch (componentKey.trim().toLowerCase()) {
            case "test":
                marks.setTestMark(valid);
                break;
            case "practical":
                marks.setPracticalMark(valid);
                break;
            case "exam":
                marks.setExamMark(valid);
                break;
            default:
                throw new IllegalArgumentException("Unknown component: " + componentKey);
        }
    }

<<<<<<< HEAD
    // Imports students and marks from a CSV file (same format as Database.saveData).
    // If any row is bad nothing gets changed.
=======
    /* Imports students and marks from a CSV file (same format as Database.saveData).
    If any row is bad nothing gets changed. New students are added and
    existing students get the marks from the file. A blank/NULL mark in
    the file will not overwrite a mark we already have.
    Returns how many rows were imported.*/
>>>>>>> origin/main
    public int importMarks(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
<<<<<<< HEAD
        if (!Files.exists(Paths.get(filePath.trim()))) {
=======
        if (!Files.exists(Paths.get(filePath))) {
>>>>>>> origin/main
            throw new IOException("File not found: " + filePath);
        }

        AssessmentComponent test = requireComponent("test");
        AssessmentComponent practical = requireComponent("practical");
        AssessmentComponent exam = requireComponent("exam");

<<<<<<< HEAD
        // load the whole file first, nothing is changed until every row is fine
        StudentManager loaded;
        try {
            loaded = new Database(filePath.trim()).loadData(test, practical, exam);
=======
        StudentManager loaded;
        try {
            loaded = new Database(filePath).loadData(test, practical, exam);
>>>>>>> origin/main
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Import cancelled: a row in the file is invalid ("
                    + ex.getClass().getSimpleName() + ": " + ex.getMessage()
                    + "). No changes were made.");
        }

        int count = 0;
        for (Student incoming : loaded.getStudents()) {
            Student existing = studentManager.findStudentByID(incoming.getStudentID());
            if (existing == null) {
                // new student
                studentManager.addStudent(incoming);
<<<<<<< HEAD
                course.addStudent(incoming);
=======
                module.addStudent(incoming);
>>>>>>> origin/main
            } else {
                // already exists, only copy over the marks that are in the file
                IndividualMarksManager from = incoming.getMarks();
                IndividualMarksManager to = existing.getMarks();
<<<<<<< HEAD
                if (from.getTestMark() != null)
                    to.setTestMark(from.getTestMark());
                if (from.getPracticalMark() != null)
                    to.setPracticalMark(from.getPracticalMark());
                if (from.getExamMark() != null)
=======
                if (from.getTestMark() != null) 
                    to.setTestMark(from.getTestMark());
                if (from.getPracticalMark() != null) 
                    to.setPracticalMark(from.getPracticalMark());
                if (from.getExamMark() != null) 
>>>>>>> origin/main
                    to.setExamMark(from.getExamMark());
            }
            count++;
        }
        return count;
    }

    // printable breakdown of one student's marks
    public String viewStudentMarks(String studentID) {
        Student s = findStudent(studentID);
        IndividualMarksManager m = s.getMarks();
        String nl = System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        sb.append(s.getName()).append(" (").append(s.getStudentID()).append(")").append(nl);
        sb.append("  Test:      ").append(show(m.getTestMark())).append(nl);
        sb.append("  Practical: ").append(show(m.getPracticalMark())).append(nl);
        sb.append("  Exam:      ").append(show(m.getExamMark())).append(nl);

        if (m.hasMissingMarks()) {
            sb.append("  Final:     incomplete - missing: ")
              .append(CollectiveMarksManager.describeMissing(s));
        } else {
            double fin = m.calculateFinalMark();
            sb.append(String.format("  Final:     %.1f%% (%s)", fin,
                    CollectiveMarksManager.letterGrade(fin)));
        }
        return sb.toString();
    }

<<<<<<< HEAD
    // students in this course who still have missing marks
    public ArrayList<Student> getStudentsWithMissingMarks() {
        CollectiveMarksManager cmm = new CollectiveMarksManager(course.getStudents());
        return cmm.getStudentsWithMissingMarks();
    }

    // These read from the keyboard, call the checked methods above and print the result or the error message
    public void addAssessment() {
        try {
            System.out.print("Assessment key (test, practical or exam): ");
            String key = input.nextLine().trim();
            System.out.print("Label (e.g. Test 1): ");
            String label = input.nextLine().trim();
            double weight = readNumber("Weight (%): ");
            double maxMark = readNumber("Maximum mark: ");
            addAssessment(key, label, weight, maxMark);
            System.out.println("Assessment added.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addMarks() {
        try {
            System.out.print("Student number: ");
            String id = input.nextLine().trim();
            findStudent(id); // stop early if the student doesn't exist
            Double test = readOptionalMark("Test mark (blank to skip): ");
            Double practical = readOptionalMark("Practical mark (blank to skip): ");
            Double exam = readOptionalMark("Exam mark (blank to skip): ");
            addMarks(id, test, practical, exam);
            save();
            System.out.println("Marks captured.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateMarks() {
        try {
            System.out.print("Student number: ");
            String id = input.nextLine().trim();
            findStudent(id);
            System.out.print("Which mark (test, practical or exam): ");
            String key = input.nextLine().trim();
            double newMark = readNumber("New mark: ");
            updateMarks(id, key, newMark);
            save();
            System.out.println("Mark updated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    // asks for a CSV file, imports it and saves
    public void importMarks() {
        try {
            System.out.print("CSV file to import: ");
            String path = input.nextLine().trim();
            int count = importMarks(path);
            save();
            System.out.println("Imported " + count + " row(s).");
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void viewStudentMarks() {
        try {
            System.out.print("Student number: ");
            String id = input.nextLine().trim();
            System.out.println(viewStudentMarks(id));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // private methods
=======
    // students in this module who still have missing marks
    public ArrayList<Student> getStudentsWithMissingMarks() {
        CollectiveMarksManager cmm = new CollectiveMarksManager(module.getStudents());
        return cmm.getStudentsWithMissingMarks();
    }

    //Methods only used in this class 
>>>>>>> origin/main

    private Student findStudent(String studentID) {
        Student s = studentManager.findStudentByID(studentID);
        if (s == null) {
            throw new IllegalArgumentException("No student found with ID " + studentID);
        }
        return s;
    }

    private AssessmentComponent requireComponent(String key) {
<<<<<<< HEAD
        for (AssessmentComponent c : course.getAssessmentComponents()) {
=======
        for (AssessmentComponent c : module.getAssessmentComponents()) {
>>>>>>> origin/main
            if (c.getKey().equalsIgnoreCase(key)) {
                return c;
            }
        }
<<<<<<< HEAD
        throw new IllegalStateException("The course needs a '" + key
=======
        throw new IllegalStateException("The module needs a '" + key
>>>>>>> origin/main
                + "' assessment first. Use addAssessment().");
    }

    // checks a mark is between 0 and the max for that component.
    // IndividualMarksManager only accepts 0-100 so we use whichever max is smaller.
    private double checkRange(String componentKey, double mark) {
        double max = 100.0;
<<<<<<< HEAD
        for (AssessmentComponent c : course.getAssessmentComponents()) {
=======
        for (AssessmentComponent c : module.getAssessmentComponents()) {
>>>>>>> origin/main
            if (c.getKey().equalsIgnoreCase(componentKey.trim())) {
                max = Math.min(c.getMaxMark(), 100.0);
                break;
            }
        }
        if (Double.isNaN(mark) || mark < 0 || mark > max) {
            throw new IllegalArgumentException(
                    "Mark for " + componentKey + " must be between 0 and " + max + ".");
        }
        return mark;
    }

<<<<<<< HEAD
    // reads a number, throws a readable error if it isn't one
    private double readNumber(String prompt) {
        System.out.print(prompt);
        String text = input.nextLine().trim();
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a number.");
        }
    }

    // same, but a blank answer means "no mark" (null)
    private Double readOptionalMark(String prompt) {
        System.out.print(prompt);
        String text = input.nextLine().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a number or leave it blank.");
        }
    }

    // writes the data to the file after a change (skipped if there is no database)
    private void save() {
        if (database == null) {
            return;
        }
        try {
            database.saveData(studentManager);
        } catch (Exception e) {
            System.out.println("Could not save to file: " + e.getMessage());
        }
    }

=======
>>>>>>> origin/main
    private String show(Double mark) {
        if (mark == null) {
            return "missing";
        }
        return String.format("%.1f", mark);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/main
