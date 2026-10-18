package classes;

public class AssessmentComponent {
    
    private String key;// unique identifier for the assessment
    private String label;// name of the assessment
    private double weight;// weight of the assessment as a decimal
    private double maxMark;// what the assessment is out of

    public AssessmentComponent(String key, String label, double weight, double maxMark) {
        this.key = key;
        this.label = label;
        this.weight = weight;
        this.maxMark = maxMark;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public double getWeight() {
        return weight;
    }

    public double getMaxMark() {
        return maxMark;
    }

}