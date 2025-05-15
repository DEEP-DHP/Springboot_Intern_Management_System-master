package com.rh4.entities;

import java.util.Date;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "admin")
public class Admin {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "contact_no" , unique = true)
    private Long contactNo;

    @Column(name = "email_id", unique = true)
    private String emailId;

    @Column(name="password")
    private String password;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = true)
    private Date createdAt;

	@Column(name = "digital_signature_path")
	private String digitalSignaturePath;

    public Admin() {
		super();
	}

	
	public Admin(Long adminId, String name, String location, Long contactNo, String emailId, String password,
			Date createdAt, String digitalSignaturePath) {
		super();
		this.adminId = adminId;
		this.name = name;
		this.location = location;
		this.contactNo = contactNo;
		this.emailId = emailId;
		this.password = password;
		this.createdAt = createdAt;
		this.digitalSignaturePath = digitalSignaturePath;

	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public Long getAdminId() {
		return adminId;
	}

	public void setAdminId(Long adminId) {
		this.adminId = adminId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Long getContactNo() {
		return contactNo;
	}

	public void setContactNo(Long contactNo) {
		this.contactNo = contactNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getDigitalSignaturePath() {
		return digitalSignaturePath;
	}

	public void setDigitalSignaturePath(String digitalSignaturePath) {
		this.digitalSignaturePath = digitalSignaturePath;
	}
}