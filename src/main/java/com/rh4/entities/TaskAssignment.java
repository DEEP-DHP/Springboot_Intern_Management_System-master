package com.rh4.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "task_assignment")
public class TaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intern_id", nullable = false)
    private String intern;

    private String assignedById;
    private String assignedByRole;
    private String taskDescription;
    private Date startDate;
    private Date endDate;
    private String status;
    private String proofAttachment;
    private boolean isApproved;
    private LocalDateTime completionTimestamp;
    public TaskAssignment() { super();}

    public TaskAssignment(Long id, String intern, String assignedById, String assignedByRole, String taskDescription, Date startDate, Date endDate, String status, String proofAttachment, boolean isApproved) {
        super();
        this.id = id;
        this.intern = intern;
        this.assignedById = assignedById;
        this.assignedByRole = assignedByRole;
        this.taskDescription = taskDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.proofAttachment = proofAttachment;
        this.isApproved = isApproved;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntern() {
        return intern;
    }

    public void setIntern(String intern) {
        this.intern = intern;
    }

    public String getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(String assignedById) {
        this.assignedById = assignedById;
    }

    public String getAssignedByRole() {
        return assignedByRole;
    }

    public void setAssignedByRole(String assignedByRole) {
        this.assignedByRole = assignedByRole;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProofAttachment() {
        return proofAttachment;
    }

    public void setProofAttachment(String proofAttachment) {
        this.proofAttachment = proofAttachment;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public LocalDateTime getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(LocalDateTime completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }
}
