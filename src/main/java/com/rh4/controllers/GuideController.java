package com.rh4.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.rh4.entities.*;
import com.rh4.repositories.*;
import com.rh4.services.*;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Autowired
    private GuideRepo guideRepo;
    @Autowired
            private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TaskAssignmentRepo taskAssignmentRepo;
    @Autowired
            private TaskAssignmentService taskAssignmentService;
    @Autowired
            private FieldService fieldService;
    @Autowired
            private InternRepo internRepo;
    @Autowired
            private LeaveApplicationService leaveApplicationService;
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
            private RecordService recordService;
    @Autowired
    private ThesisStorageService thesisStorageService;
    Intern internFromUploadFileMethod;
    int CurrentWeekNo;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private InternApplicationService internApplicationService;

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Value("${app.storage.base-dir}")
    private String baseDir;
    @Value("${app.storage.base-dir2}")
    private String baseDir2;
    public Guide getSignedInGuide() {
        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);
        return guide;
    }

    public String getUsername() {
        String username = (String) session.getAttribute("username");
        return username;
    }
    @ModelAttribute
    public void addPendingRequestCounts(Model model) {
        Guide guide = getSignedInGuide();  // Get the currently signed-in guide
        Long guideId = guide.getGuideId();
        long pendingInterviewApplications = internService.countPendingInterviewApplications();
        long pendingLeaveCount = leaveApplicationService.countPendingLeaveRequestsForGuide(guide.getGuideId());  // Count pending leave requests for the signed-in guide
        long countGPendingGroups = groupService.countPendingGroupsByGuideId(guideId);
        long gPendingFinalReportCount = groupService.countGPendingFinalReportsByGuide(guide);


        Map<String, Long> notificationCounts = new HashMap<>();
        notificationCounts.put("pendingInterviewApplications", pendingInterviewApplications);
        notificationCounts.put("pendingLeaveCount", pendingLeaveCount);
        notificationCounts.put("countGPendingGroups", countGPendingGroups);
        notificationCounts.put("gPendingFinalReportCount", gPendingFinalReportCount);
        model.addAttribute("notificationCounts", notificationCounts);
    }

    @GetMapping("/guide_dashboard")
    public ModelAndView guide_dashboard(HttpSession session, Model model) {
        Guide guide = getSignedInGuide();
        ModelAndView mv;

        // Check if it's the guide's first login
        Guide guideApplication = guideService.getGuideByUsername(guide.getEmailId());
        if (guideApplication != null && guideApplication.getFirstLogin() == 1) {
            mv = new ModelAndView("guide/change_passwordd");
            mv.addObject("forcePasswordChange", true);
        } else {
            mv = new ModelAndView("guide/guide_dashboard");
            long gPendingCount = groupService.countGPendingGroups();
            mv.addObject("gPendingCount", gPendingCount);
            mv.addObject("username", getUsername());
            mv.addObject("guide", guide);
            logService.saveLog(String.valueOf(guide.getGuideId()), "Guide Accessed Dashboard", "Guide " + guide.getName() + " visited their dashboard.");
        }

        session.setAttribute("id", guide.getGuideId());
        session.setAttribute("username", getUsername());

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

        logService.saveLog(guideId, "Viewed Pending Group Approvals",
                "Guide: " + guide.getName() + " accessed the pending group approvals page.");

        List<GroupEntity> groups = groupService.getGPendingGroups(guide);

        // Prepare intern name map for groupId
        Map<String, String> groupToInternNameMap = new HashMap<>();
        for (GroupEntity group : groups) {
            List<Intern> interns = internService.findByGroup(group);
            if (!interns.isEmpty()) {
                String names = interns.stream()
                        .map(Intern::getFirstName)
                        .collect(Collectors.joining(", "));
                groupToInternNameMap.put(group.getGroupId(), names); // Use String groupId
            }
        }

        mv.addObject("groups", groups);
        mv.addObject("guide", guide);
        mv.addObject("groupToInternNameMap", groupToInternNameMap); // Send map to view
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

//    @GetMapping("/weekly_report")
//    public ModelAndView weeklyReport() {
//        ModelAndView mv = new ModelAndView("/guide/weekly_report");
//
//        Guide guide = getSignedInGuide();
//        String guideId = String.valueOf(guide.getGuideId());
//
//        logService.saveLog(guideId, "Viewed Weekly Reports", "Guide: " + guide.getName() + " accessed the weekly reports page.");
//
//        List<GroupEntity> groups = guideService.getInternGroups(guide);
//        List<WeeklyReport> reports = weeklyReportService.getReportsByGuideId(guide.getGuideId());
//        mv.addObject("groups", groups);
//        mv.addObject("reports", reports);
//        mv.addObject("guide", getSignedInGuide());
//        return mv;
//    }
    @GetMapping("/weekly_report")
    public ModelAndView weeklyReport(Model model) {
        ModelAndView mv = new ModelAndView("/guide/weekly_report");

        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);
            List<GroupEntity> groups = guideService.getInternGroups(guide);
        List<WeeklyReport> reports = weeklyReportService.getReportsByGuideId(guide.getGuideId());
        Map<String, Long> unreadReportCounts = new HashMap<>();
        long totalUnreadReports = 0;

        for (GroupEntity group : groups) {
            long unreadCount = reports.stream()
                    .filter(report -> report.getGroup().getGroupId().equals(group.getGroupId()) && report.getGisRead() == 0)
                    .count();
            unreadReportCounts.put(group.getGroupId(), unreadCount);
            totalUnreadReports += unreadCount;
        }
        groups.sort(Comparator.comparing(GroupEntity::getGroupId));
//        model = countNotifications(model);
        mv.addObject("groups", groups);
        mv.addObject("guide", guide);
        mv.addObject("reports", reports);
        mv.addObject("unreadReportCounts", unreadReportCounts);
        mv.addObject("totalUnreadReports", totalUnreadReports);

        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Accessed Weekly Reports",
                    "Guide " + guide.getName() + " accessed the weekly reports page.");
        }

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
            mv.addObject("replacedBy", intern.getFirstName());
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
        Guide guide = getSignedInGuide();
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
        report.setGisRead(0);
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
        List<GroupEntity> groupss = groupService.getApprovedFinalReportsByGuide(guide);
        List<Intern> interns = internService.getAllInterns(); // or a filtered list if needed
        groups.sort(Comparator.comparing(GroupEntity::getGroupId).reversed());

        mv.addObject("interns", interns);
        mv.addObject("groups", groups);
        mv.addObject("groupss", groupss);
        mv.addObject("guide", getSignedInGuide());

        return mv;
    }

