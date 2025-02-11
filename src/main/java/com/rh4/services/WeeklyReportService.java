package com.rh4.services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
}