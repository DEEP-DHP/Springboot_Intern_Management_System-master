package com.rh4.services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import com.rh4.entities.Guide;
import com.rh4.entities.Intern;
import com.rh4.repositories.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.rh4.entities.GroupEntity;
import com.rh4.entities.WeeklyReport;
import com.rh4.repositories.WeeklyReportRepo;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WeeklyReportService {

	@Autowired
	private WeeklyReportRepo weeklyReportRepo;
	@Autowired
	private GroupService groupService;
	@Autowired
	private GroupRepo groupRepo;

	public int getRecentWeekNo(GroupEntity group) {
			List<WeeklyReport> reports = weeklyReportRepo.getRecentWeekNo(group);
	    if (!reports.isEmpty()) {
	        return reports.get(0).getWeekNo() + 1;
	    } else {
	        // Handle the case where no reports are found
	        return 1; // Or any other appropriate value
	    }
	}

	public void addReport(WeeklyReport weeklyReport) {
		weeklyReportRepo.save(weeklyReport);

	}

	public List<WeeklyReport> getReportsByGroupId(long id) {
		return weeklyReportRepo.findAllByGroupId(id);
	}

	public List<WeeklyReport> getReportsByGuideId(long id) {
		return weeklyReportRepo.findAllByGuideId(id);
	}

	public WeeklyReport getReportByWeekNoAndGroupId(int weekNo, GroupEntity group) {
		return weeklyReportRepo.findByWeekNoAndGroup(weekNo,group);
	}

	public List<WeeklyReport> getAllReports() {
		return weeklyReportRepo.findAll();
	}

	public WeeklyReport getReportByInternIdAndWeekNo(String internId, int weekNo) {
		return weeklyReportRepo.findByInternInternIdAndWeekNo(internId, weekNo);
	}

	public List<WeeklyReport> getReportsByDate(Date date) {
		return weeklyReportRepo.findByReportSubmittedDate(date);
	}

	public List<WeeklyReport> getReportsByYear(int year) {
		return weeklyReportRepo.findReportsByYear(year);
	}

	public void submitAdminWeeklyReport(Long groupId, Intern internId, Guide guide, int weekNo, Date deadline, String status, MultipartFile submittedPdf) {
		GroupEntity group = groupService.getGroupById(groupId);

		WeeklyReport report = new WeeklyReport();
		report.setGroup(group);
		report.setIntern(internId);
		report.setGuide(guide);
		report.setWeekNo(weekNo);
		report.setDeadline(deadline);
		report.setStatus(status);
		report.setReportSubmittedDate(new Date());

		try {
			report.setSubmittedPdf(submittedPdf.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		weeklyReportRepo.save(report);
	}

	public List<WeeklyReport> getOverdueReports() {
		return weeklyReportRepo.findOverdueReports();
	}

	public Map<String, Integer> getPendingReports() {
		List<GroupEntity> allGroups = groupRepo.findAll(); // Get all groups
		List<WeeklyReport> submittedReports = weeklyReportRepo.findAll(); // Get all reports

		Map<String, Integer> pendingReports = new HashMap<>();

		for (GroupEntity group : allGroups) {
			int currentWeek = getCurrentWeekNumber();
			int overdueWeekNo = -1;

			for (int week = 1; week <= currentWeek; week++) {
				boolean isSubmitted = false;

				for (WeeklyReport report : submittedReports) {
					// ✅ Use '==' instead of '.equals()' for primitive long comparison
					if (report.getGroup().getId() == group.getId() && report.getWeekNo() == week) {
						isSubmitted = true;
						break;
					}
				}

				Date deadline = calculateDeadline(week);
				if (!isSubmitted && deadline.before(new Date())) {
					overdueWeekNo = week;
					break;
				}
			}

			if (overdueWeekNo != -1) {
				pendingReports.put(String.valueOf(group.getGroupId()), overdueWeekNo); // Convert long to String
			}
		}

		return pendingReports;
	}

	private Date calculateDeadline(int weekNo) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, weekNo);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return cal.getTime();
	}

	private int getCurrentWeekNumber() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.WEEK_OF_YEAR);
	}
}