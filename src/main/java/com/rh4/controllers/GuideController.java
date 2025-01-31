package com.rh4.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.rh4.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.rh4.entities.Admin;
import com.rh4.entities.GroupEntity;
import com.rh4.entities.Guide;
import com.rh4.entities.Intern;
import com.rh4.entities.MyUser;
import com.rh4.entities.WeeklyReport;
import com.rh4.repositories.GroupRepo;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/bisag/guide")
public class GuideController {

    @Autowired
    HttpSession session;
    @Autowired
    private GuideService guideService;
    @Autowired
    private InternService internService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private WeeklyReportService weeklyReportService;
    @Autowired
    private GroupRepo groupRepo;
    @Autowired
    private MyUserService myUserService;
    @Autowired
    private LogService logService;
    Intern internFromUploadFileMethod;
    int CurrentWeekNo;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public Guide getSignedInGuide() {
        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);
        return guide;
    }

    public String getUsername() {
        String username = (String) session.getAttribute("username");
        return username;
    }

    @GetMapping("/guide_dashboard")
    public ModelAndView guide_dashboard(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("guide/guide_dashboard");

        Guide guide = getSignedInGuide();
        String username = getUsername();

        long gPendingCount = groupService.countGPendingGroups();
        mv.addObject("gPendingCount", gPendingCount);

        session.setAttribute("id", guide.getGuideId());
        session.setAttribute("username", username);

        mv.addObject("username", username);

        mv.addObject("guide", guide);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Guide Accessed Dashboard",
                "Guide " + guide.getName() + " visited their dashboard.");

        return mv;
    }

    //Intern Groups
    @GetMapping("/intern_groups")
    public ModelAndView internGroups(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/intern_groups");


        Guide guide = getSignedInGuide();
        String id = String.valueOf(guide.getGuideId());

        logService.saveLog(id, "Viewed Intern Groups", "Guide: " + guide.getName() + " accessed the intern groups list.");

        List<GroupEntity> internGroups = guideService.getInternGroups(guide);
        List<Intern> interns = internService.getInterns();
        mv.addObject("internGroups", internGroups);
        mv.addObject("intern", interns);
        mv.addObject("guide", getSignedInGuide());

        return mv;
    }

    @GetMapping("/intern_groups/{id}")
    public ModelAndView internGroups(@PathVariable("id") String id) {
        ModelAndView mv = new ModelAndView("/guide/intern_groups_detail");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Intern Groups Detail", "Guide: " + guide.getName() + " accessed the intern groups detail.");

        List<GroupEntity> internGroups = guideService.getInternGroups(guide);
        mv.addObject("internGroups", internGroups);
        return mv;
    }

    @GetMapping("/intern/{id}")
    public ModelAndView internDetails(@PathVariable("id") String id) {
        ModelAndView mv = new ModelAndView();

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Intern Details", "Guide: " + guide.getName() + " accessed details for intern with ID: " + id);

        Optional<Intern> intern = internService.getIntern(id);
        mv.addObject("intern", intern);
        mv.setViewName("guide/intern_detail");
        return mv;
    }

    @GetMapping("/update_guide/{id}")
    public ModelAndView updateGuide(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/update_guide");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Updated Guide Details", "Guide: " + guide.getName() + " accessed the guide update page for guide with ID: " + id);

        Optional<Guide> guideDetails = guideService.getGuide(id);
        mv.addObject("guide", guideDetails.orElse(new Guide()));
        return mv;
    }

    @PostMapping("/update_guide/{id}")
    public String updateGuide(@ModelAttribute("guide") Guide guide, @PathVariable("id") long id) {
        Optional<Guide> existingGuide = guideService.getGuide(guide.getGuideId());

        if (existingGuide.isPresent()) {
            Guide updatedGuide = existingGuide.get();
            String guideName = updatedGuide.getName();

            updatedGuide.setName(guide.getName());
            updatedGuide.setLocation(guide.getLocation());
            updatedGuide.setFloor(guide.getFloor());
            updatedGuide.setLabNo(guide.getLabNo());
            updatedGuide.setContactNo(guide.getContactNo());
            updatedGuide.setEmailId(guide.getEmailId());

            // Save the updated guide entity
            guideService.updateGuide(updatedGuide, existingGuide);

            logService.saveLog(String.valueOf(updatedGuide.getGuideId()), "Guide Profile Updated",
                    "Guide " + guideName + " updated their profile.");
        }

        return "redirect:/logout";
    }

    @GetMapping("/guide_pending_def_approvals")
    public ModelAndView pendingFromGuide(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/guide_pending_def_approvals");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Pending Group Approvals", "Guide: " + guide.getName() + " accessed the pending group approvals page.");

        List<GroupEntity> groups = groupService.getGPendingGroups(guide);
        mv.addObject("groups", groups);
        mv.addObject("guide", guide);
        return mv;
    }

    @PostMapping("/guide_pending_def_approvals/ans")
    public String pendingFromGuide(@RequestParam("gpendingAns") String gpendingAns, @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = group.getGuide();
        String guideId = String.valueOf(guide.getGuideId());
        String guideName = guide.getName();

        if (gpendingAns.equals("approve")) {
            group.setProjectDefinitionStatus("gapproved");
            logService.saveLog(guideId, "Project Definition Approved",
                    "Guide " + guideName + " approved a project definition for Group ID: " + groupId);
        } else {
            group.setProjectDefinitionStatus("pending");
            logService.saveLog(guideId, "Project Definition Pending",
                    "Guide " + guideName + " set project definition to pending for Group ID: " + groupId);
        }

        groupRepo.save(group);
        return "redirect:/bisag/guide/guide_pending_def_approvals";
    }

    @GetMapping("/admin_pending_def_approvals")
    public ModelAndView pendingFromAdmin() {
        ModelAndView mv = new ModelAndView();

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Pending Groups for Admin Approval", "Guide: " + guide.getName() + " accessed the pending groups for admin approval.");

        List<GroupEntity> groups = groupService.getAPendingGroups();
        mv.addObject("groups", groups);
        return mv;
    }

    @GetMapping("/weekly_report")
    public ModelAndView weeklyReport() {
        ModelAndView mv = new ModelAndView("/guide/weekly_report");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Weekly Reports", "Guide: " + guide.getName() + " accessed the weekly reports page.");

        List<GroupEntity> groups = guideService.getInternGroups(guide);
        List<WeeklyReport> reports = weeklyReportService.getReportsByGuideId(guide.getGuideId());
        mv.addObject("groups", groups);
        mv.addObject("reports", reports);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @GetMapping("/guide_weekly_report_details/{groupId}/{weekNo}")
    public ModelAndView changeWeeklyReportSubmission(@PathVariable("groupId") String groupId, @PathVariable("weekNo") int weekNo) {
        ModelAndView mv = new ModelAndView("/guide/guide_weekly_report_details");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Weekly Report Details", "Guide: " + guide.getName() + " accessed weekly report details for group ID: " + groupId + " and week number: " + weekNo);

        GroupEntity group = groupService.getGroup(groupId);
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(getSignedInGuide().getEmailId());
        if (user.getRole().equals("GUIDE")) {
            String name = guide.getName();
            mv.addObject("replacedBy", name);
        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName() + intern.getLastName());
        }
        mv.addObject("report", report);
        mv.addObject("group", group);

        return mv;
    }

    @PostMapping("/guide_weekly_report_details/{groupid}/changed_report")
    public String chanegWeeklyReportSubmission(@PathVariable("groupid") String groupId,
                                               @RequestParam("weekNo") int weekNo,
                                               MultipartHttpServletRequest req)
            throws IllegalStateException, IOException, Exception {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = getSignedInGuide(); // Get the currently signed-in guide
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        CurrentWeekNo = weekNo;

        MyUser user = myUserService.getUserByUsername(guide.getEmailId());
        report.setReplacedBy(user);
        Date currentDate = new Date();

        // Check if the deadline is greater than or equal to the reportSubmittedDate
        if (report.getDeadline().compareTo(currentDate) >= 0) {
            report.setStatus("submitted");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Weekly Report Submitted",
                    "Guide " + guide.getName() + " submitted a weekly report for Group ID: " + groupId + ", Week No: " + weekNo);
        } else {
            report.setStatus("late submitted");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Weekly Report Late Submitted",
                    "Guide " + guide.getName() + " submitted a late weekly report for Group ID: " + groupId + ", Week No: " + weekNo);
        }

        weeklyReportService.addReport(report);
        return "redirect:/bisag/guide/weekly_report";
    }

    @GetMapping("/guide_pending_final_reports")
    public ModelAndView guidePendingFinalReports(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/guide/guide_pending_final_reports");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        logService.saveLog(guideId, "Viewed Pending Final Reports", "Guide: " + guide.getName() + " accessed the pending final reports list.");

        List<GroupEntity> groups = groupService.getGPendingFinalReports(guide);
        mv.addObject("groups", groups);
        mv.addObject("guide", getSignedInGuide());

        return mv;
    }

    @PostMapping("/guide_pending_final_reports/ans")
    public String guidePendingFinalReports(@RequestParam("gpendingAns") String gpendingAns,
                                           @RequestParam("groupId") String groupId) {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = getSignedInGuide();

        if (gpendingAns.equals("approve")) {
            group.setFinalReportStatus("approved");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approved",
                    "Guide " + guide.getName() + " approved the final report for Group ID: " + groupId);
        } else {
            group.setFinalReportStatus("pending");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approval Pending",
                    "Guide " + guide.getName() + " marked the final report as pending for Group ID: " + groupId);
        }

        groupRepo.save(group);
        return "redirect:/bisag/guide/guide_pending_final_reports";
    }
