package classes;

/**
 *
 * @author User
 */
import java.io.*;
import java.util.*;
import java.nio.file.*;

/*Database.java will be responsible for interacting with the data.csv file.
It will be responsible for loading, saving, exporting and importing files.*/

public class Database{
    
    private String  fileName; //file in which data will be stored
    
    public Database(String fileName){
        this.fileName = fileName; //file to be instantiated
    }
    
    public String getfileName(){
        return fileName;
    }
    /*To prevent the CSV from being broken by commas, escapeCSV 
    wraps a field in quotes if it contains a comma*/
    private String escapeCSV(String value){
        if(value.contains(",")){
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /*Splits CSV lines/rows into fields/collumns, with respect to commas*/
    private String[] splitCSV(String line){
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for(int i = 0; i < line.length(); i++){
            char c = line.charAt(i);
            if(c == '"'){
                inQuotes = !inQuotes;
            } else if(c == ',' && !inQuotes){
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(String[]::new);
    }
    /*Converts a null mark into text as a placeholder for missing marks*/
    private String markToString(Double mark){
        return (mark == null) ? "NULL" : String.valueOf(mark);
        
    }
    /*Converts any placeholder text into a null*/
    private Double stringToMark(String value){
        if(value == null || value.equals("NULL") || 
                value.isBlank()){
            return null;
        }
        return Double.valueOf(value); //returns double value
    }
    
    //function to give file a name if one does not exist already
    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    
    /*saveData function will save every student's ID, name and marks to CSV file
    referenced by fileName. It will overwrite the file, creating a new updated version if it 
    already exists.*/
    public void saveData(StudentManager studentManager){
        
        List<String> lines = new ArrayList<>();//a one-dimensional array for rows
        lines.add("StudentID, Name, T1, Practical, FISA");
        //header row
        
        /*The for loop will run for every student, filling a row with 
        student information, joining them with commas and adding the row to file.*/
        for(Student student: studentManager.getStudents()){
            IndividualMarksManager marks = student.getMarks();//marks object gets student marks
            
            //String row joins student information and marks into a single row entry
            String row;
            row = String.join(",",
                    escapeCSV(student.getStudentID()),
                    escapeCSV(student.getName()),
                    markToString(marks.getTestMark()),
                    markToString(marks.getPracticalMark()),
                    markToString(marks.getExamMark())
            );
            lines.add(row);//adds row to file
        }
        try {
            //exception to be declared in case of failure
            Path parent = Paths.get(fileName).getParent();
if (parent != null) {
    Files.createDirectories(parent);
}
            Files.write(Paths.get(fileName), lines);
        } catch (IOException ex) {
            System.getLogger(Database.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }
    
    /*loadData loads students and their back from CSV file referenced by fileName
        , and rebuilds a StudentManager object populated by them.
        Three AssessComp objects are passed for every 
        Student/IMM for them to know the weight, of tests, pracs and exam comps.*/
        public StudentManager loadData(AssessmentComponent test, 
                AssessmentComponent prac,
                AssessmentComponent exam) throws IOException{
            StudentManager studentManager = new StudentManager();
            if(!Files.exists(Paths.get(fileName))){
                return studentManager; //no file saved yet so an empty manager is returned
            }
            
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            if(lines.isEmpty()){
                return studentManager;// file exists but has nothing in it
            }
            
            //skip header row (index 0)
            for(int i = 1; i < lines.size(); i++){
                String line = lines.get(i);
                if(line.isBlank()){
                    continue;//continue running program if header is blank
                }
                
                String[] fields = splitCSV(line);
                String stu_ID = fields[0];
                String name = fields[1];
                Double testMark = stringToMark(fields[2]);
                Double pracMark = stringToMark(fields[3]);
                Double examMark = stringToMark(fields[4]);
                
                Student student = new Student(name, stu_ID, testMark, 
                pracMark, examMark, test, prac, exam);
                studentManager.addStudent(student);
            }
            return studentManager;
        }
}