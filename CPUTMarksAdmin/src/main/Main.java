package main;

/**
 *
 * @author Group 3
 */

import java.util.ArrayList;
import java.util.Scanner;
import classes.*;
import java.io.IOException;

/**
 * Simple test that will run in terminal for the Marks Administration System.
 */
public class Main {

    private static final Scanner input = new Scanner(System.in);

    private static Modules module;
    private static StudentManager studentManager;
    private static Database database;
    private static Lecturer lecturer;
    private static HeadOfDepartment headOfDepartment;

    public static void main(String[] args) {
        setup();

        int choice;
        do {
            System.out.println("\n=== MARKS ADMINISTRATION SYSTEM ===");
            System.out.println("1. Lecturer");
            System.out.println("2. Head of Department");
            System.out.println("0. Exit");

            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    lecturerMenu();
                    break;
                case 2:
                    headMenu();
                    break;
                case 0:
                    System.out.println("Terminal test closed.");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        } while (choice != 0);

        input.close();
    }

    private static void setup() {
        try {
            module = new Modules("SDN260S", "Software Design 2");

            module.addComponent(new AssessmentComponent("test", "Test", 30, 100));
            module.addComponent(new AssessmentComponent("practical", "Practical", 30, 100));
            module.addComponent(new AssessmentComponent("exam", "Exam", 40, 100));

            studentManager = new StudentManager();
            database = new Database("src/data/marks.csv");
            //loadData() function of database throws an IOException
            try{
            database.loadData(new AssessmentComponent("test", "Test", 30, 100),
                  new AssessmentComponent("practical", "Practical", 30, 100),
                  new AssessmentComponent("exam", "Exam", 40, 100));
            }catch(IOException ex){
            System.out.println("Trouble accessing file : " + ex.getMessage());
            }
            
            lecturer = new Lecturer("Lecturer", module, studentManager, database);

            ArrayList<Modules> modules = new ArrayList<>();
            modules.add(module);
            headOfDepartment = new HeadOfDepartment("Head of Department", modules);

            System.out.println("System ready.");
        } catch (RuntimeException e) {
            System.out.println("Some classes could not be initialised: " + e.getMessage());
            System.out.println("The menu will still open; unavailable options will return to the menu.");
        }
    }

    private static void lecturerMenu() {
        int choice;
        do {
            System.out.println("\n--- LECTURER MENU ---");
            System.out.println("1. Add Student");
            System.out.println("2. Search Student");
            System.out.println("3. Add Assessment");
            System.out.println("4. Add Marks");
            System.out.println("5. Update Marks");
            System.out.println("6. Import Class List (CSV)");
            System.out.println("7. Export Class List (CSV)");
            System.out.println("8. View Student Marks");
            System.out.println("0. Back");

            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    safeRun("Add Student", Main::addStudent);
                    break;
                case 2:
                    safeRun("Search Student", Main::searchStudent);
                    break;
                case 3:
                    safeRun("Add Assessment", () -> lecturer.addAssessment());
                    break;
                case 4:
                    safeRun("Add Marks", () -> lecturer.addMarks());
                    break;
                case 5:
                    safeRun("Update Marks", () -> lecturer.updateMarks());
                    break;
                case 6:
                    safeRun("Import Class List", () -> lecturer.importMark());
                    break;
                case 7:
                    safeRun("Export Class List", () -> database.exportCSV(studentManager));
                    //exportCSV takes information in studentmanager and populates a file with it
                    //at the path destination
                    break;
                case 8:
                    safeRun("View Student Marks", () -> lecturer.viewStudentMarks());
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        } while (choice != 0);
    }

