package classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/* A lecturer can add assessments and students, capture/update/import marks
and view a single student's marks. They can't see reports across modules
(that's for the HeadOfDepartment).*/
public class Lecturer extends User {

    private final Module module;
    private final StudentManager studentManager;

    public Lecturer(String name, Module module, StudentManager studentManager) {
        super(name);
        if (module == null || studentManager == null) {
            throw new IllegalArgumentException("Module and StudentManager are required.");
        }
        this.module = module;
        this.studentManager = studentManager;
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
        for (AssessmentComponent c : module.getAssessmentComponents()) {
            if (c.getKey().equalsIgnoreCase(key.trim())) {
                throw new IllegalArgumentException("Assessment '" + key + "' already exists.");
            }
            totalWeight += c.getWeight();
        }
        if (totalWeight > 100) {
            throw new IllegalArgumentException(
                    "Total weight would be " + totalWeight + "%. It cannot exceed 100%.");
        }
        module.addComponent(new AssessmentComponent(key.trim(), label, weight, maxMark));
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
        if (practicalMark != null) 
            p = checkRange("practical", practicalMark);
        if (examMark != null) 
            e = checkRange("exam", examMark);

        Student s = new Student(studentName.trim(), studentID.trim(), t, p, e, test, practical, exam);
        studentManager.addStudent(s);
        module.addStudent(s);
    }

    // capture marks for a student, null means leave that mark as it is
    public void addMarks(String studentID, Double testMark, Double practicalMark, Double examMark) {
        IndividualMarksManager marks = findStudent(studentID).getMarks();

        Double t = null;
        Double p = null;
        Double e = null;
        if (testMark != null) 
            t = checkRange("test", testMark);
        if (practicalMark != null) 
            p = checkRange("practical", practicalMark);
        if (examMark != null) 
            e = checkRange("exam", examMark);

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

    /* Imports students and marks from a CSV file (same format as Database.saveData).
    If any row is bad nothing gets changed. New students are added and
    existing students get the marks from the file. A blank/NULL mark in
    the file will not overwrite a mark we already have.
    Returns how many rows were imported.*/
    public int importMarks(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
        if (!Files.exists(Paths.get(filePath))) {
            throw new IOException("File not found: " + filePath);
        }

        AssessmentComponent test = requireComponent("test");
        AssessmentComponent practical = requireComponent("practical");
        AssessmentComponent exam = requireComponent("exam");

        StudentManager loaded;
        try {
            loaded = new Database(filePath).loadData(test, practical, exam);
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
                module.addStudent(incoming);
            } else {
                // already exists, only copy over the marks that are in the file
                IndividualMarksManager from = incoming.getMarks();
                IndividualMarksManager to = existing.getMarks();
                if (from.getTestMark() != null) 
                    to.setTestMark(from.getTestMark());
                if (from.getPracticalMark() != null) 
                    to.setPracticalMark(from.getPracticalMark());
                if (from.getExamMark() != null) 
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

    // students in this module who still have missing marks
    public ArrayList<Student> getStudentsWithMissingMarks() {
        CollectiveMarksManager cmm = new CollectiveMarksManager(module.getStudents());
        return cmm.getStudentsWithMissingMarks();
    }

    //Methods only used in this class 

    private Student findStudent(String studentID) {
        Student s = studentManager.findStudentByID(studentID);
        if (s == null) {
            throw new IllegalArgumentException("No student found with ID " + studentID);
        }
        return s;
    }

    private AssessmentComponent requireComponent(String key) {
        for (AssessmentComponent c : module.getAssessmentComponents()) {
            if (c.getKey().equalsIgnoreCase(key)) {
                return c;
            }
        }
        throw new IllegalStateException("The module needs a '" + key
                + "' assessment first. Use addAssessment().");
    }

    // checks a mark is between 0 and the max for that component.
    // IndividualMarksManager only accepts 0-100 so we use whichever max is smaller.
    private double checkRange(String componentKey, double mark) {
        double max = 100.0;
        for (AssessmentComponent c : module.getAssessmentComponents()) {
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

    private String show(Double mark) {
        if (mark == null) {
            return "missing";
        }
        return String.format("%.1f", mark);
    }
}
