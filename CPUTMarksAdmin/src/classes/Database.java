/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package classes;

/**
 *
 * @author User
 */
import java.io.*;
import java.nio.file.*;
import java.util.*;

/*Database.java will be responsible for interacting with the data.csv file.
It will be responsible for loading, saving, exporting and importing files.*/

public class Database{
    
    private String  fileName; //file in which data will be stored
    //private String  pathName;
    
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
        
        /*
        importCSV and exportCSV will be similar to the saveData and loadData
        functions, except they are built with the intention of having them interact
        with external files. They are there to handle files that the lecturers did not create
        inside the application. Because of this is best to assume that the layout of CSV files
        wont be the same as what is determined by saveData and loadData. 
        importCSV is meant to provide a path for when this occurs.
        it is meant to work around files that do not have the same structure and order of what is made 
        by the application.
        */
        
        public StudentManager importCSV(Path path, 
                AssessmentComponent test, 
                AssessmentComponent practical, 
                AssessmentComponent exam) throws IOException{
            
            StudentManager stm = new StudentManager();
            //empty manager
            
            if(!Files.exists(path)){
                return stm; //nothing to import
            }
            
            List <String> lines = Files.readAllLines(path);
            if(lines.isEmpty()){
                return stm;//lines empty
            }
            
            //the headerfields need to be scanned to find which collumns are present, 
            //and which ones are missing
             String[] hField = splitCSV(lines.get(0));
             Map<String, Integer> column_idx = new HashMap<>();
             
             for(int i = 0; i < hField.length; i++){
                 //for every single field, the name in the cell is trimmed and put into lowercase
                 //it is then put into the column index map 
                 String polishedName = hField[i].trim().toLowerCase();
                 column_idx.put(polishedName, i);
             } 
             
             //in order to keep the lecturers behaving with their files, they need to have required headers for their 
             //csv files, this restricts what the headers are allowed to be
             //if headers are not properly named, an error is thrown
         String[] requiredHeaders = {"studentid", "t1", "name", "practical", "fisa"};
         for (String header : requiredHeaders) {
             if (!column_idx.containsKey(header)) {
                 throw new IOException("Required CSV header not found: " + header);
             }
         }

         //every single header name in lowercase has their index number assigned to an integer
         int IDCol = column_idx.get("studentid");
         int testCol = column_idx.get("t1");
         int nameCol = column_idx.get("name");
         int pracCol = column_idx.get("practical");
         int examCol = column_idx.get("fisa");
         
         //for loop retrieves every parameter (id, mark, name), creates a student object 
         //with name and adds that object to a manager which is returned.
         //it will do this for every student inside the csv and their data
         for(int i = 1; i < lines.size(); i++){
             String line = lines.get(i);
             
             if(line.isBlank()){
                 continue;
             }
             
             String[] fields = splitCSV(line);//cells of csv file
             
             String studentID = fields[IDCol];
             String name = fields[nameCol];
             Double tMark = stringToMark(fields[testCol]);
             Double pMark = stringToMark(fields[pracCol]);
             Double eMark = stringToMark(fields[examCol]);
             
             Student student = new Student(name, studentID, tMark, pMark, eMark,
             test, practical, exam);
             stm.addStudent(student);
         }
         return stm;
}
        /*exportCSV function copies what is in the 
        application to a location that the lecturer
        picks*/
        public void exportCSV( StudentManager stm){
            
            Scanner sc = new Scanner(System.in);
            
            System.out.println("Enter a path: ");
            Path path = Paths.get(sc.nextLine());
            
            List<String> lines = new ArrayList<>();
            lines.add("StudentID, Name, T1, Practical, FISA");
            
            for(Student student: stm.getStudents()){
                IndividualMarksManager marks = student.getMarks();
                
               
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
            Path parent = path.getParent();
if (parent != null) {
    Files.createDirectories(parent);
}
            Files.write(path, lines);
        } catch (IOException ex) {
            System.getLogger(Database.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
 }
}