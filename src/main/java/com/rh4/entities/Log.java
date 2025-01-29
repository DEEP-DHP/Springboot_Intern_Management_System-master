package com.rh4.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "contact_number", unique = true)
    @Pattern(regexp = "\\d{10}", message = "Contact number must be exactly 10 digits")
    private String contactNo;

    @Column(name = "email_id", unique = true)
    private String email;

    @Column(name = "college_name")
    private String collegeName;

    @Column(name = "branch_name")
    private String branch;

    @Column(name = "semester")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester must be at most 8")
    private int semester;

    @Column(name = "degree")
    private String degree;

    @Column(name = "domain")
    private String domain;

    @Column(name = "action")
    private String action; // "Rejected" or "Failed"
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public Log() { super();
    }
    public Log(Long id, String firstName, String lastName, String contactNo, String email, String collegeName, String branch, int semester, String degree, String domain, String action, String reason, LocalDateTime timestamp) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNo = contactNo;
        this.email = email;
        this.collegeName = collegeName;
        this.branch = branch;
        this.semester = semester;
        this.degree = degree;
        this.domain = domain;
        this.action = action;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}