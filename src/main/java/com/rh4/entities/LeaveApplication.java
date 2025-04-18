package com.rh4.entities;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "leave_applications")
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intern_id")
    private String internId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fromDate")
    private LocalDate fromDate;

    private String remarks;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "toDate")
    private LocalDate toDate;

//    @Column(name = "proofDocument")
//    private String proofDocument;

    @Column(name = "guideApproval")
    private boolean guideApproval = false;

    @Column(name = "adminApproval")
    private boolean adminApproval = false;

    @Column(name = "status")
    private String status = "Pending"; // Default status

    @Column(nullable = false)
    private LocalDateTime submittedOn;

    @Column(nullable = false)
    private String leaveType; // "Half Day" or "Full Day"

    public LeaveApplication() { super();}

    public LeaveApplication(Long id, String internId, String subject, String body, LocalDate fromDate, String remarks, LocalDate toDate, boolean guideApproval, boolean adminApproval, String status, LocalDateTime submittedOn, String leaveType) {
        super();
        this.id = id;
        this.internId = internId;
        this.subject = subject;
        this.body = body;
        this.fromDate = fromDate;
        this.remarks = remarks;
        this.toDate = toDate;
        this.guideApproval = guideApproval;
        this.adminApproval = adminApproval;
        this.status = status;
        this.submittedOn = submittedOn;
        this.leaveType = leaveType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInternId() {
        return internId;
    }

    public void setInternId(String internId) {
        this.internId = internId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public boolean isGuideApproval() {
        return guideApproval;
    }

    public void setGuideApproval(boolean guideApproval) {
        this.guideApproval = guideApproval;
    }

    public boolean isAdminApproval() {
        return adminApproval;
    }

    public void setAdminApproval(boolean adminApproval) {
        this.adminApproval = adminApproval;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(LocalDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void updateStatus() {
        if (adminApproval && guideApproval) {
            this.status = "Approved";
        } else if (adminApproval || guideApproval) {
            this.status = "Rejected";
        }
    }
}