package com.rh4.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "undertaking")
public class Undertaking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "intern_id", nullable = false)
    private String intern;

//    private boolean accepted;

    private LocalDateTime acceptedAt;

    public Undertaking() {
        this.createdAt = LocalDateTime.now();
    }

    public Undertaking(Long id, String filePath, LocalDateTime createdAt, String intern, LocalDateTime acceptedAt) {
        this.id = id;
        this.filePath = filePath;
        this.createdAt = createdAt;
        this.intern = intern;
//        this.accepted = accepted;
        this.acceptedAt = acceptedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIntern() {
        return intern;
    }

    public void setIntern(String intern) {
        this.intern = intern;
    }

//    public boolean isAccepted() {
//        return accepted;
//    }

//    public void setAccepted(boolean accepted) {
//        this.accepted = accepted;
//    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
