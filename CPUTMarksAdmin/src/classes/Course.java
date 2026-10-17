package classes;

import java.util.ArrayList;

public class Course {

    //stores the module information
    private String code;
    private String name;

    //stores the assessments and students for the module
    private ArrayList<AssessmentComponent> assessmentComponents;
    private ArrayList<Student> students;

    public Course(String code, String name) {
        this.code = code;
        this.name = name;

        assessmentComponents = new ArrayList<AssessmentComponent>();
        students = new ArrayList<Student>();
    }

    //adds an assessment to the module
    public void addComponent(AssessmentComponent component) {
        assessmentComponents.add(component);
    }

    //adds a student to the module
    public void addStudent(Student student) {
        students.add(student);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ArrayList<AssessmentComponent> getAssessmentComponents() {
        return assessmentComponents;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }
}