//    @PostMapping("/guide_pending_final_reports/ans")
//    public String guidePendingFinalReports(@RequestParam("gpendingAns") String gpendingAns,
//                                           @RequestParam("groupId") String groupId) {
//
//        GroupEntity group = groupService.getGroup(groupId);
//        Guide guide = getSignedInGuide();
//        group.setFinalReportStatusUpdatedAt(LocalDateTime.now());
//
//        if (gpendingAns.equals("approve")) {
//            group.setFinalReportStatus("gapproved");
//            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approved",
//                    "Guide " + guide.getName() + " approved the final report for Group ID: " + groupId);
//        } else {
//            group.setFinalReportStatus("pending");
//            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approval Pending",
//                    "Guide " + guide.getName() + " marked the final report as pending for Group ID: " + groupId);
//        }
//
//        groupRepo.save(group);
//        return "redirect:/bisag/guide/guide_pending_final_reports";
//    }
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    @PostMapping("/guide_pending_final_reports/ans")
    public String guidePendingFinalReports(@RequestParam("gpendingAns") String gpendingAns,
                                           @RequestParam("groupId") String groupId,
                                           @RequestParam(value = "rejectionFile", required = false) MultipartFile rejectionFile) {

        GroupEntity group = groupService.getGroup(groupId);
        Guide guide = getSignedInGuide();
        group.setFinalReportStatusUpdatedAt(LocalDateTime.now());

        if (gpendingAns.equals("approve")) {
            group.setFinalReportStatus("gapproved");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Approved",
                    "Guide " + guide.getName() + " approved the final report for Group ID: " + groupId);
        } else {
            group.setFinalReportStatus("changes");
            logService.saveLog(String.valueOf(guide.getGuideId()), "Final Report Rejected",
                    "Guide " + guide.getName() + " rejected the final report for Group ID: " + groupId);

            if (rejectionFile != null && !rejectionFile.isEmpty()) {
                try {
                    String baseDir = "E:/User/IMS/Springboot_Intern_Management_System/src/main/resources/static/files/Group Docs/";
                    String storageDir = baseDir + group.getGroupId() + "/";
                    File directory = new File(storageDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    String fileName = "Changes" + getFileExtension(rejectionFile.getOriginalFilename());
                    Path filePath = Paths.get(storageDir + fileName);
                    Files.write(filePath, rejectionFile.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
@GetMapping("/final_reports/{groupId}/{fileName}")
public ResponseEntity<Resource> openFinalReport(@PathVariable String groupId, @PathVariable String fileName) throws Exception {
    // Define the directory path based on the groupId
    String storageDir = baseDir2 + groupId + "/";
    File file = new File(storageDir + fileName);

    if (!file.exists()) {
        return ResponseEntity.notFound().build();
    }
    Resource resource = new FileSystemResource(file);
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
}
    @GetMapping("/getInternEmail/{groupId}")
    @ResponseBody
    public ResponseEntity<String> getInternEmail(@PathVariable Long groupId) {
        List<Intern> interns = internRepo.findByGroupId(groupId);

        if (!interns.isEmpty()) {
            return ResponseEntity.ok(interns.get(0).getEmail()); // Use the first intern’s email
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Intern not found");
        }
    }

    @GetMapping("/change_passwordd")
    public ModelAndView changePasswordPage(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return new ModelAndView("redirect:/bisag/guide/login");
        }

        ModelAndView mv = new ModelAndView("guide/change_passwordd");
        mv.addObject("username", username);
        mv.addObject("forcePasswordChange", true); // This will trigger the auto-show script in HTML
        return mv;
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Guide guide = getSignedInGuide(); // Get the currently signed-in guide

        guideService.changePassword(guide, newPassword);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Password Changed",
                "Guide " + guide.getName() + " changed their password.");

        return "redirect:/logout";
    }

    @PostMapping("/change_passwordd")
    public String changePasswordd(@RequestParam("newPassword") String newPassword) {
        Guide guide = getSignedInGuide(); // Get currently logged-in guide

        // Update password
        guideService.changePassword(guide, newPassword);

        // Fetch guide entity using guideId or email
        Guide guideEntity = guideService.getGuideByUsername(guide.getEmailId());
        if (guideEntity != null) {
            System.out.println("Before update, firstLogin: " + guideEntity.getFirstLogin());

            // Update firstLogin field
            guideEntity.setFirstLogin(0); // Mark first login as complete
            guideService.updateGuidee(guideEntity); // Save changes

            System.out.println("After update, firstLogin: " + guideEntity.getFirstLogin());
        } else {
            System.out.println("Guide entity not found for email: " + guide.getEmailId());
        }

        // Log the password change
        logService.saveLog(String.valueOf(guide.getGuideId()), "First Password Changed", "Password changed successfully for first time.");

        // Redirect to logout
        return "redirect:/logout";
    }

    // Fetch pending leaves for guide view
    @GetMapping("/pending_leaves")
    public String viewPendingLeaves(Model model) {
        Guide guide = getSignedInGuide();

        List<LeaveApplication> allPendingLeaves = leaveApplicationRepo.findByStatus("Pending");
        List<LeaveApplication> pendingLeavesForGuide = new ArrayList<>();
        Map<String, String> internNames = new HashMap<>();

        for (LeaveApplication leave : allPendingLeaves) {
            Intern intern = internService.getInternById(leave.getInternId());

            if (intern != null && intern.getGroup() != null &&
                    intern.getGroup().getGuide() != null &&
                    intern.getGroup().getGuide().getGuideId() == guide.getGuideId()) {

                pendingLeavesForGuide.add(leave);
                internNames.put(leave.getInternId(), intern.getFirstName());
            }
        }

        model.addAttribute("pendingLeaves", pendingLeavesForGuide);
        model.addAttribute("internNames", internNames);
        model.addAttribute("guide", guide);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Pending Leaves",
                "Guide " + guide.getName() + " viewed the list of pending leave applications.");

        return "guide/pending_leaves";
    }

    // Approve leave request
    @PostMapping("/approve_leave/{id}")
    public String approveLeave(@PathVariable Long id) {
        Guide guide = getSignedInGuide();

        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setGuideApproval(true);

            if (leave.isAdminApproval() && leave.isGuideApproval()) {
                leave.setStatus("Approved");
            }

            leaveApplicationRepo.save(leave);

            logService.saveLog(String.valueOf(guide.getGuideId()), "Approved Leave Request",
                    "Guide " + guide.getName() + " approved leave request for Intern ID: " + leave.getInternId());
        }
        return "redirect:/bisag/guide/pending_leaves";
    }

    // Reject leave request
    @PostMapping("/reject_leave/{id}")
    public String rejectLeave(@PathVariable Long id, @RequestParam("remarks") String remarks) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setStatus("Rejected");
            leave.setAdminApproval(false);
            leave.setGuideApproval(false);
            leave.setRemarks(remarks);
            leaveApplicationRepo.save(leave);

            Guide guide = getSignedInGuide();
            if (guide != null) {
                String guideId = String.valueOf(guide.getGuideId());
                logService.saveLog(guideId, "Rejected Leave Application",
                        "Guide " + guide.getName() + " rejected leave application for intern ID: " + leave.getInternId() + ". Remarks: " + remarks);
            }
        }
        return "redirect:/bisag/guide/pending_leaves";
    }

    // View Leave Details
    @GetMapping("/leave_details/{id}")
    public String viewLeaveDetails(@PathVariable Long id, Model model) {
        LeaveApplication leave = leaveApplicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Application Not Found"));
        model.addAttribute("leave", leave);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            String guideId = String.valueOf(guide.getGuideId());
            logService.saveLog(guideId, "Viewed Leave Details",
                    "Guide " + guide.getName() + " viewed details of leave application ID: " + id);
        }
        model.addAttribute("guide", guide);
        return "guide/leave_details";
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Messaging Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    @GetMapping("/query_to_admin")
    public ModelAndView queryToAdmin() {
        ModelAndView mv = new ModelAndView("/guide/query_to_admin");

        Guide guide = getSignedInGuide();
        String guideId = String.valueOf(guide.getGuideId());

        List<Admin> admins = adminService.getAdmin();
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);

        // Unread messages map
        Map<String, Long> unreadCounts = new HashMap<>();

        // Count unread messages from Admins
        for (Admin admin : admins) {
            String senderId = String.valueOf(admin.getAdminId());
            long count = messageService.countUnreadMessagesForReceiver(senderId, guideId);
            if (count > 0) {
                unreadCounts.put(senderId, count);
            }
        }

        // Count unread messages from Interns
        for (Intern intern : interns) {
            String senderId = intern.getInternId();
            long count = messageService.countUnreadMessagesForReceiver(senderId, guideId);
            if (count > 0) {
                unreadCounts.put(senderId, count);
            }
        }

        mv.addObject("admins", admins);
        mv.addObject("interns", interns);
        mv.addObject("guides", guides);
        mv.addObject("groups", groups);
        mv.addObject("guide", guide);
        mv.addObject("unreadCounts", unreadCounts);
        return mv;
    }
        // Guide sends a message
