package com.rh4.entities;


import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Past;

@Entity
@Table(name = "intern")
public class Intern {
    @Id
    @Column(name = "intern_id")
    private String internId;

    @Column(name = "first_name")
    private String firstName;

//    @Column(name = "last_name")
//    private String lastName;

    @Column(name = "contact_no", unique = true)
    private String contactNo;

    @Column(name = "email_id", unique = true)
    private String email;

    @Column(name = "college_name")
    private String collegeName;

//    @Column(name = "branch_name")
//    private String branch;

    @Lob
    @Column(name = "icard_image", columnDefinition = "LONGBLOB")
    private byte[] collegeIcardImage;

    @Lob
    @Column(name = "noc_pdf", columnDefinition = "LONGBLOB")
    private byte[] nocPdf;

    @Lob
    @Column(name = "resume_pdf", columnDefinition = "LONGBLOB")
    private byte[] resumePdf;

    @Lob
    @Column(name = "project_definition_form", columnDefinition = "LONGBLOB")
    private byte[] projectDefinitionForm;

    @Lob
    @Column(name = "extra_form", columnDefinition = "LONGBLOB")
    private byte[] extraForm;

    @Lob
    @Column(name = "extra_form2", columnDefinition = "LONGBLOB")
    private byte[] extraForm2;

    @Lob
    @Column(name = "passport_size_image", columnDefinition = "LONGBLOB")
    private byte[] passportSizeImage;

    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @Column(name = "semester")
    private int semester;

    @Column(name = "domain")
    private String domain;

