
package sdn260s_software_design_2_group_3_project;

import java.io.*;
import java.util.*;

public class SDN260S_SOFTWARE_DESIGN_2_Group_3_Project {
    //A name for the file that will hole the data
    static final String FILE_NAME = "data/marks.csv";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in); 
        
        //choice program
        int c; 
        
        //Student manager
        StudentManager sm = new StudentManager();
        
        //AssesComp
        AssesmentComponent t1 = new AssesmentComponent("T1", "Test 1", 0.3, 80);
        AssesmentComponent t2 =new AssesmentComponent("T3", "Practical 1", 0.4, 80);
        AssesmentComponent t3 = new AssesmentComponent("FSA", "FISA", 0.4, 70);
        
        //Student object
        Student r = new Student("Rachel", 
                "S1001",
        80.0,
        80.0,
        80.0,
        t1,
        t2,
        t3);
        
        Student j = new Student("Jason",
        "S1002",
        45.0,
        56.0,
        75.0,
        t1,
        t2,
        t3);
        
        //Database
        Database f = new Database(FILE_NAME);
        
        //add student
        sm.addStudent(r);
        sm.addStudent(j);
        
        //saveData
        f.saveData(sm);
        
        //loadData
        //f.loadData(t1, t2, t3);
    }
    
}
