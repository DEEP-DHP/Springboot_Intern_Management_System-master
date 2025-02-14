package com.rh4.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.rh4.entities.*;
import com.rh4.repositories.LeaveApplicationRepo;
import com.rh4.repositories.TaskAssignmentRepo;
import com.rh4.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

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
    @Autowired
            private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TaskAssignmentRepo taskAssignmentRepo;
    @Autowired
            private TaskAssignmentService taskAssignmentService;
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
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        Guide guide = getSignedInGuide();
        List<GroupEntity> groups = guideService.getInternGroups(guide);
        mv.addObject("admins", admins);
        mv.addObject("interns", interns);
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

        return "redirect:/logout";
    }

    // Fetch pending leaves for guide view
    @GetMapping("/pending_leaves")
    public String viewPendingLeaves(Model model) {
        Guide guide = getSignedInGuide(); // Get the currently signed-in guide

        List<LeaveApplication> pendingLeaves = leaveApplicationRepo.findByStatus("Pending");
        model.addAttribute("pendingLeaves", pendingLeaves);

        logService.saveLog(String.valueOf(guide.getGuideId()), "Viewed Pending Leaves",
                "Guide " + guide.getName() + " viewed the list of pending leave applications.");

        return "guide/pending_leaves"; // Thymeleaf template
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
    public String rejectLeave(@PathVariable Long id) {
        Guide guide = getSignedInGuide();

        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setStatus("Rejected");
            leave.setAdminApproval(false);
            leave.setGuideApproval(false);
            leaveApplicationRepo.save(leave);

            logService.saveLog(String.valueOf(guide.getGuideId()), "Rejected Leave Request",
                    "Guide " + guide.getName() + " rejected leave request for Intern ID: " + leave.getInternId());
        }
        return "redirect:/bisag/guide/pending_leaves";
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Messaging Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
        // Guide sends a message
        @PostMapping("/chat/send")
        public ResponseEntity<Message> sendMessageAsGuide(
                @RequestParam String senderId,
                @RequestParam String receiverId,
                @RequestParam String messageText) {

            Optional<Guide> guide = guideService.findById(Long.valueOf(senderId));
            if (guide.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Message message = messageService.sendMessage(String.valueOf(guide.get().getGuideId()), receiverId, messageText);
            return ResponseEntity.ok(message);
        }

    @GetMapping("/chat/history")
    public ResponseEntity<List<Message>> getChatHistoryAsGuide(
            @RequestParam Long senderId,
            @RequestParam String receiverId) {

        // Ensure senderId is correctly mapped to an actual Guide ID
        Optional<Guide> guide = guideService.findById(senderId);
        if (guide.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String guideId = String.valueOf(guide.get().getGuideId());

        // Fetch both sent and received messages
        List<Message> messages = messageService.getChatHistory(guideId, receiverId);
        messages.addAll(messageService.getChatHistory(receiverId, guideId)); // Fetch messages in reverse order

        // Sort messages by timestamp to maintain chronological order
        messages.sort(Comparator.comparing(Message::getTimestamp));

        return ResponseEntity.ok(messages);
    }
    // Load chat page for Guide
//    @GetMapping("/chat")
//    public String loadGuideMessengerPage(Model model) {
//        Guide guide = getSignedInGuide();
//
//        if (guide == null) {
//            System.out.println("Guide not found or session expired.");
//            return "redirect:/login"; // Redirect to login if guide is null
//        }
//
//        model.addAttribute("loggedInGuide", guide);
//
//        // Load receivers dynamically
//        List<Admin> admins = adminService.getAdmin(); // Fetch all admins
//        List<Intern> interns = internService.getAllInterns(); // Fetch all interns
//
//        model.addAttribute("admins", admins);
//        model.addAttribute("interns", interns);
//
//        return "guide/query_to_admin"; // Thymeleaf template for guide chat
//    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Task Assignment Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    @GetMapping("/tasks_assignments")
    public String viewTaskAssignmentsPage(Model model) {
        // Fetch all task assignments
        List<TaskAssignment> tasks = taskAssignmentService.getAllTasks();

        // Add the task list to the model
        model.addAttribute("tasks", tasks);

        return "guide/task_assignments"; // Ensure this matches your actual HTML file
    }
    // ✅ Assign a New Task
    @PostMapping("/tasks/assign")
    public String assignTask(
            @RequestParam("intern") String intern,
            @RequestParam("assignedById") String assignedById,
            @RequestParam("assignedByRole") String assignedByRole,
            @RequestParam("taskDescription") String taskDescription,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Convert String to Date
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            Optional<Intern> optionalIntern = internService.getIntern(intern);
            if (optionalIntern.isPresent()) {
                TaskAssignment task = new TaskAssignment();
                task.setIntern(intern);
                task.setAssignedById(assignedById);
                task.setAssignedByRole(assignedByRole);
                task.setTaskDescription(taskDescription);
                task.setStartDate(startDate);
                task.setEndDate(endDate);
                task.setStatus("Pending");
                task.setApproved(false);

                taskAssignmentService.saveTask(task);
            }
        } catch (ParseException e) {
            // Handle exception silently
        }

        return "redirect:/bisag/guide/tasks_assignments";  // ✅ Redirects to the same task assignment page
    }

    // ✅ Get Tasks Assigned by Admin/Guide
    @GetMapping("/tasks/assignedBy/{assignedById}")
    public ResponseEntity<List<TaskAssignment>> getTasksAssignedBy(@PathVariable("assignedById") String assignedById) {
        return ResponseEntity.ok(taskAssignmentService.getTasksAssignedBy(assignedById));
    }

    // ✅ Approve Task Completion
    @PostMapping("/tasks/approve/{taskId}")
    public ResponseEntity<String> approveTask(@PathVariable("taskId") Long taskId) {
        Optional<TaskAssignment> optionalTask = taskAssignmentService.getTaskById(taskId);

        if (optionalTask.isPresent()) {
            TaskAssignment task = optionalTask.get();
            task.setApproved(true);
            task.setStatus("Completed");

            taskAssignmentService.saveTask(task);
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
            // Fetch file from local storage
            String fileName = taskOpt.get().getProofAttachment();
            Path filePath = Paths.get("uploads/task_proofs/", fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
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
            task.setStatus(newStatus); // Updating only the status
            taskAssignmentService.saveTask(task);

            return ResponseEntity.ok(Map.of("success", true, "message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
        }
    }
    }