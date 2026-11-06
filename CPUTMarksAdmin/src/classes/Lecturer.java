package classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

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

    public Lecturer(String name, Course course, StudentManager studentManager) {
        this(name, course, studentManager, null);
    }

    public void setInput(Scanner input) {
        this.input = input;
    }

    @Override
    public String getRole() {
        return "Lecturer";
    }

    @Override
    public boolean canEditMarks() {
        return true;
    }

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

        double totalWeight = weight;
        for (AssessmentComponent c
                : course.getAssessmentComponents()) {
            if (c.getKey().equalsIgnoreCase(key.trim())) {
                throw new IllegalArgumentException("Assessment '" + key + "' already exists.");
            }
            totalWeight += c.getWeight();
        }
        if (totalWeight > 100) {
            throw new IllegalArgumentException("Total weight would be " + totalWeight + "%. It cannot exceed 100%.");
        }
        course.addComponent(new AssessmentComponent(key.trim(), label, weight, maxMark));
    }

    public void addStudent(String studentID, String studentName) {
        addStudent(studentID, studentName, null, null, null);
    }

    public void addStudent(String studentID, String studentName, Double testMark, Double practicalMark, Double examMark) {
        if (studentID == null || studentID.trim().isEmpty() || studentName == null || studentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID and name cannot be empty.");
        }
        if (studentManager.findStudentByID(studentID.trim()) != null) {
            throw new IllegalArgumentException("Student " + studentID + " already exists.");
        }

        AssessmentComponent test = requireComponent(course.getAssessmentComponents().get(0).getKey());
        AssessmentComponent practical = requireComponent(course.getAssessmentComponents().get(1).getKey());
        AssessmentComponent exam = requireComponent(course.getAssessmentComponents().get(2).getKey());

        Double t = (testMark != null) ? checkRange(test.getKey(), testMark) : null;
        Double p = (practicalMark != null) ? checkRange(practical.getKey(), practicalMark) : null;
        Double e = (examMark != null) ? checkRange(exam.getKey(), examMark) : null;

        Student s = new Student(studentName.trim(), studentID.trim(), t, p, e, test, practical, exam);
        studentManager.addStudent(s);
        course.addStudent(s);
    }

    public void addMarks(String studentID, Double testMark, Double practicalMark, Double examMark) {
        IndividualMarksManager marks = findStudent(studentID).getMarks();

        String testKey = course.getAssessmentComponents().get(0).getKey();
        String pracKey = course.getAssessmentComponents().get(1).getKey();
        String examKey = course.getAssessmentComponents().get(2).getKey();

        Double t = (testMark != null) ? checkRange(testKey, testMark) : null;
        Double p = (practicalMark != null) ? checkRange(pracKey, practicalMark) : null;
        Double e = (examMark != null) ? checkRange(examKey, examMark) : null;

        if (t != null) {
            marks.setTestMark(t);
        }
        if (p != null) {
            marks.setPracticalMark(p);
        }
        if (e != null) {
            marks.setExamMark(e);
        }
    }

    public void updateMarks(String studentID, String componentKey, double newMark) {
        IndividualMarksManager marks = findStudent(studentID).getMarks();
        double valid = checkRange(componentKey, newMark);

        if (componentKey.trim().equalsIgnoreCase(course.getAssessmentComponents().get(0).getKey())) {
            marks.setTestMark(valid);
        } else if (componentKey.trim().equalsIgnoreCase(course.getAssessmentComponents().get(1).getKey())) {
            marks.setPracticalMark(valid);
        } else if (componentKey.trim().equalsIgnoreCase(course.getAssessmentComponents().get(2).getKey())) {
            marks.setExamMark(valid);
        } else {
            throw new IllegalArgumentException("Unknown evaluation component context structural key: " + componentKey);
        }
    }

    public int importMarks(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
        if (!Files.exists(Paths.get(filePath.trim()))) {
            throw new IOException("File not found: " + filePath);
        }

        AssessmentComponent test = requireComponent(course.getAssessmentComponents().get(0).getKey());
        AssessmentComponent practical = requireComponent(course.getAssessmentComponents().get(1).getKey());
        AssessmentComponent exam = requireComponent(course.getAssessmentComponents().get(2).getKey());

        StudentManager loaded;
        try {
            loaded = new Database(filePath.trim()).loadData(test, practical, exam);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Import cancelled: an input row element is corrupted. No updates applied.");
        }

        int count = 0;
        for (Student incoming
                : loaded.getStudents()) {
            Student existing = studentManager.findStudentByID(incoming.getStudentID());
            if (existing == null) {
                studentManager.addStudent(incoming);
                course.addStudent(incoming);
            } else {
                IndividualMarksManager from = incoming.getMarks();
                IndividualMarksManager to = existing.getMarks();
                if (from.getTestMark() != null) {
                    to.setTestMark(from.getTestMark());
                }
                if (from.getPracticalMark() != null) {
                    to.setPracticalMark(from.getPracticalMark());
                }
                if (from.getExamMark() != null) {
                    to.setExamMark(from.getExamMark());
                }
            }
            count++;
        }
        return count;
    }

    public String viewStudentMarks(String studentID) {
        Student s = findStudent(studentID);
        IndividualMarksManager m = s.getMarks();
        String nl = System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        sb.append(s.getName()).append(" (").append(s.getStudentID()).append(")").append(nl);
        sb.append("  ").append(course.getAssessmentComponents().get(0).getLabel()).append(": ").append(show(m.getTestMark())).append(nl);
        sb.append("  ").append(course.getAssessmentComponents().get(1).getLabel()).append(": ").append(show(m.getPracticalMark())).append(nl);
        sb.append("  ").append(course.getAssessmentComponents().get(2).getLabel()).append(": ").append(show(m.getExamMark())).append(nl);

        if (m.hasMissingMarks()) {
            sb.append("  Final summary: Incomplete evaluation record structure - missing: ")
                    .append(CollectiveMarksManager.describeMissing(s));
        } else {
            double fin = m.calculateFinalMark();
            sb.append(String.format("  Final composite mark evaluation: %.1f%% (%s)", fin, CollectiveMarksManager.letterGrade(fin)));
        }
        return sb.toString();
    }

    public ArrayList<Student> getStudentsWithMissingMarks() {
        CollectiveMarksManager cmm = new CollectiveMarksManager(course.getStudents());
        return cmm.getStudentsWithMissingMarks();
    }

    public void addAssessment() {
        try {
            System.out.print("Assessment structural descriptor label key: ");
            String key = input.nextLine().trim();
            System.out.print("Label name display context: ");
            String label = input.nextLine().trim();
            double weight = readNumber("Weight percentage metrics allocation (%): ");
            double maxMark = readNumber("Maximum threshold: ");
            addAssessment(key, label, weight, maxMark);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addMarks() {
        try {
            System.out.print("Student profile unique identifier matching reference number: ");
            String id = input.nextLine().trim();
            findStudent(id);
            Double test = readOptionalMark("First evaluation category entry: ");
            Double practical = readOptionalMark("Second evaluation category entry: ");
            Double exam = readOptionalMark("Third evaluation category entry: ");
            addMarks(id, test, practical, exam);
            save();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateMarks() {
        try {
            System.out.print("Student context sequence: ");
            String id = input.nextLine().trim();
            findStudent(id);
            System.out.print("Target subcategory key identifier code: ");
            String key = input.nextLine().trim();
            double newMark = readNumber("Updated numeric value assignment: ");
            updateMarks(id, key, newMark);
            save();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void importMarks() {
        try {
            System.out.print("Absolute system tracking location path target destination: ");
            String path = input.nextLine().trim();
            int count = importMarks(path);
            save();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void viewStudentMarks() {
        try {
            System.out.print("Student context verification parameter code: ");
            String id = input.nextLine().trim();
            System.out.println(viewStudentMarks(id));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private Student findStudent(String studentID) {
        Student s = studentManager.findStudentByID(studentID);
        if (s == null) {
            throw new IllegalArgumentException("Registry missing error: No records tracked matching target ID sequence " + studentID);
        }
        return s;
    }

    private AssessmentComponent requireComponent(String key) {
        for (AssessmentComponent c
                : course.getAssessmentComponents()) {
            if (c.getKey().equalsIgnoreCase(key)) {
                return c;
            }
        }
        throw new IllegalStateException("Missing dependency parameter element structural context validation error matching layout for key descriptor: '" + key + "'");
    }

    private double checkRange(String componentKey, double mark) {
        double max = 100.0;
        for (AssessmentComponent c
                : course.getAssessmentComponents()) {
            if (c.getKey().equalsIgnoreCase(componentKey.trim())) {
                max = Math.min(c.getMaxMark(), 100.0);
                break;
            }
        }
        if (Double.isNaN(mark) || mark < 0 || mark > max) {
            throw new IllegalArgumentException("Boundary mismatch exception error: Assessment values for element field [" + componentKey + "] must register bounded strictly between 0 and " + max);
        }
        return mark;
    }

    private double readNumber(String prompt) {
        System.out.print(prompt);
        String text = input.nextLine().trim();
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Exception: Entered parameter input layout fail parsing to expected numbers.");
        }
    }

    private Double readOptionalMark(String prompt) {
        System.out.print(prompt);
        String text = input.nextLine().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Type Exception: Value mapping tracking constraints fail expected layout formatting.");
        }
    }

    private void save() {
        if (database == null) {
            return;
        }
        try {
            database.saveData(studentManager);
        } catch (Exception e) {
            System.out.println("Persistent state serialization issue: " + e.getMessage());
        }
    }

    private String show(Double mark) {
        return (mark == null) ? "missing" : String.format("%.1f", mark);
    }
}
