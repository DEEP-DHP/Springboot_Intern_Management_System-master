package com.rh4.entities;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "weekly_report")
public class WeeklyReport {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_report")
	private long weeklyReportId;
	
	@JoinColumn(name = "intern_id")
	@ManyToOne
	private Intern intern;
	
	@JoinColumn(name = "group_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private GroupEntity group;
	
	private Date reportSubmittedDate;
	private Date deadline;
	
	private int weekNo;

	@Lob
	@Column(name = "submitted_pdf", columnDefinition = "LONGBLOB")
    private byte[] submittedPdf;
	
	@JoinColumn(name = "guide_id")
	@ManyToOne
	private Guide guide;
	
	@ManyToOne
	private MyUser replacedBy;
	
	private String status;

	@Column(name = "is_read", nullable = false)
	private int isRead = 0;  // 0 = Unread, 1 = Read (default is Unread)

	@Column(name = "g_is_read", nullable = false)
	private int GisRead = 0;  // 0 = Unread, 1 = Read (default is Unread)

	@Column(name = "changed_report", nullable = false)
	private boolean changedReport = false;

	public WeeklyReport() {
		super();
	}

	public WeeklyReport(long weeklyReportId, Intern intern, GroupEntity group, Date reportSubmittedDate, Date deadline,
			int weekNo, byte[] submittedPdf, Guide guide, MyUser replacedBy, String status, int isRead, int GisRead, boolean changedReport) {
		super();
		this.weeklyReportId = weeklyReportId;
		this.intern = intern;
		this.group = group;
		this.reportSubmittedDate = reportSubmittedDate;
		this.deadline = deadline;
		this.weekNo = weekNo;
		this.submittedPdf = submittedPdf;
		this.guide = guide;
		this.replacedBy = replacedBy;
		this.status = status;
		this.isRead = isRead;
		this.GisRead = GisRead;
		this.changedReport = changedReport;
	}

	public long getWeeklyReportId() {
		return weeklyReportId;
	}

	public void setWeeklyReportId(long weeklyReportId) {
		this.weeklyReportId = weeklyReportId;
	}

	public Intern getIntern() {
		return intern;
	}

	public void setIntern(Intern intern) {
		this.intern = intern;
	}

	public GroupEntity getGroup() {
		return group;
	}

	public void setGroup(GroupEntity group) {
		this.group = group;
	}

	public Date getReportSubmittedDate() {
		return reportSubmittedDate;
	}

	public void setReportSubmittedDate(Date reportSubmittedDate) {
		this.reportSubmittedDate = reportSubmittedDate;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public int getWeekNo() {
		return weekNo;
	}

	public void setWeekNo(int weekNo) {
		this.weekNo = weekNo;
	}

	public byte[] getSubmittedPdf() {
		return submittedPdf;
	}

	public void setSubmittedPdf(byte[] submittedPdf) {
		this.submittedPdf = submittedPdf;
	}

	public Guide getGuide() {
		return guide;
	}

	public void setGuide(Guide guide) {
		this.guide = guide;
	}

	public MyUser getReplacedBy() {
		return replacedBy;
	}

	public void setReplacedBy(MyUser replacedBy) {
		this.replacedBy = replacedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getIsRead() {
		return isRead;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}

	public int getGisRead() {
		return GisRead;
	}

	public void setGisRead(int GisRead) {
		this.GisRead = GisRead;
	}

	public boolean isChangedReport() {
		return changedReport;
	}
	public void setChangedReport(boolean changedReport) {
		this.changedReport = changedReport;
	}
}