//		private String changeWeeklyReport(MultipartFile file, GroupEntity group) {
//
//			try {
//				File myDir = new File(weeklyReportSubmission + "/"+ group.getGroupId());
//				if(!myDir.exists())
//				{
//					myDir.mkdirs();
//				}
//				if(!file.isEmpty())
//				{
//					file.transferTo(Paths.get(myDir.getAbsolutePath(), group.getGroupId() + "_week_" + CurrentWeekNo + ".pdf"));
//					return group.getGroupId() + "_week_" + CurrentWeekNo + ".pdf";
//				}
//				else
//					return null;
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//				return "redirect:/";
//			}
//		}

    @GetMapping("/query_to_admin")
    public ModelAndView queryToAdmin() {
        ModelAndView mv = new ModelAndView("/guide/query_to_admin");
        List<Admin> admins = adminService.getAdmin();
        List<Guide> guides = guideService.getGuide();
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        mv.addObject("admins", admins);
        mv.addObject("guides", guides);
        mv.addObject("groups", groups);
        mv.addObject("guide", getSignedInGuide());
        return mv;
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Guide guide = getSignedInGuide(); // Get the currently signed-in guide

        guideService.changePassword(guide, newPassword);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Password Changed",
                "Guide " + guide.getName() + " changed their password.");

        return "redirect:/logout"; // Redirect to logout after password change
    }

}