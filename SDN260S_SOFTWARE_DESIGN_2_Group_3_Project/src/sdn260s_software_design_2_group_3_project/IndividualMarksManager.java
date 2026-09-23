
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
    //checks if test mark is valid, if not throws an exception
        if (!isValidMark(testMark)) {
            throw new IllegalArgumentException("Invalid test mark. Please enter a mark between 0 and 100.");
        }
        if (!isValidMark(practicalMark)) {
            throw new IllegalArgumentException("Invalid practical mark. Please enter a mark between 0 and 100.");
        }
        if (!isValidMark(examMark)) {
            throw new IllegalArgumentException("Invalid exam mark. Please enter a mark between 0 and 100.");
        }

        //stores marks inside the object
        this.testMark = testMark;
        this.practicalMark = practicalMark;
        this.examMark = examMark;
    }

    public double calculateFinalMark() {
        //calculates the contribution of each mark to the final mark
        double testContribution = testMark * testWeight;
        double practicalContribution = practicalMark * practicalWeight;
        double examContribution = examMark * examWeight;
        //calculates the final mark by summing the contributions
        double finalMark = testContribution + practicalContribution + examContribution;
        return finalMark;   
    }

    public double calculateAverage(){
        //adds all marks together to get the total
        double total = testMark + practicalMark + examMark;
        double average = total / 3; 
        return average;
    }
    
    public double getHighestMark() {
        //finds the highest mark among the three
        double highest = testMark; // assume testMark is the highest initially
        if (practicalMark > highest) {
            highest = practicalMark;
        }
        if (examMark > highest) {
            highest = examMark;
        }
        return highest;
    }

    public double getLowestMark() {
        double lowest = testMark; // assume testMark is the lowest initially
        if (practicalMark < lowest) {
            lowest = practicalMark;
        }
        if (examMark < lowest) {
            lowest = examMark;
        }
        return lowest;
    }

    private boolean isValidMark(double mark) {
        //checks if the mark is between 0 and 100
        return mark >= 0 && mark <= 100;
    }
    
}
