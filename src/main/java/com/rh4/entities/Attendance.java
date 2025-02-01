package com.rh4.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intern_id")
    private String internId;

    @Column(name = "month")
    private String month;

    @Column(name = "year")
    private int year;

    @Column(name = "total_working_days")
    private int totalWorkingDays;

    @Column(name = "total_present_days")
    private int totalPresentDays;

    @Column(name = "total_absent_days")
    private int totalAbsentDays;

    @Column(name = "attendance_percentage")
    private float attendancePercentage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "upload_date")
    private Date uploadDate = new Date();

    public Attendance() {super();}

    public Attendance(Long id, String internId, String month, int year, int totalWorkingDays, int totalPresentDays, int totalAbsentDays, float attendancePercentage, Date uploadDate) {
        super();
        this.id = id;
        this.internId = internId;
        this.month = month;
        this.year = year;
        this.totalWorkingDays = totalWorkingDays;
        this.totalPresentDays = totalPresentDays;
        this.totalAbsentDays = totalAbsentDays;
        this.attendancePercentage = attendancePercentage;
        this.uploadDate = uploadDate;
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public int getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(int totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public int getTotalPresentDays() {
        return totalPresentDays;
    }

    public void setTotalPresentDays(int totalPresentDays) {
        this.totalPresentDays = totalPresentDays;
    }

    public int getTotalAbsentDays() {
        return totalAbsentDays;
    }

    public void setTotalAbsentDays(int totalAbsentDays) {
        this.totalAbsentDays = totalAbsentDays;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(float attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }
}