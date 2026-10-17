package classes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

// Calculates the class results (average, highest, lowest, pass/fail etc).
// Students with missing marks are left out of the stats because their final mark are not complete yet. They are listed separately.
public class CollectiveMarksManager {

    public static final double PASS_MARK = 50.0;

    private final ArrayList<Student> students;

    public CollectiveMarksManager(ArrayList<Student> students) {
        if (students == null) {
            this.students = new ArrayList<>();
        } else {
            this.students = students;
        }
    }

    // only the students who have all their marks
    private ArrayList<Student> completeStudents() {
        ArrayList<Student> complete = new ArrayList<>();
        for (Student s : students) {
            if (!s.getMarks().hasMissingMarks()) {
                complete.add(s);
            }
        }
        return complete;
    }

    // average final mark, 0 if nobody is complete
    public double calculateClassAverage() {
        ArrayList<Student> complete = completeStudents();
        if (complete.isEmpty()) {
            return 0.0;
        }
        double total = 0;
        for (Student s : complete) {
            total += s.getMarks().calculateFinalMark();
        }
        return total / complete.size();
    }

    // highest final mark, 0 if nobody is complete
    public double getHighestMark() {
        ArrayList<Student> complete = completeStudents();
        if (complete.isEmpty()) {
            return 0.0;
        }
        double highest = complete.get(0).getMarks().calculateFinalMark();
        for (Student s : complete) {
            double mark = s.getMarks().calculateFinalMark();
            if (mark > highest) {
                highest = mark;
            }
        }
        return highest;
    }

    // lowest final mark, 0 if nobody is complete
    public double getLowestMark() {
        ArrayList<Student> complete = completeStudents();
        if (complete.isEmpty()) {
            return 0.0;
        }
        double lowest = complete.get(0).getMarks().calculateFinalMark();
        for (Student s : complete) {
            double mark = s.getMarks().calculateFinalMark();
            if (mark < lowest) {
                lowest = mark;
            }
        }
        return lowest;
    }

    // students that still have a missing mark
    public ArrayList<Student> getStudentsWithMissingMarks() {
        ArrayList<Student> missing = new ArrayList<>();
        for (Student s : students) {
            if (s.getMarks().hasMissingMarks()) {
                missing.add(s);
            }
        }
        return missing;
    }

    public int countStudentsWithMissingMarks() {
        return getStudentsWithMissingMarks().size();
    }

    public int calculatePassCount() {
        int count = 0;
        for (Student s : completeStudents()) {
            if (s.getMarks().calculateFinalMark() >= PASS_MARK) {
                count++;
            }
        }
        return count;
    }

    public int calculateFailCount() {
        return completeStudents().size() - calculatePassCount();
    }

    // pass rate as a percentage (only counts complete students)
    public double calculatePassRate() {
        int complete = completeStudents().size();
        if (complete == 0) {
            return 0.0;
        }
        return (calculatePassCount() * 100.0) / complete;
    }

    // turns a final mark into the university's result symbol:
    // 1 (75-100), 2+ (70-74), 2- (60-69), 3 (50-59), F (0-49)
    public static String letterGrade(double mark) {
        if (mark >= 75) 
            return "1";
        if (mark >= 70) 
            return "2+";
        if (mark >= 60) 
            return "2-";
        if (mark >= PASS_MARK) 
            return "3";
        return "F";
    }

    // how many students got each symbol (1, 2+, 2-, 3, F)
    public Map<String, Integer> getGradeDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        String[] grades = {"1", "2+", "2-", "3", "F"};
        for (String g : grades) {
            distribution.put(g, 0);
        }
        for (Student s : completeStudents()) {
            String grade = letterGrade(s.getMarks().calculateFinalMark());
            distribution.put(grade, distribution.get(grade) + 1);
        }
        return distribution;
    }

    // students sorted from highest to lowest mark
    public ArrayList<Student> getStudentsRankedByMark() {
        ArrayList<Student> ranked = completeStudents();
        ranked.sort(Comparator.comparingDouble(
                (Student s) -> s.getMarks().calculateFinalMark()).reversed());
        return ranked;
    }

    // e.g. "Test, Exam" - empty string if nothing is missing
    public static String describeMissing(Student s) {
        IndividualMarksManager m = s.getMarks();
        ArrayList<String> parts = new ArrayList<>();
        if (m.getTestMark() == null) 
            parts.add("Test");
        if (m.getPracticalMark() == null) 
            parts.add("Practical");
        if (m.getExamMark() == null) 
            parts.add("Exam");
        return String.join(", ", parts);
    }

    // summary text used by the CLI, GUI and HoD view
    public String buildSummary() {
        int missingCount = countStudentsWithMissingMarks();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Students: %d (complete: %d, missing marks: %d)%n",
                students.size(), students.size() - missingCount, missingCount));
        sb.append(String.format("Class average: %.1f%%%n", calculateClassAverage()));
        sb.append(String.format("Highest: %.1f%%   Lowest: %.1f%%%n",
                getHighestMark(), getLowestMark()));
        sb.append(String.format("Pass: %d   Fail: %d   Pass rate: %.1f%%%n",
                calculatePassCount(), calculateFailCount(), calculatePassRate()));
        sb.append("Grade distribution: ").append(getGradeDistribution())
          .append(System.lineSeparator());
        return sb.toString();
    }
}