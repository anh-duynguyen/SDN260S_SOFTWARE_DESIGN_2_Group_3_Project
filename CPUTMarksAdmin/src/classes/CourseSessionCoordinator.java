package classes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CourseSessionCoordinator {

    private final ArrayList<Course> courses; 
    private Course currentCourse; 
    private StudentManager currentStudentManager;
    private Database currentDatabase;

    public CourseSessionCoordinator() {
        this.courses = new ArrayList<>();
        discoverAndLoadCourses();
    }

    private void discoverAndLoadCourses() {
        File dataFolder = new File("src/data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File[] foundFiles = dataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith("_marks.csv"));

        if (foundFiles != null && foundFiles.length > 0) {
            for (File file : foundFiles) {
                String filename = file.getName();
                String courseCode = filename.substring(0, filename.toLowerCase().lastIndexOf("_marks.csv")).toUpperCase();
                
                // Read embedded dynamic description metadata header safely
                String courseName = "Module Profile " + courseCode; // Fallback default
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    if (!lines.isEmpty() && lines.get(0).startsWith("#CourseName:")) {
                        courseName = lines.get(0).substring("#CourseName:".length()).trim();
                    }
                } catch (IOException ignored) {}
                
                Course reloadedCourse = new Course(courseCode, courseName);
                reloadedCourse.addComponent(new AssessmentComponent("test", "Test", 30, 100));
                reloadedCourse.addComponent(new AssessmentComponent("practical", "Practical", 30, 100));
                reloadedCourse.addComponent(new AssessmentComponent("exam", "Exam", 40, 100));
                
                courses.add(reloadedCourse);
            }
        } else {
            Course defaultCourse = new Course("SDN260S", "Software Design 2");
            defaultCourse.addComponent(new AssessmentComponent("test", "Test", 30, 100));
            defaultCourse.addComponent(new AssessmentComponent("practical", "Practical", 30, 100));
            defaultCourse.addComponent(new AssessmentComponent("exam", "Exam", 40, 100));
            courses.add(defaultCourse);
            
            try {
                File firstFile = new File("src/data/SDN260S_marks.csv");
                firstFile.getParentFile().mkdirs();
                Files.write(firstFile.toPath(), List.of("#CourseName:Software Design 2", "StudentID, Name, T1, T2, FISA, Final %, Grade, Status"));
            } catch (IOException ignored) {}
        }
        if (!courses.isEmpty()) {
            switchCourseContext(courses.get(0));
        }
    }

    public void switchCourseContext(Course targetCourse) {
        if (targetCourse == null) {
            this.currentCourse = null;
            this.currentStudentManager = new StudentManager();
            this.currentDatabase = null;
            return;
        }

        this.currentCourse = targetCourse;
        this.currentStudentManager = new StudentManager();
        
        String pathName = "src/data/" + targetCourse.getCode().toUpperCase() + "_marks.csv";
        this.currentDatabase = new Database(pathName);

        AssessmentComponent test = targetCourse.getAssessmentComponents().get(0);
        AssessmentComponent practical = targetCourse.getAssessmentComponents().get(1);
        AssessmentComponent exam = targetCourse.getAssessmentComponents().get(2);

        try {
            this.currentStudentManager = currentDatabase.loadData(test, practical, exam);
        } catch (Exception ignored) {}

        targetCourse.getStudents().clear();
        for (Student s : currentStudentManager.getStudents()) {
            targetCourse.addStudent(s);
        }
    }

    public ArrayList<Course> getCourses() { return courses; }
    public Course getCurrentCourse() { return currentCourse; }
    public StudentManager getCurrentStudentManager() { return currentStudentManager; }
    public Database getCurrentDatabase() { return currentDatabase; }
}
