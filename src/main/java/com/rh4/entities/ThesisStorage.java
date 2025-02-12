package com.rh4.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "thesis_storage")
public class ThesisStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private String internId;
    private String fileName;
    private String thesisTitle;
    private String filePath;
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;
    private String allowedInternId;

    public ThesisStorage(Long id, String fileName, String thesisTitle, String filePath, Date uploadDate, String allowedInternId) {
        this.id = id;
//        this.internId = internId;
        this.fileName = fileName;
        this.thesisTitle = thesisTitle;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.allowedInternId = allowedInternId;
    }
    public ThesisStorage() { super();}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public String getInternId() {
//        return internId;
//    }
//
//    public void setInternId(String internId) {
//        this.internId = internId;
//    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getThesisTitle() {
        return thesisTitle;
    }

    public void setThesisTitle(String thesisTitle) {
        this.thesisTitle = thesisTitle;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getAllowedInternId() {
        return allowedInternId;
    }

    public void setAllowedInternId(String allowedInternId) {
        this.allowedInternId = allowedInternId;
    }
}