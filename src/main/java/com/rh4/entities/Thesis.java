package com.rh4.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Date;

@Entity
@Table(name = "thesis")
public class Thesis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thesis_id")
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "internname")
    private String internname;

    @Column(name = "contactNo")
    private String contactNo;

    @Column(name = "department")
    private String department;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "type")
    private String type;

    @CreationTimestamp
    @Column(name = "issueDate")
    private Date issueDate;

    @Column(name = "actualReturnDate")
    private Date actualReturnDate;

    @Column(name = "expectedDate")  // New field for expected return date
    private Date expectedDate;

    @Column(name = "location")  // New field for location
    private String location = "Intern";  // Default value is 'intern' when issued

    // Default constructor
    public Thesis() {}

    // Parameterized constructor
    public Thesis(long id, String title, String internname, String contactNo, String department, String purpose, String type, Date issueDate, Date actualReturnDate, Date expectedDate, String location) {
        this.id = id;
        this.title = title;
        this.internname = internname;
        this.contactNo = contactNo;
        this.department = department;
        this.purpose = purpose;
        this.type = type;
        this.issueDate = issueDate;
        this.actualReturnDate = actualReturnDate;
        this.expectedDate = expectedDate;
        this.location = location;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInternname() {
        return internname;
    }

    public void setInternname(String internname) {
        this.internname = internname;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(Date actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public Date getExpectedDate() {  // Getter for expected return date
        return expectedDate;
    }

    public void setExpectedDate(Date expectedDate) {  // Setter for expected return date
        this.expectedDate = expectedDate;
    }

    public String getLocation() {  // Getter for location
        return location;
    }

    public void setLocation(String location) {  // Setter for location
        this.location = location;
    }

    // Method to update location to 'admin' when the thesis is returned
    public void markReturned() {
        this.location = "admin";  // Change location to 'admin' when returned
    }
}