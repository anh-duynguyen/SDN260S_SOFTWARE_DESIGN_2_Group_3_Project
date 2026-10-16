package classes;

/**
 *
 * @author User
 */
public class Student {
    
    private String name;;
    private String studentID;
    private IndividualMarksManager marks;
    
    public Student(String name, String studentID, Double testMark, Double practicalMark, Double examMark, AssessmentComponent test, AssessmentComponent practical, AssessmentComponent exam) {
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

