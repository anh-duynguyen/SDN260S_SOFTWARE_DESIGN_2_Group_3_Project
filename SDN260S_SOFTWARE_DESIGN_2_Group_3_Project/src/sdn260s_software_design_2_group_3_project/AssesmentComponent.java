
package sdn260s_software_design_2_group_3_project;

public class AssesmentComponent {
    private String key;// unique identifier for the assessment
    private String lable;// name of the assessment
    private double weight;// weight of the assessment in percentage
    private double maxMark;// what the assesment is out of

    public AssesmentComponent(String key, String lable, double weight, double maxMark) {
        this.key = key;
        this.lable = lable;
        this.weight = weight;
        this.maxMark = maxMark;
    }

    public String getKey() {
        return key;
    }
    public String getLabel() {
        return lable;
    }
    public double getWeight() {
        return weight;
    }
    public double getMaxMark() {
        return maxMark;
    }

}