    @Column(name = "password")
    private String password;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "date_of_birth")
    @Past(message = "Birth date must be in the past")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "college_guide_hod_name")
    private String collegeGuideHodName;

    @Column(name = "degree")
    private String degree;

    @Column(name = "aggregate_percentage")
    private Double aggregatePercentage;

    @Column(name = "project_definition_name")
    private String projectDefinitionName;

    private boolean undertakingAccepted = false;

    @ManyToOne
    private Guide guide;

    @Column(name = "joining_date")
    private Date joiningDate;

    @Column(name = "completion_date")
    private Date completionDate;

    @Column(name = "used_resource", columnDefinition = "TEXT")
    private String usedResource;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "cancellation_status")
    private String cancellationStatus;

    @Lob
    @Column(name = "icard_form", columnDefinition = "LONGBLOB", nullable = true)
    private byte[] icardForm;

    @Lob
    @Column(name = "registration_form", columnDefinition = "LONGBLOB")
    private byte[] registrationForm;

    @Lob
    @Column(name = "security_form", columnDefinition = "LONGBLOB")
    private byte[] securityForm;

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id") // Reference GroupEntity primary key 'id'
    private GroupEntity group;

    private String status;

    private LocalDateTime cancelTime;

    @Column(name = "first_login", nullable = false)
    private Integer firstLogin = 0; // Default value set to 0

    @Column(name = "security_verified", nullable = false)
    private int securityVerified; // 0 = not verified, 1 = verified

    @Column(name = "cancellation_remarks")
    private String cancellationRemarks;

    @Column(name = "cancel_file_path")
    private String cancelFilePath;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean isCredentialsGenerated;

    public int getSecurityVerified() {
        return securityVerified;
    }

    public void setSecurityVerified(int securityVerified) {
        this.securityVerified = securityVerified;
    }

    public Integer getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Integer firstLogin) {
        this.firstLogin = firstLogin;
    }

    @Column(name = "profile_updated", nullable = false)
    private int profileUpdated = 0;

    @Lob
    @Column(name = "sign", columnDefinition = "LONGBLOB")
    private byte[] sign;

    @Column(name = "icard_approved", nullable = false)
    private boolean icardApproved = false;

    @Column(name = "security_approved", nullable = false)
    private boolean securityApproved = false;

    @Column(name = "security_form_approved", nullable = false)
    private boolean securityFormApproved = false;

    @Column(name = "cancellation_file_path")
    private String cancellationFilePath;

    @Column(name = "alertMessage")
    private String alertMessage;

    @Column(name = "alertTimestamp")
    private LocalDateTime alertTimestamp;

    @Column(name = "alertSeen", nullable = false)
    private int alertSeen = 0;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "admin_approved_icard", nullable = false)
    private boolean adminApprovedIcard = false;

    @Column(name = "guide_approved_icard", nullable = false)
    private boolean guideApprovedIcard = false;

    @Column(name = "intern_registration_form", nullable = false)
    private boolean internRegistrationForm = false;

    @Column(name = "admin_security_approved", nullable = false)
    private boolean adminSecurityApproved = false;

    @Column(name = "admin_security_form_approved", nullable = false)
    private boolean adminSecurityFormApproved = false;

    @Column(name = "admin_registration_approved", nullable = false)
    private boolean adminRegistrationApproved = false;

    public Intern() {
        super();
    }

    public Intern(String internId, String firstName, String contactNo, String email,
                  String collegeName, byte[] collegeIcardImage, byte[] nocPdf, byte[] projectDefinitionForm, byte[] extraForm, byte[] extraForm2, byte[] resumePdf, int semester,
                  String permanentAddress, Date dateOfBirth, String gender, String collegeGuideHodName, String degree, Double aggregatePercentage, String projectDefinitionName, String cancellationStatus,
                  Guide guide, String domain, Date joiningDate, Date completionDate, String password, byte[] icardForm, byte[] registrationForm, byte[] securityForm,
                  String usedResource, LocalDateTime createdAt, LocalDateTime updatedAt, GroupEntity group, boolean isActive, String status, LocalDateTime cancelTime, boolean isCredentialsGenerated, int profileUpdated, byte[] sign, boolean icardApproved, boolean securityApproved, boolean securityFormApproved, String cancellationFilePath, String alertMessage, LocalDateTime alertTimestamp, int alertSeen, String currentAddress, boolean adminApprovedIcard, boolean guideApprovedIcard, boolean internRegistrationForm, boolean adminSecurityApproved, boolean adminSecurityFormApproved, boolean adminRegistrationApproved) {
        super();
        this.internId = internId;
        this.firstName = firstName;
        this.isActive = isActive;
//        this.lastName = lastName;
        this.contactNo = contactNo;
        this.email = email;
        this.collegeName = collegeName;
//        this.branch = branch;
        this.collegeIcardImage = collegeIcardImage;
        this.nocPdf = nocPdf;
        this.projectDefinitionForm = projectDefinitionForm;
        this.extraForm = extraForm;
        this.extraForm2 = extraForm2;
        this.resumePdf = resumePdf;
        this.semester = semester;
        this.permanentAddress = permanentAddress;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.password = password;
        this.collegeGuideHodName = collegeGuideHodName;
        this.degree = degree;
        this.aggregatePercentage = aggregatePercentage;
        this.projectDefinitionName = projectDefinitionName;
        this.guide = guide;
        this.securityForm = securityForm;
        this.icardForm = icardForm;
        this.registrationForm = registrationForm;
        this.domain = domain;
        this.joiningDate = joiningDate;
        this.completionDate = completionDate;
        this.usedResource = usedResource;
        this.cancellationStatus = cancellationStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.group = group;
        this.status = status;
        this.cancelTime = cancelTime;
        this.isCredentialsGenerated = isCredentialsGenerated;
        this.profileUpdated = profileUpdated;
        this.sign = sign;
        this.icardApproved = icardApproved;
        this.securityApproved = securityApproved;
        this.securityFormApproved = securityFormApproved;
        this.cancellationFilePath = cancellationFilePath;
        this.alertMessage = alertMessage;
        this.alertTimestamp = alertTimestamp;
        this.alertSeen = alertSeen;
        this.currentAddress = currentAddress;
        this.adminApprovedIcard = adminApprovedIcard;
        this.guideApprovedIcard = guideApprovedIcard;
        this.internRegistrationForm = internRegistrationForm;
        this.adminSecurityApproved = adminSecurityApproved;
        this.adminSecurityFormApproved = adminSecurityFormApproved;
        this.adminRegistrationApproved = adminRegistrationApproved;
    }

    public Intern(String firstName, String contactNo, String email, String collegeName, Date joiningDate, Date completionDate,
                   String degree, String password, byte[] collegeIcardImage, byte[] nocPdf, byte[] resumePdf, byte[] passportSizeImage, int semester, String permanentAddress, String collegeGuideHodName, double aggregatePercentage, byte[] sign, String gender, Date dateOfBirth, String usedResource, String domain, GroupEntity group) {
        super();
        this.firstName = firstName;
//        this.lastName = lastName;
        this.contactNo = contactNo;
        this.email = email;
        this.collegeName = collegeName;
        this.joiningDate = joiningDate;
        this.completionDate = completionDate;
//        this.branch = branch;
        this.degree = degree;
        this.collegeIcardImage = collegeIcardImage;
        this.nocPdf = nocPdf;
        this.resumePdf = resumePdf;
        this.passportSizeImage = passportSizeImage;
        this.semester = semester;
        this.permanentAddress = permanentAddress;
        this.collegeGuideHodName = collegeGuideHodName;
        this.aggregatePercentage = aggregatePercentage;
        this.sign = sign;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.usedResource = usedResource;
        this.password = password;
        this.domain = domain;
        this.group = group;
    }

    public byte[] getPassportSizeImage() {
        return passportSizeImage;
    }

    public void setPassportSizeImage(byte[] passportSizeImage) {
        this.passportSizeImage = passportSizeImage;
    }

    public byte[] getIcardForm() {
        return icardForm;
    }

    public void setIcardForm(byte[] icardForm) {
        this.icardForm = icardForm;
    }

    public byte[] getRegistrationForm() {
        return registrationForm;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setRegistrationForm(byte[] registrationForm) {
        this.registrationForm = registrationForm;
    }

    public byte[] getSecurityForm() {
        return securityForm;
    }

    public void setSecurityForm(byte[] securityForm) {
        this.securityForm = securityForm;
    }

    public String getCancellationStatus() {
        return cancellationStatus;
    }

    public void setCancellationStatus(String cancellationStatus) {
        this.cancellationStatus = cancellationStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

//    public String getLastName() {
//        return lastName;
//    }

//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public String getBranch() {
//        return branch;
//    }
//
//    public void setBranch(String branch) {
//        this.branch = branch;
//    }

    public byte[] getCollegeIcardImage() {
        return collegeIcardImage;
    }

    public void setCollegeIcardImage(byte[] collegeIcardImage) {
        this.collegeIcardImage = collegeIcardImage;
    }

    public byte[] getNocPdf() {
        return nocPdf;
    }

    public void setNocPdf(byte[] nocPdf) {
        this.nocPdf = nocPdf;
    }

    public byte[] getResumePdf() {
        return resumePdf;
    }

    public void setResumePdf(byte[] resumePdf) {
        this.resumePdf = resumePdf;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getInternId() {
        return internId;
    }

    public void setInternId(String internId) {
        this.internId = internId;
    }


    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCollegeGuideHodName() {
        return collegeGuideHodName;
    }

    public void setCollegeGuideHodName(String collegeGuideHodName) {
        this.collegeGuideHodName = collegeGuideHodName;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Double getAggregatePercentage() {
        return aggregatePercentage;
    }

    public void setAggregatePercentage(Double aggregatePercentage) {
        this.aggregatePercentage = aggregatePercentage;
    }

    public String getProjectDefinitionName() {
        return projectDefinitionName;
    }

    public void setProjectDefinitionName(String projectDefinitionName) {
        this.projectDefinitionName = projectDefinitionName;
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String getUsedResource() {
        return usedResource;
    }

    public void setUsedResource(String usedResource) {
        this.usedResource = usedResource;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public GroupEntity getGroup() {
        return group;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Object getGroupEntity() {
        return group;
    }

    public boolean isUndertakingAccepted() {
        return undertakingAccepted;
    }

    public void setUndertakingAccepted(boolean undertakingAccepted) {
        this.undertakingAccepted = undertakingAccepted;
    }

    public byte[] getProjectDefinitionForm() {
        return projectDefinitionForm;
    }

    public void setProjectDefinitionForm(byte[] projectDefinitionForm) {
        this.projectDefinitionForm = projectDefinitionForm;
    }

    public byte[] getExtraForm() {
        return extraForm;
    }

    public void setExtraForm(byte[] extraForm) {
        this.extraForm = extraForm;
    }

    public byte[] getExtraForm2() {
        return extraForm2;
    }

    public void setExtraForm2(byte[] extraForm2) {
        this.extraForm2 = extraForm2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCancellationRemarks() {
        return cancellationRemarks;
    }

    public void setCancellationRemarks(String cancellationRemarks) {
        this.cancellationRemarks = cancellationRemarks;
    }

    public String getCancelFilePath() {
        return cancelFilePath;
    }

    public void setCancelFilePath(String cancelFilePath) {
        this.cancelFilePath = cancelFilePath;
    }

    public boolean getIsCredentialsGenerated() {
        return isCredentialsGenerated;
    }

    public void setIsCredentialsGenerated(boolean credentialsGenerated) {
        isCredentialsGenerated = credentialsGenerated;
    }

    public int getProfileUpdated() {
        return profileUpdated;
    }

    public void setProfileUpdated(int profileUpdated) {
        this.profileUpdated = profileUpdated;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }

    public boolean isIcardApproved() {
        return icardApproved;
    }

    public void setIcardApproved(boolean icardApproved) {
        this.icardApproved = icardApproved;
    }

    public boolean isSecurityApproved() {
        return securityApproved;
    }

    public void setSecurityApproved(boolean securityApproved) {
        this.securityApproved = securityApproved;
    }

    public boolean isSecurityFormApproved() {
        return securityFormApproved;
    }

    public void setSecurityFormApproved(boolean securityFormApproved) {
        this.securityFormApproved = securityFormApproved;
    }

    public String getCancellationFilePath() {
        return cancellationFilePath;
    }

    public void setCancellationFilePath(String cancellationFilePath) {
        this.cancellationFilePath = cancellationFilePath;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public LocalDateTime getAlertTimestamp() {
        return alertTimestamp;
    }

    public void setAlertTimestamp(LocalDateTime alertTimestamp) {
        this.alertTimestamp = alertTimestamp;
    }

    public int getAlertSeen() {
        return alertSeen;
    }

    public void setAlertSeen(int alertSeen) {
        this.alertSeen = alertSeen;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public boolean isAdminApprovedIcard() {
        return adminApprovedIcard;
    }

    public void setAdminApprovedIcard(boolean adminApprovedIcard) {
        this.adminApprovedIcard = adminApprovedIcard;
    }

    public boolean isGuideApprovedIcard() {
        return guideApprovedIcard;
    }

    public void setGuideApprovedIcard(boolean guideApprovedIcard) {
        this.guideApprovedIcard = guideApprovedIcard;
    }

    public boolean isInternRegistrationForm() {
        return internRegistrationForm;
    }

    public void setInternRegistrationForm(boolean internRegistrationForm) {
        this.internRegistrationForm = internRegistrationForm;
    }

    public boolean isAdminSecurityApproved() {
        return adminSecurityApproved;
    }

    public void setAdminSecurityApproved(boolean adminSecurityApproved) {
        this.adminSecurityApproved = adminSecurityApproved;
    }

    public boolean isAdminSecurityFormApproved() {
        return adminSecurityFormApproved;
    }

    public void setAdminSecurityFormApproved(boolean adminSecurityFormApproved) {
        this.adminSecurityFormApproved = adminSecurityFormApproved;
    }

    public boolean isAdminRegistrationApproved() {
        return adminRegistrationApproved;
    }

    public void setAdminRegistrationApproved(boolean adminRegistrationApproved) {
        this.adminRegistrationApproved = adminRegistrationApproved;
    }
}