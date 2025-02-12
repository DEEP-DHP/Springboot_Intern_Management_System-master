package com.rh4.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderId;  // ID of the sender (Admin, Guide, Intern)

    @Column(nullable = false)
    private String receiverId;  // ID of the receiver (Admin, Guide, Intern)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(nullable = false)
    private Date timestamp;

    public Message() {
        this.timestamp = new Date();
    }

    public Message(Long id, String senderId, String receiverId, String messageText, Date timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public String getMessageText() {
        return messageText;
    }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
