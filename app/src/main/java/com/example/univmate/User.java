package com.example.univmate;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    // Basic user information
    private String email;
    private String username;
    private String fullName;
    private String userType;

    // Student-specific fields
    private String college;
    private String course;
    private String yearLevel;
    private String section;

    // Faculty-specific fields
    private String role;
    private String employeeId;

    // Staff-specific fields
    private String department;

    // Required empty constructor for Firebase
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    // Convenience constructor
    public User(String email, String username, String fullName, String userType) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.userType = userType;
    }

    // Getters and Setters (required for Firebase data mapping)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType='" + userType + '\'' +
                ", college='" + college + '\'' +
                ", course='" + course + '\'' +
                ", yearLevel='" + yearLevel + '\'' +
                ", section='" + section + '\'' +
                ", role='" + role + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}