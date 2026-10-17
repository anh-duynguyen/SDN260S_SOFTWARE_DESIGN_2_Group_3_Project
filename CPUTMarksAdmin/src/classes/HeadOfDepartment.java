package classes;

import java.util.ArrayList;
import java.util.Scanner;

// Head of Department can only VIEW results, not change any marks.
public class HeadOfDepartment extends User {

    private final ArrayList<Course> courses;
    private Scanner input = new Scanner(System.in);

    public HeadOfDepartment(String name) {
        this(name, new ArrayList<>());
    }

    // courses this HoD looks after
    public HeadOfDepartment(String name, ArrayList<Course> courses) {
        super(name);
        if (courses == null) {
            this.courses = new ArrayList<>();
        } else {
            this.courses = courses;
        }
    }

    // lets Main share its Scanner so two Scanners don't fight over System.in
    public void setInput(Scanner input) {
        this.input = input;
    }

    @Override
    public String getRole() {
        return "Head of Department";
    }

    @Override
    public boolean canEditMarks() {
        return false;
    }

    // ranked results for all students in a course
    public String viewModuleResults(Course course) {
        requireCourse(course);
        CollectiveMarksManager cmm = new CollectiveMarksManager(course.getStudents());
        StringBuilder sb = new StringBuilder();
        sb.append("Results for ").append(course.getCode()).append(" - ")
          .append(course.getName()).append(System.lineSeparator());

        int rank = 1;
        for (Student s : cmm.getStudentsRankedByMark()) {
            double fin = s.getMarks().calculateFinalMark();
            sb.append(String.format("%3d. %-25s %-12s %5.1f%% (%s)%n", rank, s.getName(),
                    s.getStudentID(), fin, CollectiveMarksManager.letterGrade(fin)));
            rank++;
        }

        // students who don't have all their marks yet
        for (Student s : cmm.getStudentsWithMissingMarks()) {
            sb.append(String.format("  -  %-25s %-12s incomplete (missing: %s)%n",
                    s.getName(), s.getStudentID(), CollectiveMarksManager.describeMissing(s)));
        }
        return sb.toString();
    }

    // look at one student's marks
    public String viewStudentMarks(Course course, String studentID) {
        requireCourse(course);
        for (Student s : course.getStudents()) {
            if (s.getStudentID().equals(studentID)) {
                IndividualMarksManager m = s.getMarks();
                if (m.hasMissingMarks()) {
                    return s.getName() + ": incomplete (missing: "
                            + CollectiveMarksManager.describeMissing(s) + ")";
                }
                double fin = m.calculateFinalMark();
                return String.format("%s: %.1f%% (%s)", s.getName(), fin,
                        CollectiveMarksManager.letterGrade(fin));
            }
        }
        throw new IllegalArgumentException("No student " + studentID + " in " + course.getCode());
    }

    // average, pass rate and grades for one course
    public String viewClassSummary(Course course) {
        requireCourse(course);
        CollectiveMarksManager cmm = new CollectiveMarksManager(course.getStudents());
        return "Summary for " + course.getCode() + System.lineSeparator() + cmm.buildSummary();
    }

    // These methods from the UML (no parameters, they print)

    public void viewModuleResults() {
        Course course = pickCourse();
        if (course != null) {
            System.out.print(viewModuleResults(course));
        }
    }

    public void viewStudentMarks() {
        Course course = pickCourse();
        if (course == null) {
            return;
        }
        System.out.print("Student number: ");
        String id = input.nextLine().trim();
        try {
            System.out.println(viewStudentMarks(course, id));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    public void viewClassSummary() {
        Course course = pickCourse();
        if (course != null) {
            System.out.print(viewClassSummary(course));
        }
    }

    // overview of the courses this HoD was created with
    public void viewAllModules() {
        System.out.print(viewAllModules(courses));
    }

    // one line per course so they can be compared
    public String viewAllModules(ArrayList<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return "No courses available.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-25s %8s %8s %8s%n",
                "Code", "Course", "Average", "PassRate", "Missing"));
        for (Course m : courses) {
            CollectiveMarksManager cmm = new CollectiveMarksManager(m.getStudents());
            sb.append(String.format("%-10s %-25s %7.1f%% %7.1f%% %8d%n", m.getCode(), m.getName(),
                    cmm.calculateClassAverage(), cmm.calculatePassRate(),
                    cmm.countStudentsWithMissingMarks()));
        }
        return sb.toString();
    }

    // if there is one course use it, otherwise ask which one
    private Course pickCourse() {
        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return null;
        }
        if (courses.size() == 1) {
            return courses.get(0);
        }
        System.out.print("Course code: ");
        String code = input.nextLine().trim();
        for (Course m : courses) {
            if (m.getCode().equalsIgnoreCase(code)) {
                return m;
            }
        }
        System.out.println("No course with code " + code);
        return null;
    }

    private void requireCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
    }
}