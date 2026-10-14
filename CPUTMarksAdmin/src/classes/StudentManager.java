/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

/**
 *
 * @author User
 */
import java.util.ArrayList;

public class StudentManager {
    // Stores all students in the system
    private ArrayList<Student> students;

    public StudentManager() {
        // Create an empty list ready to store students
        this.students = new ArrayList<>();
    }

    public void addStudent(Student student) {
        // Add the student to the list
        this.students.add(student);
    }

    public ArrayList<Student> getStudents() {
        // Return the list of students
        return this.students;
    }

public Student findStudentByID(String studentID) {
        // Search for a student by their ID
        for (Student student : students) {
            if (student.getStudentID().equals(studentID)) {
                return student;
            }
        }
        // Return null if no student is found with the given ID
        return null;
    }

}
