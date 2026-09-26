
package sdn260s_software_design_2_group_3_project;

public class Student {
    
    private String name;;
    private String studentID;
    private IndividualMarksManager marks;
    
    public Student(String name, String studentID, Double testMark, Double practicalMark, Double examMark, AssesmentComponent test, AssesmentComponent practical, AssesmentComponent exam) {
        this.name = name;
        this.studentID = studentID;
        this.marks = new IndividualMarksManager(testMark, practicalMark, examMark, test, practical, exam);
    }

    public String getName() {
        return name;
    }

    public String getStudentID() {
        return studentID;
    }

    public IndividualMarksManager getMarks() {
        return marks;
    }
}