//    @PostMapping("/chat/send")
//    public ResponseEntity<Message> sendMessageAsGuide(
//            @RequestParam String senderId,
//            @RequestParam String receiverId,
//            @RequestParam String messageText) {
//
//        Optional<Guide> guide = guideService.findById(Long.valueOf(senderId));
//        if (guide.isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        Message message = messageService.sendMessage(String.valueOf(guide.get().getGuideId()), receiverId, messageText);
//
//        Guide senderGuide = guide.get();
//        logService.saveLog(senderId, "Sent Message",
//                "Guide " + senderGuide.getName() + " sent a message to Intern ID: " + receiverId + ". Message: " + messageText);
//
//        return ResponseEntity.ok(message);
//    }
//
//    @GetMapping("/chat/history")
//    public ResponseEntity<List<Message>> getChatHistoryAsGuide(
//            @RequestParam Long senderId,
//            @RequestParam String receiverId) {
//
//        Optional<Guide> guide = guideService.findById(senderId);
//        if (guide.isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        String guideId = String.valueOf(guide.get().getGuideId());
//
//        List<Message> messages = messageService.getChatHistory(guideId, receiverId);
//        messages.addAll(messageService.getChatHistory(receiverId, guideId));
//
//        messages.sort(Comparator.comparing(Message::getTimestamp));
//
//        Guide senderGuide = guide.get();
//        logService.saveLog(guideId, "Viewed Chat History",
//                "Guide " + senderGuide.getName() + " viewed chat history with ID: " + receiverId);
//
//        return ResponseEntity.ok(messages);
//    }
        @PostMapping("/chat/send")
        public ResponseEntity<Message> sendMessageAsAdmin(
                @RequestParam String receiverId,
                @RequestParam String messageText,
                @RequestParam(value = "file", required = false) MultipartFile file) {

            Guide guide = getSignedInGuide();
            String senderId = String.valueOf(guide.getGuideId());

            String filePath = null;
            String originalFileName = null;

            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = "/Users/pateldeep/Desktop/Coding/Springboot_Intern_Management_System-master-main/E:/User/IMS/Springboot_Intern_Management_System/src/main/resources/static/files/chat";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    originalFileName = file.getOriginalFilename();
                    String newFileName = System.currentTimeMillis() + "_" + originalFileName;
                    Path path = Paths.get(uploadDir, newFileName);
                    file.transferTo(path.toFile());

                    filePath = newFileName; // Store only filename, not full path
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            Message message = messageService.sendMessage(senderId, receiverId, messageText, filePath, originalFileName);

            logService.saveLog(senderId, "Sent a Message",
                    "Guide " + guide.getName() + " sent a message to User ID: " + receiverId);

            return ResponseEntity.ok(message);
        }
    @GetMapping("/chat/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileName) throws IOException {
        Path file = Paths.get("/Users/pateldeep/Desktop/Coding/Springboot_Intern_Management_System-master-main/E:/User/IMS/Springboot_Intern_Management_System/src/main/resources/static/files/chat", fileName);
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName().toString() + "\"")
                .body(resource);
    }

    private final Path fileStorageLocation = Paths.get("/Users/pateldeep/Desktop/Coding/Springboot_Intern_Management_System-master-main/E:/User/IMS/Springboot_Intern_Management_System/src/main/resources/static/files/chat")
            .toAbsolutePath().normalize();
    @GetMapping("/chat/view")
    public ResponseEntity<Resource> viewFile(@RequestParam String filePath) {
        try {
            Path file = fileStorageLocation.resolve(filePath).normalize();
            if (!Files.exists(file) || !file.toString().startsWith(fileStorageLocation.toString())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // Admin fetches chat history (both sent and received messages)
    @GetMapping("/chat/history")
    public ResponseEntity<List<Message>> getChatHistoryAsGuide(@RequestParam String receiverId) {
        Guide guide = getSignedInGuide();
        String senderId = String.valueOf(guide.getGuideId());

        List<Message> messages = messageService.getChatHistoryForBothUsers(senderId, receiverId);

        for (Message message : messages) {
            if (message.getReceiverId().equals(senderId) && !message.isRead()) {
                message.setRead(true);
                message.setReadTimestamp(LocalDateTime.now());
                messageRepo.save(message);
            }
        }
        logService.saveLog(senderId, "Viewed Chat History",
                "Guide " + guide.getName() + " viewed chat history with User ID: " + receiverId);

        return ResponseEntity.ok(messages);
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Task Assignment Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    @GetMapping("/tasks_assignments")
    public String viewTaskAssignmentsPage(Model model) {
        Guide guide = getSignedInGuide();
        List<TaskAssignment> tasks = taskAssignmentService.getAllTasks();
        List<Intern> interns = internService.getInternsByGuideId(guide.getGuideId());
        Map<String, String> internIdToNameMap = interns.stream()
                .collect(Collectors.toMap(Intern::getInternId, Intern::getFirstName));

        List<TaskAssignment> filteredTasks = tasks.stream()
                .filter(task -> internIdToNameMap.containsKey(task.getIntern()))
                .collect(Collectors.toList());
        model.addAttribute("interns", interns);
        model.addAttribute("tasks", filteredTasks);
        model.addAttribute("internIdToNameMap", internIdToNameMap);
        if (guide != null) {
            String guideId = String.valueOf(guide.getGuideId());
            logService.saveLog(guideId, "Viewed Task Assignments",
                    "Guide " + guide.getName() + " viewed the task assignments page.");
        }
        model.addAttribute("guide", guide);
        return "guide/task_assignments";
    }
    // Assign a New Task
    @PostMapping("/tasks/assign")
    public String assignTask(
            @RequestParam("intern") String intern,
//            @RequestParam("assignedById") String assignedById,
            @RequestParam("assignedByRole") String assignedByRole,
            @RequestParam("taskDescription") String taskDescription,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr,
            RedirectAttributes redirectAttributes) {
        Guide guide = getSignedInGuide();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            Date today = new Date();
            if (startDate.before(dateFormat.parse(dateFormat.format(today)))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Start date cannot be in the past!");
                return "redirect:/bisag/guide/tasks_assignments";
            }

            if (endDate.before(startDate)) {
                redirectAttributes.addFlashAttribute("errorMessage", "End date cannot be before start date!");
                return "redirect:/bisag/guide/tasks_assignments";
            }
            Optional<Intern> optionalIntern = internService.getIntern(intern);
            if (optionalIntern.isPresent()) {
                TaskAssignment task = new TaskAssignment();
                task.setIntern(intern);
//                task.setAssignedById(assignedById);
                task.setAssignedByRole(guide.getName());
                task.setTaskDescription(taskDescription);
                task.setStartDate(startDate);
                task.setEndDate(endDate);
                task.setStatus("Pending");
                task.setApproved(false);
                taskAssignmentService.saveTask(task);
                logService.saveLog(String.valueOf(guide.getGuideId()), "Assigned a Task",
                        "Guide " + guide.getName() + " assigned a task to Intern ID: " + intern);

                redirectAttributes.addFlashAttribute("successMessage", "Task assigned successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid Intern ID! Please select a valid intern.");
            }
        } catch (ParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format! Please enter a valid date.");
            return "redirect:/bisag/guide/tasks_assignments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/bisag/guide/tasks_assignments";
        }
        return "redirect:/bisag/guide/tasks_assignments";
    }

    // View Task Details ID-wise
    @GetMapping("/task_details/{id}")
    public String viewTaskDetails(@PathVariable Long id, Model model) {
        TaskAssignment tasks = taskAssignmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task Assignment Not Found"));
        model.addAttribute("tasks", tasks);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            String guideId = String.valueOf(guide.getGuideId());
            logService.saveLog(guideId, "Viewed Task Details",
                    "Guide " + guide.getName() + " viewed details of Task Assignment ID: " + id);
        }
        model.addAttribute("guide", guide);
        return "guide/task_details";
    }

    // Get Tasks Assigned by Admin/Guide
    @GetMapping("/tasks/assignedBy/{assignedById}")
    public ResponseEntity<List<TaskAssignment>> getTasksAssignedBy(@PathVariable("assignedById") String assignedById) {
        List<TaskAssignment> tasks = taskAssignmentService.getTasksAssignedBy(assignedById);

        logService.saveLog(assignedById, "Viewed Assigned Tasks",
                "User with ID " + assignedById + " viewed tasks assigned by them.");

        return ResponseEntity.ok(tasks);
    }

    // Approve Task Completion
    @PostMapping("/tasks/approve/{taskId}")
    public ResponseEntity<String> approveTask(@PathVariable("taskId") Long taskId) {
        Optional<TaskAssignment> optionalTask = taskAssignmentService.getTaskById(taskId);

        if (optionalTask.isPresent()) {
            TaskAssignment task = optionalTask.get();
            task.setApproved(true);
            task.setStatus("Completed");

            taskAssignmentService.saveTask(task);

            logService.saveLog(task.getAssignedById(), "Approved Task",
                    "User with ID " + task.getAssignedById() + " approved Task ID: " + taskId);

            return ResponseEntity.ok("Task approved successfully.");
        }
        return ResponseEntity.badRequest().body("Task not found.");
    }

    @GetMapping("/tasks/proof/{taskId}")
    public ResponseEntity<Resource> getProofAttachment(@PathVariable Long taskId) {
        Optional<TaskAssignment> taskOpt = taskAssignmentRepo.findById(taskId);

        if (taskOpt.isEmpty() || taskOpt.get().getProofAttachment() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {

            String fileName = taskOpt.get().getProofAttachment();
            Path filePath = Paths.get("main/resources/static/files/Task_Proofs/", fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            logService.saveLog(taskOpt.get().getAssignedById(), "Viewed Task Proof",
                    "Guide with ID " + taskOpt.get().getAssignedById() + " viewed proof for Task ID: " + taskId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update-task/{id}")
    public ResponseEntity<Map<String, Object>> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> taskData) {
        try {
            String newStatus = taskData.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Status cannot be empty"));
            }

            Optional<TaskAssignment> optionalTask = taskAssignmentService.getTaskById(id);
            if (optionalTask.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Task not found"));
            }

            TaskAssignment task = optionalTask.get();
            String oldStatus = task.getStatus();
            task.setStatus(newStatus); // Updating only the status
            taskAssignmentService.saveTask(task);

            Guide guide = getSignedInGuide();
            if (guide != null) {
                String guideId = String.valueOf(guide.getGuideId());
                logService.saveLog(guideId, "Updated Task Status",
                        "Guide " + guide.getName() + " updated Task ID: " + id + " from '" + oldStatus + "' to '" + newStatus + "'.");
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
        }
    }

    //Shortlisting Interns
    @PostMapping("/intern_application/approved_intern/ans")
    public String approvedInterns(@RequestParam String message, @RequestParam long id, @RequestParam String finalStatus) {
        System.out.println("id: " + id + ", finalStatus: " + finalStatus);

        Optional<InternApplication> intern = internService.getInternApplication(id);
        intern.get().setFinalStatus(finalStatus);
        internService.addInternApplication(intern.get());

        if (finalStatus.equals("failed")) {

        } else {
            String finalMessage = message + "\n" + "Username: " + intern.get().getFirstName() + "\n Password: " + intern.get().getFirstName() + "_" + intern.get().getId();
        }

        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Updated Final Status",
                    "Guide with ID: " + guide.getGuideId() + " updated final status of intern with ID: " + id + " to " + finalStatus);
        }

        return "redirect:/bisag/guide/intern_application/approved_interns";
    }

    @GetMapping("/approved_interns")
    public ModelAndView approvedInterns(Model model) {
        ModelAndView mv = new ModelAndView();

        // Get the logged-in guide's username from session
        String username = (String) session.getAttribute("username");
        Guide guide = guideService.getGuideByUsername(username);

        if (guide != null) {
            long guideId = guide.getGuideId();

            List<InternApplication> intern = internApplicationService.getApprovedInternsByGuideId(guideId);

            mv.addObject("intern", intern);
            mv.addObject("guide", guide);
            session.setAttribute("id", guideId);

            logService.saveLog(String.valueOf(guideId),
                    "View Shortlisted Intern Applications",
                    "Guide " + guide.getName() + " accessed the shortlisted intern applications page.");
        } else {
            System.out.println("Error: Guide not found for logging!");
            mv.addObject("interns", List.of());
        }

        mv.setViewName("guide/approved_interns");
        return mv;
    }

    @GetMapping("/intern_application/{id}")
    public ModelAndView internApplication(@PathVariable("id") long id, Model model) {
        System.out.println("id" + id);
        ModelAndView mv = new ModelAndView();

        Optional<InternApplication> intern = internService.getInternApplication(id);
        List<Guide> guides = guideService.getGuide();

        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "View Intern Application Details",
                    "Guide " + guide.getName() + " viewed the details of Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in guide not found for logging!");
        }

        mv.addObject("intern", intern);
        model.addAttribute("guides", guides);
        mv.addObject("guide", guide);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);

        mv.setViewName("guide/intern_application_detail");
        return mv;
    }
    @PostMapping("/intern_application/ans")
    public String internApplicationSubmission(@RequestParam String message, @RequestParam long id,
                                              @RequestParam String status, @RequestParam String finalStatus) {
        System.out.println("id: " + id + ", status: " + status);

        Optional<InternApplication> intern = internService.getInternApplication(id);

        if (intern.isPresent()) {
            intern.get().setStatus(status);
            intern.get().setFinalStatus(finalStatus);
            internService.addInternApplication(intern.get());

            logService.saveLog(String.valueOf(id), "Updated application status for intern", "Status Change");

            if (status.equals("rejected")) {
            } else {
            }
        }
        return "redirect:/bisag/guide/approved_interns";
    }

    @GetMapping("/approveDefinition")
    public String showApproveDefinitionForm(Model model) {
        List<GroupEntity> pendingGroups = groupRepo.findByProjectDefinitionStatus("gpending");

        if (pendingGroups.isEmpty()) {
            model.addAttribute("error", "No pending project definitions found!");
        } else {
            model.addAttribute("groups", pendingGroups);
        }

        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Pending Project Definitions", "Guide " + guide.getName() + " accessed the pending project definitions page.");
        }

        return "guide/guide_pending_def_approvals";
    }

    @PostMapping("/approveDefinition")
    public ResponseEntity<String> approveProjectDefinition(@RequestParam String groupId,
                                                           @RequestParam String status) {
        List<Guide> guides = guideRepo.findByGroupId(groupId);
        if (!guides.isEmpty()) {
            for (Guide guide : guides) {
                guide.setDefinitionStatus(status);
                guideRepo.save(guide);
            }

            GroupEntity group = groupRepo.getByGroupId(groupId);
            if (group != null) {
                String oldStatus = group.getProjectDefinitionStatus();
                group.setProjectDefinitionStatus(status.equalsIgnoreCase("Approved") ? "Approved" : "Rejected");
                groupRepo.save(group);

                Guide guide = getSignedInGuide();
                if (guide != null) {
                    logService.saveLog(String.valueOf(guide.getGuideId()), "Updated Project Definition Status",
                            "Guide " + guide.getName() + " updated Group ID: " + groupId + " from '" + oldStatus + "' to '" + group.getProjectDefinitionStatus() + "'.");
                }
            }
            return ResponseEntity.ok("Project Definition " + status);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Guide not found.");
    }

    @GetMapping("/create_project_def")
    public String showAssignProjectDefinitionForm(Model model) {
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        model.addAttribute("groups", groups);
        model.addAttribute("guide", guide);

        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Assign Project Definition Form",
                    "Guide " + guide.getName() + " accessed the assign project definition form.");
        }

        return "guide/create_project_def";
    }

    @PostMapping("/create_project_def")
    public String submitProjectDefinition(@RequestParam String groupId,
                                          @RequestParam String description,
                                          @RequestParam String projectDefinition,
                                          Model model) {
        GroupEntity group = groupRepo.getByGroupId(groupId);

        if (group == null) {
            model.addAttribute("success", "Group not found.");
            return "guide/create_project_def";
        }
        String oldStatus = group.getProjectDefinitionStatus();
        group.setProjectDefinition(projectDefinition);
        group.setDescription(description);
        group.setProjectDefinitionStatus("pending");
        groupRepo.save(group);

        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Submitted Project Definition",
                    "Guide " + guide.getName() + " updated Group ID: " + groupId + " from '" + oldStatus + "' to 'pending'.");
        }

        return "guide/create_project_def";
    }

    @GetMapping("/view_weekly_reports/{weekNo}")
    public ModelAndView chanegWeeklyReportSubmission(@PathVariable("weekNo") int weekNo) {
        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Weekly Report",
                    "Guide " + guide.getName() + " viewed Weekly Report for Week No: " + weekNo);
        }
        ModelAndView mv = new ModelAndView("intern/change_weekly_report");
        Intern inetrn = getSignedInIntern();
        GroupEntity group = inetrn.getGroup();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(report.getReplacedBy().getUsername());
        if (user.getRole().equals("GUIDE")) {
            String status = "Your Current Weekly report is required some modifications given by guide. Please check it out.";
            mv.addObject("status", status);
            mv.addObject("replacedBy", guide.getName());
        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName());
            mv.addObject("status",
                    "Your current weekly report is accepted and if any changes are required then you will be notified.");
        }
        mv.addObject("report", report);
        mv.addObject("group", group);
        return mv;
    }

    public Intern getSignedInIntern() {
        String username = (String) session.getAttribute("username");
        Intern intern = internService.getInternByUsername(username);
        if (intern != null && intern.getIsActive()) {
            return intern;
        } else {
            return null;
        }
    }

