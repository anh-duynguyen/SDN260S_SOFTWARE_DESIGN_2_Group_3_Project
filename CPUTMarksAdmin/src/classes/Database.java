package classes;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Database {

    private String fileName;

    public Database(String fileName) {
        this.fileName = fileName;
    }

    public String getfileName() {
        return fileName;
    }

    private String escapeCSV(String value) {
        if (value.contains(",")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] splitCSV(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0;
                i < line.length();
                i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(String[]::new);
    }

    private String markToString(Double mark) {
        return (mark == null) ? "NULL" : String.valueOf(mark);
    }

    private Double stringToMark(String value) {
        if (value == null || value.equals("NULL") || value.isBlank()) {
            return null;
        }
        return Double.valueOf(value);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void saveSemData(StudentManager studentManager) {
        // Look up context name via active runtime tracks to embed inside file comments
        String actualCourseName = "Module Profile";
        File currentFile = new File(fileName);
        String extractedCode = currentFile.getName().substring(0, currentFile.getName().toLowerCase().lastIndexOf("_marks.csv")).toUpperCase();

        List<String> lines = new ArrayList<>();
        // Inject dynamic tag on row 1
        lines.add("#CourseName:" + actualCourseName);
        lines.add("StudentID, Name, T1, T2, FISA, Final %, Grade, Status");

        for (Student student
                : studentManager.getStudents()) {
            IndividualMarksManager marks = student.getMarks();
            String finalMark = "--";
            String symbol = "--";
            String status = "Complete";

            if (marks.hasMissingMarks()) {
                status = "Missing" + CollectiveMarksManager.describeMissing(student);
            } else {
                double mark = marks.calculateFinalMark();
                finalMark = String.format("%.1f", mark);
                symbol = CollectiveMarksManager.letterGrade(mark);
            }

            String row = String.join(",",
                    escapeCSV(student.getStudentID()),
                    escapeCSV(student.getName()),
                    markToString(marks.getTestMark()),
                    markToString(marks.getPracticalMark()),
                    markToString(marks.getExamMark()),
                    finalMark,
                    symbol,
                    status
            );
            lines.add(row);
        }
        try {
            Path parent = Paths.get(fileName).getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(Paths.get(fileName), lines);
        } catch (IOException ex) {
            System.getLogger(Database.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public StudentManager loadData(AssessmentComponent test, AssessmentComponent prac, AssessmentComponent exam) throws IOException {
        StudentManager studentManager = new StudentManager();
        if (!Files.exists(Paths.get(fileName))) {
            return studentManager;
        }

        List<String> lines = Files.readAllLines(Paths.get(fileName));
        if (lines.isEmpty()) {
            return studentManager;
        }

        for (int i = 0;
                i < lines.size();
                i++) {
            String line = lines.get(i);
            // Skip comments or metadata markers
            if (line.isBlank() || line.trim().startsWith("#") || line.toLowerCase().contains("studentid")) {
                continue;
            }

            String[] fields = splitCSV(line);
            String stu_ID = fields[0];
            String name = fields[1];
            Double testMark = stringToMark(fields[2]);
            Double pracMark = stringToMark(fields[3]);
            Double examMark = stringToMark(fields[4]);

            Student student = new Student(name, stu_ID, testMark, pracMark, examMark, test, prac, exam);
            studentManager.addStudent(student);
        }
        return studentManager;
    }

    public StudentManager importCSV(Path path, AssessmentComponent test, AssessmentComponent practical, AssessmentComponent exam) throws IOException {
        StudentManager stm = new StudentManager();
        if (!Files.exists(path)) {
            return stm;
        }

        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty()) {
            return stm;
        }

        String[] hField = splitCSV(lines.get(0));
        Map<String, Integer> column_idx = new HashMap<>();

        for (int i = 0;
                i < hField.length;
                i++) {
            String polishedName = hField[i].trim().toLowerCase();
            column_idx.put(polishedName, i);
        }

        String[] requiredHeaders = {"studentid", "t1", "name", "practical", "fisa"};
        for (String header
                : requiredHeaders) {
            if (!column_idx.containsKey(header)) {
                throw new IOException("Required CSV header not found: " + header);
            }
        }

        int IDCol = column_idx.get("studentid");
        int testCol = column_idx.get("t1");
        int nameCol = column_idx.get("name");
        int pracCol = column_idx.get("practical");
        int examCol = column_idx.get("fisa");

        for (int i = 1;
                i < lines.size();
                i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.trim().startsWith("#")) {
                continue;
            }

            String[] fields = splitCSV(line);
            String studentID = fields[IDCol];
            String name = fields[nameCol];
            Double tMark = stringToMark(fields[testCol]);
            Double pMark = stringToMark(fields[pracCol]);
            Double eMark = stringToMark(fields[examCol]);

            Student student = new Student(name, studentID, tMark, pMark, eMark, test, practical, exam);
            stm.addStudent(student);
        }
        return stm;
    }

    public void exportCSVSem(StudentManager stm, Path path) {
        List<String> lines = new ArrayList<>();
        lines.add("StudentID, Name, T1, T2, FISA, Final %, Symbol, Status");

        for (Student student
                : stm.getStudents()) {
            IndividualMarksManager marks = student.getMarks();
            String finalMark = "--";
            String symbol = "--";
            String status = "Complete";

            if (marks.hasMissingMarks()) {
                status = "Missing" + CollectiveMarksManager.describeMissing(student);
            } else {
                double mark = marks.calculateFinalMark();
                finalMark = String.format("%.1f", mark);
                symbol = CollectiveMarksManager.letterGrade(mark);
            }

            String row = String.join(",",
                    escapeCSV(student.getStudentID()),
                    escapeCSV(student.getName()),
                    markToString(marks.getTestMark()),
                    markToString(marks.getPracticalMark()),
                    markToString(marks.getExamMark()),
                    finalMark,
                    symbol,
                    status
            );
            lines.add(row);
        }

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines);
        } catch (IOException ex) {
            System.getLogger(Database.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public void exportCSVYear(StudentManager stm, Path path) {
        List<String> lines = new ArrayList<>();
        lines.add("StudentID, Name, T1, T2, FISA, Final %, Symbol, Status");

        for (Student student
                : stm.getStudents()) {
            IndividualMarksManager marks = student.getMarks();
            String finalMark = "--";
            String symbol = "--";
            String status = "Complete";

            if (marks.hasMissingMarks()) {
                status = "Missing" + CollectiveMarksManager.describeMissing(student);
            } else {
                double mark = marks.calculateFinalMark();
                finalMark = String.format("%.1f", mark);
                symbol = CollectiveMarksManager.letterGrade(mark);
            }

            String row = String.join(",",
                    escapeCSV(student.getStudentID()),
                    escapeCSV(student.getName()),
                    markToString(marks.getTestMark()),
                    markToString(marks.getPracticalMark()),
                    markToString(marks.getExamMark()),
                    finalMark,
                    symbol,
                    status
            );
            lines.add(row);
        }

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines);
        } catch (IOException ex) {
            System.getLogger(Database.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public void saveYearData(StudentManager studentManager) {
        List lines = new ArrayList<>();
        lines.add("StudentID, Name, T1, T2, FISA, Final %, Grade, Status");
        for (Student student
                : studentManager.getStudents()) {
            IndividualMarksManager marks = student.getMarks();
            String finalMark = "--";
            String symbol = "--";
            String status = "Complete";
            if (marks.hasMissingMarks()) {
                status = "Missing" + CollectiveMarksManager.describeMissing(student);
            } else {
                double mark = marks.calculateFinalMark();
                finalMark = String.format("%.1f", mark);
                symbol = CollectiveMarksManager.letterGrade(mark);
            }
            String row = String.join(",",
                    escapeCSV(student.getStudentID()),
                    escapeCSV(student.getName()),
                    markToString(marks.getTestMark()),
                    markToString(marks.getPracticalMark()),
                    markToString(marks.getExamMark()),
                    finalMark,
                    symbol,
                    status
            );
            lines.add(row);
        }
        try {
            Path parent = Paths.get(fileName).getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(Paths.get(fileName), lines);
        } catch (IOException ex) {
            System.getLogger(Database.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
