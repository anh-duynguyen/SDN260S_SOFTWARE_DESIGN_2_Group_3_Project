
package sdn260s_software_design_2_group_3_project;

public class IndividualMarksManager {
    //Temporary weights - to be confirmed with the group 
    private double testWeight = 0.20;
    private double practicalWeight = 0.30;
    private double examWeight = 0.50;
    
    //student marks
    private double testMark;
    private double practicalMark;
    private double examMark;

    //constructor to create the object with marks already provided in the csv file
    public IndividualMarksManager(double testMark, double practicalMark, double examMark) {
        //stores marks inside the object
        this.testMark = testMark;
        this.practicalMark = practicalMark;
        this.examMark = examMark;
    }
    
}