    private static void headMenu() {
        int choice;
        do {
            System.out.println("\n--- HEAD OF DEPARTMENT MENU ---");
            System.out.println("1. View Module Results");
            System.out.println("2. View Student Marks");
            System.out.println("3. View Class Summary");
            System.out.println("4. View All Modules");
            System.out.println("0. Back");

            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    safeRun("View Module Results", () -> headOfDepartment.viewModuleResults());
                    break;
                case 2:
                    safeRun("View Student Marks", () -> headOfDepartment.viewStudentMarks());
                    break;
                case 3:
                    safeRun("View Class Summary", () -> headOfDepartment.viewClassSummary());
                    break;
                case 4:
                    safeRun("View All Modules", () -> headOfDepartment.viewAllModules());
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        } while (choice != 0);
    }

    private static void addStudent() {
        if (studentManager == null || module == null || database == null) {
            System.out.println("The class system is not available. Returning to menu.");
            return;
        }

        System.out.print("Student number: ");
        String id = input.nextLine().trim();

        if (id.isEmpty()) {
            System.out.println("Student number cannot be empty.");
            return;
        }
        if (studentManager.findStudentByID(id) != null) {
            System.out.println("That student number already exists.");
            return;
        }

        System.out.print("Student name: ");
        String name = input.nextLine().trim();

        AssessmentComponent test = getComponent("test");
        AssessmentComponent practical = getComponent("practical");
        AssessmentComponent exam = getComponent("exam");

        if (test == null || practical == null || exam == null) {
            System.out.println("Test, Practical or Exam assessment is missing. Returning to menu.");
            return;
        }

        Double testMark = readMark("Test mark (blank if not captured): ", test);
        Double practicalMark = readMark("Practical mark (blank if not captured): ", practical);
        Double examMark = readMark("Exam mark (blank if not captured): ", exam);

        Student student = new Student(name, id, testMark, practicalMark, examMark,
                test, practical, exam);

        studentManager.addStudent(student);
        module.addStudent(student);
        database.saveData(studentManager);
        System.out.println("Student added.");
    }

    private static void searchStudent() {
        if (studentManager == null || studentManager.getStudents().isEmpty()) {
            System.out.println("There are no students in the class yet. Returning to menu.");
            return;
        }

        System.out.print("Student number to search: ");
        Student student = studentManager.findStudentByID(input.nextLine().trim());

        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        IndividualMarksManager marks = student.getMarks();
        System.out.println("\nStudent: " + student.getName());
        System.out.println("Student number: " + student.getStudentID());
        System.out.println("Test: " + show(marks.getTestMark()));
        System.out.println("Practical: " + show(marks.getPracticalMark()));
        System.out.println("Exam: " + show(marks.getExamMark()));

        if (marks.hasMissingMarks()) {
            System.out.println("Final mark: -- (marks missing)");
        } else {
            System.out.printf("Final mark: %.1f%%%n", marks.calculateFinalMark());
        }
    }

    private static AssessmentComponent getComponent(String key) {
        for (AssessmentComponent component : module.getAssessmentComponents()) {
            if (component.getKey().equalsIgnoreCase(key)) {
                return component;
            }
        }
        return null;
    }

    private static Double readMark(String prompt, AssessmentComponent component) {
        while (true) {
            System.out.print(prompt);
            String value = input.nextLine().trim();

            if (value.isEmpty()) {
                return null;
            }

            try {
                double mark = Double.parseDouble(value);
                if (mark >= 0 && mark <= component.getMaxMark()) {
                    return mark;
                }
            } catch (NumberFormatException ignored) {
                // Show the validation message below.
            }

            System.out.println("Enter a mark between 0 and " + component.getMaxMark()
                    + ", or leave it blank.");
        }
    }

    private static String show(Double mark) {
        return mark == null ? "--" : mark.toString();
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void safeRun(String feature, Runnable action) {
        try {
            action.run();
        } catch (UnsupportedOperationException e) {
            System.out.println(feature + " is not available yet. Returning to menu.");
        } catch (NullPointerException e) {
            System.out.println(feature + " is unavailable because a required class or object is missing.");
            System.out.println("Returning to menu.");
        } catch (RuntimeException e) {
            System.out.println(feature + " could not run. Returning to menu.");
        }
    }
}
