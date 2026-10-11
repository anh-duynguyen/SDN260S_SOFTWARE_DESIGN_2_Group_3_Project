package sdn260s_software_design_2_group_3_project;

import java.util.ArrayList;

public class Module {

    //stores the module information
    private String code;
    private String name;

    //stores the assessments and students for the module
    private ArrayList<AssessmentComponent> assessmentComponents;
    private ArrayList<Student> students;

    public Module(String code, String name) {
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