//    @GetMapping("/viewPdf/{internId}/{weekNo}")
//    public ResponseEntity<byte[]> viewPdf(@PathVariable String internId, @PathVariable int weekNo) {
//        Guide guide = getSignedInGuide();
//        if (guide != null) {
//            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed PDF",
//                    "Guide " + guide.getName() + " viewed Weekly Report PDF for Intern ID: " + internId + ", Week No: " + weekNo);
//        }
//        WeeklyReport report = weeklyReportService.getReportByInternIdAndWeekNo(internId, weekNo);
//        byte[] pdfContent = report.getSubmittedPdf();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
//    }

    @GetMapping("/viewPdf/{internId}/{weekNo}")
    public ResponseEntity<byte[]> viewPdf(@PathVariable String internId, @PathVariable int weekNo) {
        String groupId = internRepo.findGroupIdByInternId(internId);

        if (groupId == null) {
            return ResponseEntity.notFound().build();
        }

        WeeklyReport report = weeklyReportService.getReportByInternIdAndWeekNo(internId, weekNo);
        Guide guide = getSignedInGuide();

        if (report != null) {
            report.setGisRead(1);
            weeklyReportService.addReport(report);
        }

        logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed PDF",
                "Guide " + guide.getName() + " viewed Weekly Report PDF for Intern ID: " + internId + ", Week No: " + weekNo);

        String baseDir = baseDir2;
        String filePath = baseDir + groupId + "/Weekly Reports/" + groupId + "_Week" + weekNo + ".pdf";

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] pdfContent = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setCacheControl(CacheControl.noCache().mustRevalidate());

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/viewProjectDefinition/{groupId}")
    public ResponseEntity<byte[]> viewProjectDefinition(@PathVariable String groupId) {
        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Project Definition",
                    "Guide " + guide.getName() + " viewed Project Definition for Group ID: " + groupId);
        }
        GroupEntity group = groupService.getGroupByGroupId(groupId);

        if (group == null || group.getProjectDefinitionDocument() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        byte[] pdfContent = group.getProjectDefinitionDocument();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
    @GetMapping("/getInternName/{internId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> fetchInternDetailsById(@PathVariable String internId) {
        Optional<Intern> internOptional = internService.findById(internId);
        if (internOptional.isPresent()) {
            Intern intern = internOptional.get();
            Map<String, String> response = new HashMap<>();
            String internName = (intern.getFirstName() != null) ? intern.getFirstName() : "N/A";
            response.put("internName", internName);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PostMapping("/markGroupReportsRead")
    public String markGroupReportsAsReadByGuide(@RequestParam("groupId") String groupId) {
        weeklyReportService.markReportsAsReadByGuide(groupId);
        Guide guide = getSignedInGuide();
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed All Weekly Reports",
                    "Guide " + guide.getName() + " viewed all weekly reports for Group ID: " + groupId);
        }
        return "redirect:/bisag/guide/weekly_report"; // or your actual guide weekly report page
    }
    @GetMapping("/group/{groupId}/members")
    public String viewGroupMembers(@PathVariable("groupId") String groupId, Model model) {
        List<Intern> groupMembers = internService.getInternsByGroupId(groupId);
        model.addAttribute("groupId", groupId);
        model.addAttribute("groupMembers", groupMembers);
        Guide guide = getSignedInGuide();
        model.addAttribute("guide", guide);
        if (guide != null) {
            logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Group Members",
                    "Guide " + guide.getName() + " viewed details for Group ID: " + groupId);
        }
        return "guide/group_members";
    }
