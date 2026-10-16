package classes;

/**
 *
 * @author User
 */
public class IndividualMarksManager { 
    //student marks... Double can hold null and double cannot, so we use Double to allow for null values
    private Double testMark;
    private Double practicalMark;
    private Double examMark;
    private AssessmentComponent test;
    private AssessmentComponent practical;
    private AssessmentComponent exam;

    //constructor to create the object with marks already provided
    public IndividualMarksManager(Double testMark, Double practicalMark, Double examMark, AssessmentComponent test, AssessmentComponent practical, AssessmentComponent exam) {
        this.test = test;
        this.practical = practical;
        this.exam = exam;
    //checks if mark is valid, if not throws an exception
        if (testMark != null && !isValidMark(testMark)) {
            throw new IllegalArgumentException("Invalid test mark. Please enter a mark between 0 and 100.");
        }
        if (practicalMark != null && !isValidMark(practicalMark)) {
            throw new IllegalArgumentException("Invalid practical mark. Please enter a mark between 0 and 100.");
        }
        if (examMark != null && !isValidMark(examMark)) {
            throw new IllegalArgumentException("Invalid exam mark. Please enter a mark between 0 and 100.");
        }

        //stores marks inside the object
        this.testMark = testMark;
        this.practicalMark = practicalMark;
        this.examMark = examMark;
    }

    public double calculateFinalMark() {
        if (hasMissingMarks()) {
            //throws an exception if any of the marks are missing
            throw new IllegalStateException("Cannot calculate final mark because there are missing values.");
        }

        //calculates the contribution of each mark to the final mark
        double testContribution = testMark * test.getWeight();
        double practicalContribution = practicalMark * practical.getWeight();
        double examContribution = examMark * exam.getWeight();
        //calculates the final mark by summing the contributions
        double finalMark = testContribution + practicalContribution + examContribution;
        return finalMark;   
    }

    public double calculateAverage(){
        if (hasMissingMarks()) {
            //throws an exception if any of the marks are missing
            throw new IllegalStateException("Cannot calculate average mark because there are missing values.");
        }
        //adds all marks together to get the total
        double total = testMark + practicalMark + examMark;
        double average = total / 3; 
        return average;
    }
    
    public double getHighestMark() {
        if (hasMissingMarks()) {
            //throws an exception if any of the marks are missing
            throw new IllegalStateException("Cannot calculate highest mark because there are missing values.");
        }
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
        if (hasMissingMarks()) {
            //throws an exception if any of the marks are missing
            throw new IllegalStateException("Cannot calculate lowest mark because there are missing values.");
        }
        double lowest = testMark; // assume testMark is the lowest initially
        if (practicalMark < lowest) {
            lowest = practicalMark;
        }
        if (examMark < lowest) {
            lowest = examMark;
        }
        return lowest;
    }

    public boolean hasMissingMarks() {
        //checks if any of the marks are null
        if (testMark == null){
            return true;
        }
        if (practicalMark == null){
            return true;
        }
        if (examMark == null){
            return true;
        } 
            return false;
        
    }

    public String getMissingMarks(){
    //store a message indicating which marks are missing
    String missing = "";
    //finds out which marks are missing and returns a message indicating which ones are missing
    if(testMark == null){
        missing += "-Test.\n";
    }
    if(practicalMark == null){
        missing += "-Practical.\n";
    }
    if(examMark == null){
        missing += "-Exam.\n";
    }
    if (missing.isEmpty()) {
       return "No marks are missing.";
    }
    return "missing marks:\n" + missing;
}

    public void setTestMark(Double testMark) {
        //checks if the mark is valid, if not throws an exception
        if (testMark != null && !isValidMark(testMark)) {
            throw new IllegalArgumentException("Invalid test mark. Please enter a mark between 0 and 100.");
        }
        this.testMark = testMark;
    }

    public void setPracticalMark(Double practicalMark){
          //checks if the mark is valid, if not throws an exception
        if (practicalMark != null && !isValidMark(practicalMark)) {
            throw new IllegalArgumentException("Invalid practical mark. Please enter a mark between 0 and 100.");
        }
        this.practicalMark = practicalMark;
    }
    
    public void setExamMark(Double examMark) {
        //checks if the mark is valid, if not throws an exception
        if (examMark != null && !isValidMark(examMark)) {
            throw new IllegalArgumentException("Invalid exam mark. Please enter a mark between 0 and 100.");
        }
        this.examMark = examMark;
    }

    public Double getTestMark() {
        return testMark;
    }
    
    public Double getPracticalMark() {
        return practicalMark;
    }
    
    public Double getExamMark() {
        return examMark;
    }

    private boolean isValidMark(double mark) {
        //checks if the mark is between 0 and 100
        return mark >= 0 && mark <= 100;
    }
    
 

}