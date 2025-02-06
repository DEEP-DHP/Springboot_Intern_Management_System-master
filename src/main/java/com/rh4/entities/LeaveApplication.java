package com.rh4.entities;

import jakarta.persistence.*;

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

    @Column(name = "fromDate")
    private Date fromDate;

    @Column(name = "toDate")
    private Date toDate;

    @Column(name = "proofDocument")
    private String proofDocument;

    @Column(name = "guideApproval")
    private boolean guideApproval = false;

    @Column(name = "adminApproval")
    private boolean adminApproval = false;

    @Column(name = "status")
    private String status = "Pending"; // Default status

    @Column(nullable = false)
    private LocalDateTime submittedOn;

    public void updateStatus() {
        if (guideApproval && adminApproval) {
            this.status = "Approved";
        } else if (!guideApproval || !adminApproval) {
            this.status = "Rejected";
        }
    }

    public LeaveApplication() { super();}

    public LeaveApplication(Long id, String internId, String subject, String body, Date fromDate, Date toDate, String proofDocument, boolean guideApproval, boolean adminApproval, String status, LocalDateTime submittedOn) {
        super();
        this.id = id;
        this.internId = internId;
        this.subject = subject;
        this.body = body;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.proofDocument = proofDocument;
        this.guideApproval = guideApproval;
        this.adminApproval = adminApproval;
        this.status = status;
        this.submittedOn = submittedOn;
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

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getProofDocument() {
        return proofDocument;
    }

    public void setProofDocument(String proofDocument) {
        this.proofDocument = proofDocument;
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
}