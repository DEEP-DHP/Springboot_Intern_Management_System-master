package com.rh4.entities;

import jakarta.persistence.*;

import java.sql.Date;
import java.time.LocalDate;

@Entity
@Table(name = "record")
public class RRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intern_id")
    private String internId;

    @Column(name = "groupId")
    private String groupId;

    @Column(name = "first_name")
    private String FirstName;

    @Column(name = "college_name")
    private String collegeName;

    @Column(name = "joining_date")
    private Date joiningDate;

    @Column(name = "planned_date")
    private Date plannedDate;

    @Column(name = "password")
    private String password;

    @Column(name = "media")
    private String media;

    @Column(name = "status")
    private String status;

    @Column(name = "project")
    private String project;

    @Column(name = "thesis")
    private String thesis;

    @Column(name = "others")
    private String others;

    @Column(name = "books")
    private String books;

    @Column(name = "subscription")
    private String subscription;

    @Column(name = "access_rights")
    private String accessRights;

    @Column(name = "pendrives")
    private String pendrives;

    @Column(name = "unused_cd")
    private String unusedCd;

    @Column(name = "backup_project")
    private String backupProject;

    @Column(name = "system")
    private String system;

    @Column(name = "identity_cards")
    private String identityCards;

    @Column(name = "stipend")
    private String stipend;

    @Column(name = "information")
    private String information;

    @Column(name = "weekly_report")
    private String weeklyReport;

    @Column(name = "attendance")
    private String attendance;

    public RRecord() { super(); }

    public RRecord(Long id, String internId, String FirstName, String groupId, String collegeName, Date joiningDate, Date plannedDate, String password, String media, String project, String thesis, String others, String books, String subscription, String accessRights, String pendrives, String unusedCd, String backupProject, String system, String identityCards, String stipend, String information, String weeklyReport, String attendance) {
        this.id = id;
        this.internId = internId;
        this.FirstName = FirstName;
        this.groupId = groupId;
        this.collegeName = collegeName;
        this.joiningDate = joiningDate;
        this.plannedDate = plannedDate;
        this.password = password;
        this.media = media;
        this.project = project;
        this.thesis = thesis;
        this.others = others;
        this.books = books;
        this.subscription = subscription;
        this.accessRights = accessRights;
        this.pendrives = pendrives;
        this.unusedCd = unusedCd;
        this.backupProject = backupProject;
        this.system = system;
        this.identityCards = identityCards;
        this.stipend = stipend;
        this.information = information;
        this.weeklyReport = weeklyReport;
        this.attendance = attendance;
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

    public String getFirstName() { return FirstName; }
    public void setFirstName(String FirstName) { this.FirstName = FirstName; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Date getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getThesis() {
        return thesis;
    }

    public void setThesis(String thesis) {
        this.thesis = thesis;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getBooks() {
        return books;
    }

    public void setBooks(String books) {
        this.books = books;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public String getPendrives() {
        return pendrives;
    }

    public void setPendrives(String pendrives) {
        this.pendrives = pendrives;
    }

    public String getUnusedCd() {
        return unusedCd;
    }

    public void setUnusedCd(String unusedCd) {
        this.unusedCd = unusedCd;
    }

    public String getBackupProject() {
        return backupProject;
    }

    public void setBackupProject(String backupProject) {
        this.backupProject = backupProject;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getIdentityCards() {
        return identityCards;
    }

    public void setIdentityCards(String identityCards) {
        this.identityCards = identityCards;
    }

    public String getStipend() {
        return stipend;
    }

    public void setStipend(String stipend) {
        this.stipend = stipend;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(String weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }
}
