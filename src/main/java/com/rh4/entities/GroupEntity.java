package com.rh4.entities;

import java.beans.JavaBean;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name="group_entity")
public class GroupEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private long id;
	
	@Column(name = "group_id")
    private String groupId;
	
	@Column(name = "project_definition")
	private String projectDefinition;

	@Lob
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	@Column(name = "project_definition_status")
	private String projectDefinitionStatus = "pending";
	
	@Column(name = "final_report_status")
	private String finalReportStatus = "pending";

	@Lob
	@Column(name = "project_definition_document", columnDefinition = "LONGBLOB")
	private byte[] projectDefinitionDocument;

	@Lob
	@Column(name = "final_report", columnDefinition = "LONGBLOB")
    private byte[] finalReport;
	
	@Column(name = "domain")
    private String domain;
	
	@JoinColumn(name = "guide_id")
	@ManyToOne
	public Guide guide;

	@Column(name = "confirmation_letter", nullable = false)
	private String confirmationLetter = "pending";

	@Column(name = "confirmation_letter_path")
	private String confirmationLetterPath;

	@Column(name = "final_report_status_updated_at")
	private LocalDateTime finalReportStatusUpdatedAt;

	@Column(name = "admin_final_report_status_updated_at")
	private LocalDateTime AdminfinalReportStatusUpdatedAt;

	@Column(name = "confirmation_timestamp")
	private LocalDateTime confirmationTimestamp;

	@Column(length = 1000)
	private String projectObjectives;

	@Column(length = 1000)
	private String expectedOutcomes;

	@Column(length = 1000)
	private String impactAreaTechnology;

	@Column(length = 1000)
	private String impactAreaDomain;

	@Column(length = 1000)
	private String impactAreaManagement;

	@Column(length = 1000)
	private String impactAreaOthers;

	@Column(length = 1000)
	private String impactStatement;

	private boolean projectDefinitionSubmitted;
		
   public GroupEntity() {
			super();
		}
  	
	public GroupEntity(long id, String groupId, String projectDefinition, String description, byte[] finalReport, String finalReportStatus,
		String projectDefinitionStatus, byte[] projectDefinitionDocument, String domain, Guide guide, String confirmationLetter, String confirmationLetterPath, LocalDateTime finalReportStatusUpdatedAt, LocalDateTime AdminfinalReportStatusUpdatedAt, LocalDateTime confirmationTimestamp, String projectObjectives, String expectedOutcomes, String impactAreaTechnology, String impactAreaDomain, String impactAreaManagement, String impactAreaOthers, String impactStatement, boolean projectDefinitionSubmitted) {
	super();
	this.id = id;
	this.groupId = groupId;
	this.projectDefinition = projectDefinition;
	this.description = description;
	this.finalReport = finalReport;
	this.finalReportStatus = finalReportStatus;
	this.domain = domain;
	this.projectDefinitionStatus = projectDefinitionStatus;
	this.projectDefinitionDocument = projectDefinitionDocument;
	this.guide = guide;
	this.confirmationLetter = confirmationLetter;
	this.confirmationLetterPath = confirmationLetterPath;
	this.finalReportStatusUpdatedAt = finalReportStatusUpdatedAt;
	this.AdminfinalReportStatusUpdatedAt = AdminfinalReportStatusUpdatedAt;
	this.confirmationTimestamp = confirmationTimestamp;
	this.projectObjectives = projectObjectives;
	this.expectedOutcomes = expectedOutcomes;
	this.impactAreaTechnology = impactAreaTechnology;
	this.impactAreaDomain = impactAreaDomain;
	this.impactAreaManagement = impactAreaManagement;
	this.impactAreaOthers = impactAreaOthers;
	this.impactStatement = impactStatement;
	this.projectDefinitionSubmitted = projectDefinitionSubmitted;
}
	public String getFinalReportStatus() {
		return finalReportStatus;
	}

	public void setFinalReportStatus(String finalReportStatus) {
		this.finalReportStatus = finalReportStatus;
	}

	public byte[] getFinalReport() {
		return finalReport;
	}

	public void setFinalReport(byte[] finalReport) {
		this.finalReport = finalReport;
	}

	public Guide getGuide() {
		return guide;
	}

	public void setGuide(Guide guide) {
		this.guide = guide;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getProjectDefinition() {
		return projectDefinition;
	}

	public void setProjectDefinition(String projectDefinition) {
		this.projectDefinition = projectDefinition;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProjectDefinitionStatus() {
		return projectDefinitionStatus;
	}

	public void setProjectDefinitionStatus(String projectDefinitionStatus) {
		this.projectDefinitionStatus = projectDefinitionStatus;
	}

	public byte[] getProjectDefinitionDocument() {
		return projectDefinitionDocument;
	}

	public void setProjectDefinitionDocument(byte[] projectDefinitionDocument) {
		this.projectDefinitionDocument = projectDefinitionDocument;
	}

	public String getConfirmationLetter() {
		return confirmationLetter;
	}

	public void setConfirmationLetter(String confirmationLetter) {
		this.confirmationLetter = confirmationLetter;
	}

	public String getConfirmationLetterPath() {
		return confirmationLetterPath;
	}

	public void setConfirmationLetterPath(String confirmationLetterPath) {
		this.confirmationLetterPath = confirmationLetterPath;
	}

	public LocalDateTime getFinalReportStatusUpdatedAt() {
		return finalReportStatusUpdatedAt;
	}

	public void setFinalReportStatusUpdatedAt(LocalDateTime finalReportStatusUpdatedAt) {
		this.finalReportStatusUpdatedAt = finalReportStatusUpdatedAt;
	}

	public LocalDateTime getAdminfinalReportStatusUpdatedAt() {
	   return AdminfinalReportStatusUpdatedAt;
	}
	public void setAdminfinalReportStatusUpdatedAt(LocalDateTime adminfinalReportStatusUpdatedAt) {
	   this.AdminfinalReportStatusUpdatedAt = adminfinalReportStatusUpdatedAt;
	}

	public LocalDateTime getConfirmationTimestamp() {
		return confirmationTimestamp;
	}

	public void setConfirmationTimestamp(LocalDateTime confirmationTimestamp) {
		this.confirmationTimestamp = confirmationTimestamp;
	}

	public String getProjectObjectives() {
		return projectObjectives;
	}

	public void setProjectObjectives(String projectObjectives) {
		this.projectObjectives = projectObjectives;
	}

	public String getExpectedOutcomes() {
		return expectedOutcomes;
	}

	public void setExpectedOutcomes(String expectedOutcomes) {
		this.expectedOutcomes = expectedOutcomes;
	}

	public String getImpactAreaTechnology() {
		return impactAreaTechnology;
	}

	public void setImpactAreaTechnology(String impactAreaTechnology) {
		this.impactAreaTechnology = impactAreaTechnology;
	}

	public String getImpactAreaDomain() {
		return impactAreaDomain;
	}

	public void setImpactAreaDomain(String impactAreaDomain) {
		this.impactAreaDomain = impactAreaDomain;
	}

	public String getImpactAreaManagement() {
		return impactAreaManagement;
	}

	public void setImpactAreaManagement(String impactAreaManagement) {
		this.impactAreaManagement = impactAreaManagement;
	}

	public String getImpactAreaOthers() {
		return impactAreaOthers;
	}

	public void setImpactAreaOthers(String impactAreaOthers) {
		this.impactAreaOthers = impactAreaOthers;
	}

	public String getImpactStatement() {
		return impactStatement;
	}

	public void setImpactStatement(String impactStatement) {
		this.impactStatement = impactStatement;
	}

	public boolean isProjectDefinitionSubmitted() {
		return projectDefinitionSubmitted;
	}

	public void setProjectDefinitionSubmitted(boolean projectDefinitionSubmitted) {
		this.projectDefinitionSubmitted = projectDefinitionSubmitted;
	}
}