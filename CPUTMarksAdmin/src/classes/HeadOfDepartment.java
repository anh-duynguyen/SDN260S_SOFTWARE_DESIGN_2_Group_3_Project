package classes;

import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Scanner;
=======
>>>>>>> origin/main

// Head of Department can only VIEW results, not change any marks.
public class HeadOfDepartment extends User {

<<<<<<< HEAD
    private final ArrayList<Course> courses;
    private Scanner input = new Scanner(System.in);
=======
    private final ArrayList<Course> modules;
>>>>>>> origin/main

    public HeadOfDepartment(String name) {
        this(name, new ArrayList<>());
    }

<<<<<<< HEAD
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

=======
    // modules this HoD looks after
    public HeadOfDepartment(String name, ArrayList<Course> modules) {
        super(name);
        if (modules == null) {
            this.modules = new ArrayList<>();
        } else {
            this.modules = modules;
        }
    }

>>>>>>> origin/main
    @Override
    public String getRole() {
        return "Head of Department";
    }

    @Override
    public boolean canEditMarks() {
        return false;
    }

<<<<<<< HEAD
    // ranked results for all students in a course
    public String viewModuleResults(Course course) {
        requireCourse(course);
        CollectiveMarksManager cmm = new CollectiveMarksManager(course.getStudents());
        StringBuilder sb = new StringBuilder();
        sb.append("Results for ").append(course.getCode()).append(" - ")
          .append(course.getName()).append(System.lineSeparator());
=======
    // ranked results for all students in a module
    public String viewModuleResults(Course module) {
        requireModule(module);
        CollectiveMarksManager cmm = new CollectiveMarksManager(module.getStudents());
        StringBuilder sb = new StringBuilder();
        sb.append("Results for ").append(module.getCode()).append(" - ")
          .append(module.getName()).append(System.lineSeparator());
>>>>>>> origin/main

        int rank = 1;
        for (Student s : cmm.getStudentsRankedByMark()) {
            double fin = s.getMarks().calculateFinalMark();
<<<<<<< HEAD
            sb.append(String.format("%3d. %-25s %-12s %5.1f%% (%s)%n", rank, s.getName(),
                    s.getStudentID(), fin, CollectiveMarksManager.letterGrade(fin)));
=======
            sb.append(String.format("%3d. %-25s %-12s %5.1f%% (%s)%n", 
                    rank, s.getName(), s.getStudentID(), fin, CollectiveMarksManager.letterGrade(fin)));
>>>>>>> origin/main
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
<<<<<<< HEAD
    public String viewStudentMarks(Course course, String studentID) {
        requireCourse(course);
        for (Student s : course.getStudents()) {
=======
    public String viewStudentMarks(Course module, String studentID) {
        requireModule(module);
        for (Student s : module.getStudents()) {
>>>>>>> origin/main
            if (s.getStudentID().equals(studentID)) {
                IndividualMarksManager m = s.getMarks();
                if (m.hasMissingMarks()) {
                    return s.getName() + ": incomplete (missing: "
                            + CollectiveMarksManager.describeMissing(s) + ")";
                }
                double fin = m.calculateFinalMark();
<<<<<<< HEAD
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
=======
                return String.format("%s: %.1f%% (%s)", 
                        s.getName(), fin, CollectiveMarksManager.letterGrade(fin));
            }
        }
        throw new IllegalArgumentException("No student " + studentID + " in " + module.getCode());
    }

    // average, pass rate and grades for one module
    public String viewClassSummary(Module module) {
        requireModule(module);
        CollectiveMarksManager cmm = new CollectiveMarksManager(module.getStudents());
        return "Summary for " + module.getCode() + System.lineSeparator() + cmm.buildSummary();
    }

    // overview of the modules this HoD was created with
    public String viewAllModules() {
        return viewAllModules(modules);
    }

    // one line per module so they can be compared
    public String viewAllModules(ArrayList<Module> modules) {
        if (modules == null || modules.isEmpty()) {
            return "No modules available.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-25s %8s %8s %8s%n",
                "Code", "Module", "Average", "PassRate", "Missing"));
        for (Module m : modules) {
>>>>>>> origin/main
            CollectiveMarksManager cmm = new CollectiveMarksManager(m.getStudents());
            sb.append(String.format("%-10s %-25s %7.1f%% %7.1f%% %8d%n", m.getCode(), m.getName(),
                    cmm.calculateClassAverage(), cmm.calculatePassRate(),
                    cmm.countStudentsWithMissingMarks()));
        }
        return sb.toString();
    }

<<<<<<< HEAD
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
=======
    private void requireModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null.");
        }
    }
}
>>>>>>> origin/main