//    @GetMapping("/interns")
//    public String viewAssignedInterns(Model model) {
//        Guide signedInGuide = getSignedInGuide(); // This should return the Guide object for the logged-in guide
//        if (signedInGuide == null) {
//            return "redirect:/bisag/login"; // Handle not-logged-in scenario appropriately
//        }
//
//        Long guideId = signedInGuide.getGuideId();
//        List<Intern> interns = internService.getInternsByGuideId(guideId);
//        model.addAttribute("interns", interns);
//        return "guide/intern_list"; // Thymeleaf template name for displaying the intern list
//    }
    @GetMapping("/interns")
    public ModelAndView newInternsForGuide(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) Boolean all
    ) {
        ModelAndView mv = new ModelAndView();

        Guide guide = getSignedInGuide();
        if (guide == null) {
            mv.setViewName("redirect:/bisag/login");
            return mv;
        }

        List<Intern> internList;
        if (Boolean.TRUE.equals(all)) {
            internList = internService.getInternsByGuideId(guide.getGuideId());
            mv.addObject("showAll", true);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Intern> internPage = internService.getInternsByGuideIdPaginated(guide.getGuideId(), pageable);
            internList = internPage.getContent();
            mv.addObject("currentPage", page);
            mv.addObject("totalPages", internPage.getTotalPages());
            mv.addObject("pageSize", size);
            mv.addObject("showAll", false);
        }

        mv.addObject("intern", internList);
        mv.addObject("guide", guide.getName());

        List<RRecord> records = recordService.getAllRecords();
        model.addAttribute("records", records);
        List<College> college = fieldService.getColleges();
        List<Domain> domain = fieldService.getDomains();
        List<Degree> degree = fieldService.getDegrees();
        List<GroupEntity> groupEntities = groupService.getGroups();
        List<String> projectDefinitions = internService.getDistinctProjectDefinitions();
        List<String> genders = internService.getDistinctGenders();

        mv.addObject("colleges", college);
        mv.addObject("domains", domain);
        mv.addObject("degrees", degree);
        mv.addObject("groups", groupEntities);
        mv.addObject("project_definition_name", projectDefinitions);
        mv.addObject("genders", genders);
        mv.addObject("guide", guide);
        // Final Report Statuses
        List<String> internIds = internList.stream()
                .map(Intern::getInternId)
                .collect(Collectors.toList());

        Map<String, String> finalReportStatuses = recordService.findFinalReportsForInternIds(internIds);
        model.addAttribute("finalReportStatuses", finalReportStatuses);

//        Map<String, String> reportTimestamps = new HashMap<>();
//        for (Intern i : internList) {
//            RRecord record = recordService.findLatestRecordByInternId(i.getInternId());
//            if (record != null && record.getSubmissionTimestamp() != null) {
//                reportTimestamps.put(i.getInternId(),
//                        record.getSubmissionTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
//            } else {
//                reportTimestamps.put(i.getInternId(), "N/A");
//            }
//        }
//        model.addAttribute("reportTimestamps", reportTimestamps);

        // Save Log
        logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Guide New Interns",
                "Guide " + guide.getName() + " accessed their assigned interns page.");

        mv.setViewName("guide/intern_list");
        return mv;
    }

    @GetMapping("/view-thesis/{id}")
    public ResponseEntity<Resource> viewThesisAsGuide(@PathVariable Long id, Principal principal) throws IOException {
        Optional<ThesisStorage> optionalThesisStorage = thesisStorageService.getThesisById(id);

        if (optionalThesisStorage.isEmpty()) {
            logService.saveLog("N/A", "Attempted to View Thesis", "Thesis with ID " + id + " not found.");
            return ResponseEntity.notFound().build();
        }

        ThesisStorage thesisStorage = optionalThesisStorage.get();
        String emailOrUsername = principal.getName();

        Guide guide = guideService.getGuideByUsername(emailOrUsername);
        if (guide == null) {
            logService.saveLog("N/A", "Unauthorized Thesis Access Attempt",
                    "User " + emailOrUsername + " tried to access Thesis ID: " + id + " but is not a recognized guide.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String internId = thesisStorage.getAllowedInternId();
        if (!internService.isInternAssignedToGuide(internId, guide.getGuideId())) {
            logService.saveLog("GuideID: " + guide.getGuideId(), "Unauthorized Thesis Access Attempt",
                    "Guide " + guide.getName() + " attempted to access Thesis ID: " + id + " not assigned to them.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (thesisStorage.getFilePath() == null || thesisStorage.getFilePath().isEmpty()) {
            logService.saveLog("GuideID: " + guide.getGuideId(), "Thesis File Missing",
                    "Guide " + guide.getName() + " tried to view Thesis ID: " + id + " but the file path is missing.");
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(thesisStorage.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            logService.saveLog("GuideID: " + guide.getGuideId(), "Failed to View Thesis",
                    "Guide " + guide.getName() + " tried to view Thesis ID: " + id + " but file is not readable.");
            return ResponseEntity.notFound().build();
        }

        logService.saveLog("GuideID: " + guide.getGuideId(), "Viewed Thesis",
                "Guide " + guide.getName() + " successfully viewed Thesis ID: " + id + ".");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }

    @Value("${app.storage.base-dir4}")
    private String appStorageBaseDir4;

    @PostMapping("/upload_guide_sign/{id}")
    public String uploadGuideSignature(@RequestParam("signatureFile") MultipartFile signatureFile,
                                       @PathVariable("id") long id,
                                       RedirectAttributes redirectAttributes) {
        Optional<Guide> optionalGuide = guideService.getGuide(id);

        if (optionalGuide.isPresent()) {
            Guide guide = optionalGuide.get();

            if (signatureFile != null && !signatureFile.isEmpty()) {
                try {
                    String originalFilename = signatureFile.getOriginalFilename();
                    String extension = "";

                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String emailFolder = guide.getEmailId().replaceAll("[^a-zA-Z0-9@.]", "_");
                    String fullDirPath = appStorageBaseDir4 + File.separator + emailFolder;
                    Path dirPath = Paths.get(fullDirPath);
                    Files.createDirectories(dirPath);
                    String fileName = guide.getName().replaceAll("\\s+", "_") + "_Sign" + extension;
                    Path filePath = dirPath.resolve(fileName);
                    Files.write(filePath, signatureFile.getBytes());
                    String relativePath = "/files/Admin Docs/" + emailFolder + "/" + fileName;
                    guide.setDigitalSignaturePath(relativePath);
                    guideService.updateGuide(guide, Optional.of(guide));

                    redirectAttributes.addFlashAttribute("success", "Signature uploaded successfully!");
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Error uploading signature!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "No file selected!");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Guide not found!");
        }

        return "redirect:/bisag/guide/guide_dashboard";
    }

    @GetMapping("/guide_sign/{guideId}")
    public ResponseEntity<byte[]> getGuideSignature(@PathVariable Long guideId) {
        Optional<Guide> optionalGuide = guideService.getGuide(guideId);

        if (optionalGuide.isPresent()) {
            Guide guide = optionalGuide.get();
            String relativePath = guide.getDigitalSignaturePath();

            if (relativePath != null && !relativePath.isEmpty()) {
                try {
                    String absolutePath = appStorageBaseDir4 + File.separator +
                            guide.getEmailId().replaceAll("[^a-zA-Z0-9@.]", "_") + File.separator +
                            new File(relativePath).getName(); // extract filename

                    Path imagePath = Paths.get(absolutePath);
                    byte[] imageBytes = Files.readAllBytes(imagePath);
                    String contentType = Files.probeContentType(imagePath);
                    if (contentType == null) {
                        contentType = MediaType.IMAGE_PNG_VALUE;
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));

                    return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/document_status")
    public String showDocumentStatus(Model model, Principal principal) {
        String username = principal.getName();
        Guide guide = guideService.getGuideByUsername(username);

        if (guide != null) {
            List<Intern> interns = internService.getInternsByGuideId(guide.getGuideId());
            interns.sort(Comparator.comparing(Intern::getInternId).reversed());
            model.addAttribute("interns", interns);

            logService.saveLog(
                    String.valueOf(guide.getGuideId()),
                    "Accessed Intern Document List",
                    "Guide " + guide.getName() + " viewed the list of document statuses for their assigned interns."
            );
            model.addAttribute("guide", guide);
        } else {
            model.addAttribute("interns", List.of()); // empty list
        }

        return "guide/document_status_list";
    }

    @GetMapping("/intern_icard/{internId}")
    public ModelAndView viewInternICard(@PathVariable String internId,
                                        RedirectAttributes redirectAttributes,
                                        Principal principal) {
        Intern intern = internService.getInternById(internId);
        if (intern == null) {
            redirectAttributes.addFlashAttribute("error", "Intern not found.");
            return new ModelAndView("redirect:/bisag/guide/document_status");
        }

        if (!intern.isIcardApproved()) {
            redirectAttributes.addFlashAttribute("error", "I-Card has not been approved by the intern yet.");
            return new ModelAndView("redirect:/bisag/guide/document_status");
        }
        String username = principal.getName();
        Guide guide = guideService.getGuideByUsername(username);
        if (guide != null) {
            logService.saveLog(
                    String.valueOf(guide.getGuideId()),
                    "Viewed Intern I-Card",
                    "Guide " + guide.getName() + " viewed the I-Card of intern with ID " + internId + "."
            );
        }

        ModelAndView mv = new ModelAndView("guide/intern_icard");
        mv.addObject("intern", intern);
        return mv;
    }

    @GetMapping("/photo/{internId}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String internId) {
        Intern intern = internService.getInternById(internId);
        byte[] imageBytes = intern.getPassportSizeImage();
        if (imageBytes == null || imageBytes.length == 0) {
            InputStream in = getClass().getResourceAsStream("/static/images/default-avatar.jpg");
            try {
                imageBytes = IOUtils.toByteArray(in);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/digital_sign/{internId}")
    public ResponseEntity<byte[]> getDigitalSign(@PathVariable String internId) throws IOException {
        Intern intern = internService.getInternById(internId);
        if (intern == null || intern.getSign() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] imageData = intern.getSign();
        try (FileOutputStream fos = new FileOutputStream("debug_signature.jpg")) {
            fos.write(imageData);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    @PostMapping("/approve_icard/{internId}")
    public String approveIcard(@PathVariable String internId, RedirectAttributes redirectAttributes) {
        Optional<Intern> optionalIntern = internService.getInternByInternId(internId);
        if (optionalIntern.isPresent()) {
            Intern intern = optionalIntern.get();
            intern.setGuideApprovedIcard(true);
            internService.saveIntern(intern);
            redirectAttributes.addFlashAttribute("success", "I-Card approved successfully!!!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Intern not found. Unable to approve i-Card.");
        }
        return "redirect:/bisag/guide/document_status";
    }
}