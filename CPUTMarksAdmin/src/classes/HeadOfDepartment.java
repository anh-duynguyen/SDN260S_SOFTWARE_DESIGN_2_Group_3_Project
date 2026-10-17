package classes;

import java.util.ArrayList;

// Head of Department can only VIEW results, not change any marks.
public class HeadOfDepartment extends User {

    private final ArrayList<Module> modules;

    public HeadOfDepartment(String name) {
        this(name, new ArrayList<>());
    }

    // modules this HoD looks after
    public HeadOfDepartment(String name, ArrayList<Module> modules) {
        super(name);
        if (modules == null) {
            this.modules = new ArrayList<>();
        } else {
            this.modules = modules;
        }
    }

    @Override
    public String getRole() {
        return "Head of Department";
    }

    @Override
    public boolean canEditMarks() {
        return false;
    }

    // ranked results for all students in a module
    public String viewModuleResults(Module module) {
        requireModule(module);
        CollectiveMarksManager cmm = new CollectiveMarksManager(module.getStudents());
        StringBuilder sb = new StringBuilder();
        sb.append("Results for ").append(module.getCode()).append(" - ")
          .append(module.getName()).append(System.lineSeparator());

        int rank = 1;
        for (Student s : cmm.getStudentsRankedByMark()) {
            double fin = s.getMarks().calculateFinalMark();
            sb.append(String.format("%3d. %-25s %-12s %5.1f%% (%s)%n", 
                    rank, s.getName(), s.getStudentID(), fin, CollectiveMarksManager.letterGrade(fin)));
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
    public String viewStudentMarks(Module module, String studentID) {
        requireModule(module);
        for (Student s : module.getStudents()) {
            if (s.getStudentID().equals(studentID)) {
                IndividualMarksManager m = s.getMarks();
                if (m.hasMissingMarks()) {
                    return s.getName() + ": incomplete (missing: "
                            + CollectiveMarksManager.describeMissing(s) + ")";
                }
                double fin = m.calculateFinalMark();
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
            CollectiveMarksManager cmm = new CollectiveMarksManager(m.getStudents());
            sb.append(String.format("%-10s %-25s %7.1f%% %7.1f%% %8d%n", m.getCode(), m.getName(),
                    cmm.calculateClassAverage(), cmm.calculatePassRate(),
                    cmm.countStudentsWithMissingMarks()));
        }
        return sb.toString();
    }

    private void requireModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null.");
        }
    }
}
