package com.rh4.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.rh4.entities.*;
import com.rh4.repositories.*;
import com.rh4.services.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/bisag/intern")
public class InternController {

    @Value("${app.storage.base-dir}")
    private String baseDir;

    @Value("${app.storage.base-dir2}")
    private String baseDir2;

    @Autowired
    private InternService internService;
    @Autowired
    private WeeklyReportService weeklyReportService;
    @Autowired
    private MyUserService myUserService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private GuideService guideService;
    @Autowired
    private LeaveApplicationService leaveApplicationService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private InternRepo internRepo;
    @Autowired
    private UndertakingRepo undertakingRepo;
    @Autowired
    private UndertakingService undertakingService;
    @Autowired
    private ThesisStorageService thesisStorageService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private TaskAssignmentService taskAssignmentService;
    @Autowired
    private TaskAssignmentRepo taskAssignmentRepo;
    @Autowired
    private FeedBackService feedBackService;
    @Autowired
    private FeedbackRepo feedbackRepo;
    @Autowired
    HttpSession session;
    @Autowired
    GroupRepo groupRepo;
    @Autowired
    LogService logService;
    boolean WEEKLYREPORTDISABLE;
    int CurrentWeekNo;

    public Intern getSignedInIntern() {
        String username = (String) session.getAttribute("username");
        Intern intern = internService.getInternByUsername(username);
        if (intern.getIsActive()) {
            return intern;
        } else {
            return null;
        }
    }

    public String getUsername() {
        String username = (String) session.getAttribute("username");
        return username;
    }

    public Date getNextSubmissionDate() {
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        List<Intern> interns = internService.getInternsByGroupId(group.getId());
        Date oldestJoiningDate = null;

        for (Intern i : interns) {
            Date joiningDate = i.getJoiningDate();

            // Check if the oldestJoiningDate is null or if the current intern's joining
            // date is older
            if (oldestJoiningDate == null || joiningDate.before(oldestJoiningDate)) {
                oldestJoiningDate = joiningDate;
            }
        }

        System.out.println("Oldest joining date from each intern: " + oldestJoiningDate);

        Integer recentWeekNo = (Integer) weeklyReportService.getRecentWeekNo(group);
        System.out.println(recentWeekNo);

        // Calculate next submission date based on recentWeekNo and oldestJoiningDate
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldestJoiningDate);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, (recentWeekNo) * 7); // Add 6 weeks for each week number
        // compare with completion date

        return calendar.getTime();
    }

    public void checkWeeklyReportDisable() {
        if (youngestCompletionDate().before(new Date()))
            WEEKLYREPORTDISABLE = true;
        else
            WEEKLYREPORTDISABLE = false;
    }

    public Date youngestCompletionDate() {
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        List<Intern> interns = internService.getInternsByGroupId(group.getId());
        Date youngestCompletionDate = null;

        for (Intern i : interns) {
            Date completionDate = i.getCompletionDate();

            // Check if the oldestJoiningDate is null or if the current intern's joining
            // date is older
            if (youngestCompletionDate == null || completionDate.after(youngestCompletionDate)) {
                youngestCompletionDate = completionDate;
            }
        }
        return youngestCompletionDate;
    }

    public Date checkLastWeeklyReportSubmissionDate(Date nextSubmissionDate) {
        System.out.println("youngest completion date from each intern: " + youngestCompletionDate());

        if (nextSubmissionDate.after(youngestCompletionDate())) {
            return youngestCompletionDate();
        } else if (nextSubmissionDate.before(youngestCompletionDate())) {
            return nextSubmissionDate;
        } else
            return null;
    }

    public Date addDaysToYoungestCompletionDate() {
        Date youngestCompletionDate = youngestCompletionDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(youngestCompletionDate);
        calendar.add(Calendar.DAY_OF_MONTH, 7); // Add 7 days
        return calendar.getTime();
    }

    private String saveFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/"; // Change this to your actual upload directory
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs(); // Create directory if it doesn't exist
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());

            return fileName; // Return the file name to store in DB
        } catch (IOException e) {
            throw new RuntimeException("Error saving file: " + e.getMessage());
        }
    }

    @GetMapping("/intern_dashboard")
    public ModelAndView intern_dashboard(HttpSession session, Model model) {
        ModelAndView mv;

        Intern intern = getSignedInIntern();
        String internId = intern.getInternId();

        // ✅ Store internId in session to prevent null issues
        session.setAttribute("internId", internId);

        // ✅ Check if the intern has accepted the undertaking
        boolean hasAccepted = undertakingRepo.existsByIntern(internId);
        if (!hasAccepted) {
            return new ModelAndView("redirect:/bisag/intern/undertaking"); // ✅ Redirect only if not accepted
        }

        // ✅ Proceed to dashboard if undertaking is accepted
        mv = new ModelAndView("intern/intern_dashboard");
        String username = getUsername();
        InternApplication internApplication = internService.getInternApplicationByUsername(username);

        if (intern.getGroupEntity() != null) {
            mv.addObject("group", intern.getGroupEntity());
            List<Intern> interns = internService.getInternsByGroupId(intern.getGroup().getId());
            mv.addObject("interns", interns);
            mv.addObject("internCountGroupWise", interns.size());
        } else {
            mv.addObject("group", null);
        }

        if (intern.getProfilePicture() != null) {
            String encodedImage = Base64.encodeBase64String(intern.getProfilePicture());
            model.addAttribute("encodedProfilePicture", encodedImage);
        }

        session.setAttribute("id", internId);
        session.setAttribute("username", username);
        mv.addObject("username", username);
        mv.addObject("intern", intern);
        mv.addObject("internApplication", internApplication);

        String internFullName = intern.getFirstName() + " " + intern.getLastName();
        logService.saveLog(internId, "Intern Accessed Dashboard", "Intern " + internFullName + " visited their dashboard.");

        return mv;
    }

    @PostMapping("/requestCancellation")
    public String requestCancellation(HttpSession session) {
        Intern intern = getSignedInIntern();
        intern.setCancellationStatus("requested");
        internService.updateCancellationStatus(intern);
        String internFullName = intern.getFirstName() + " " + intern.getLastName();
        logService.saveLog(intern.getInternId(), "Cancellation Request Submitted", "Intern " + internFullName + " submitted cancellation request.");
        return "redirect:/bisag/intern/intern_dashboard";
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String id) {
        // Fetch intern details by their ID
        Intern intern = internService.getInternById(id);
        String internId = intern != null ? String.valueOf(intern.getInternId()) : "Unknown";

//        String internFullName = intern.getFirstName() + " " + intern.getLastName();
//        logService.saveLog(internId, "Viewed Image", internFullName + " viewed the image with ID: " + id);

        // Fetch the image data
        byte[] imageData = internService.getImageData(id);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=image.jpg")
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(byteArrayInputStream));
    }

    // project def approval
    @GetMapping("/project_definition")
    public ModelAndView project_definition(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/intern/project_definition");
        String username = getUsername();
        Intern intern = getSignedInIntern();

        String internId = intern != null ? String.valueOf(intern.getInternId()) : "Unknown";
        String internFullName = intern != null ? intern.getFirstName() + " " + intern.getLastName() : "Unknown";
        logService.saveLog(internId, "Viewed Project Definition", internFullName + " viewed the project definition page.");

        if (intern.getGroupEntity() != null) {
            mv.addObject("group", intern.getGroupEntity());
        } else {
            mv.addObject("group", null);
        }

        mv.addObject("intern", getSignedInIntern());
        return mv;
    }

    @PostMapping("/project_definition_submission")
    public String approveProjectDefinition(@RequestParam("projectDefinition") String projectDefinition,
                                           @RequestParam("description") String description,
                                           @RequestParam("projectDefinitionDocument") MultipartFile projectDefinitionDocument
    )
            throws Exception {
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();

        String storageDir = baseDir2 + group.getGroupId() + "/";
        File directory = new File(storageDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String projectDefinitionDocumentFileName = storageDir + "projectDefinitionDocument.pdf";

        Files.write(Paths.get(projectDefinitionDocumentFileName), projectDefinitionDocument.getBytes());

        group.setProjectDefinition(projectDefinition);
        group.setDescription(description);
        group.setProjectDefinitionDocument(projectDefinitionDocument.getBytes());
        group.setProjectDefinitionStatus("gpending");
        groupRepo.save(group);
        logService.saveLog(intern.getInternId(), "Project Definition Submitted", "Project Definition Submitted successfully.");
        return "redirect:/bisag/intern/project_definition";
    }

    @GetMapping("/weekly_report_submission")
    public ModelAndView weeklyReportSubmission() {
        ModelAndView mv = new ModelAndView("intern/weekly_report_submission");
        Date nextSubmissionDate = getNextSubmissionDate();
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        Integer nextSubmissionWeekNo = (Integer) weeklyReportService.getRecentWeekNo(group);
        List<WeeklyReport> weeklyReports = weeklyReportService.getReportsByGroupId(group.getId());
        checkWeeklyReportDisable();
        String weeklyReportDisable1;
        if (WEEKLYREPORTDISABLE) {
            weeklyReportDisable1 = "true";
        } else {
            weeklyReportDisable1 = "false";
        }

        // Retrieve the last weekly report
        WeeklyReport lastWeeklyReport = null;
        if (!weeklyReports.isEmpty()) {
            lastWeeklyReport = weeklyReports.get(weeklyReports.size() - 1);
        }

        // Extract deadline of the last weekly report if it exists
        Date deadlineOfLastWeeklyReport = null;
        String weeklyReportDisable2;
        if (lastWeeklyReport != null) {
            deadlineOfLastWeeklyReport = lastWeeklyReport.getDeadline();
            LocalDate localDate = deadlineOfLastWeeklyReport.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = localDate.format(formatter);

            String checkWithFormattedDate = checkLastWeeklyReportSubmissionDate(nextSubmissionDate).toString();
            if (formattedDate.equals(checkWithFormattedDate)) {
                weeklyReportDisable2 = "true";
            } else {
                weeklyReportDisable2 = "false";
            }
            mv.addObject("weeklyReportDisable2", weeklyReportDisable2);
        }

        mv.addObject("nextSubmissionDate", checkLastWeeklyReportSubmissionDate(nextSubmissionDate));
        mv.addObject("nextSubmissionWeekNo", nextSubmissionWeekNo);
        mv.addObject("weeklyReports", weeklyReports);
        mv.addObject("intern", intern);
        mv.addObject("group", group);
        mv.addObject("weeklyReportDisable1", weeklyReportDisable1);

        String internId = intern != null ? String.valueOf(intern.getInternId()) : "Unknown";
        String internFullName = intern != null ? intern.getFirstName() + " " + intern.getLastName() : "Unknown";
        logService.saveLog(internId, "Accessed Weekly Report Submission", internFullName + " accessed the weekly report submission page.");

        return mv;
    }

    @PostMapping("/weekly_report_submission")
    public String weeklyReportSubmission(@RequestParam("currentWeekNo") int currentWeekNo,
                                         @RequestParam("weeklyReportSubmission") MultipartFile weeklyReportSubmission) throws IllegalStateException, IOException, Exception {
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        Date currentDate = new Date();
        WeeklyReport weeklyReport = new WeeklyReport();
        CurrentWeekNo = currentWeekNo;

        String storageDir = baseDir2 + intern.getGroup().getGroupId() + "/Weekly Reports";
        String storageDir2 = storageDir + "/" + intern.getGroup().getGroupId() + "_" + "Week" + currentWeekNo;
        File directory = new File(storageDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String weeklyReportFileName = storageDir2 + ".pdf";

        Files.write(Paths.get(weeklyReportFileName), weeklyReportSubmission.getBytes());

        weeklyReport.setGroup(group);
        weeklyReport.setGuide(group.getGuide());
        weeklyReport.setIntern(intern);
        weeklyReport.setReportSubmittedDate(currentDate);
        weeklyReport.setSubmittedPdf(weeklyReportSubmission.getBytes());
        weeklyReport.setWeekNo(currentWeekNo);

        Date updatedNextSubmissionDate = checkLastWeeklyReportSubmissionDate(getNextSubmissionDate());

        weeklyReport.setDeadline(updatedNextSubmissionDate);
        MyUser user = myUserService.getUserByUsername(intern.getEmail());
        weeklyReport.setReplacedBy(user);

        if (weeklyReport.getDeadline().compareTo(currentDate) >= 0) {
            // status to "submitted"
            weeklyReport.setStatus("submitted");
        } else {
            // submitted"
            weeklyReport.setStatus("late submitted");
        }
        logService.saveLog(intern.getInternId(), "Submitted Weekly Report", "Report submitted successfully.");

        weeklyReportService.addReport(weeklyReport);
        return "redirect:/bisag/intern/weekly_report_submission";
    }

    @GetMapping("/change_weekly_report/{weekNo}")
    public ModelAndView chanegWeeklyReportSubmission(@PathVariable("weekNo") int weekNo) {
        ModelAndView mv = new ModelAndView("intern/change_weekly_report");
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(report.getReplacedBy().getUsername());

        String internId = intern != null ? String.valueOf(intern.getInternId()) : "Unknown";
        String internFullName = intern != null ? intern.getFirstName() + " " + intern.getLastName() : "Unknown";
        logService.saveLog(internId, "Accessed Weekly Report Change", internFullName + " accessed the change weekly report page for week: " + weekNo);

        if (user.getRole().equals("GUIDE")) {
            Guide guide = guideService.getGuideByUsername(user.getUsername());
            String status = "Your current weekly report is required some modifications given by guide. Please check it out.";
            mv.addObject("status", status);
            mv.addObject("replacedBy", guide.getName());
        } else if (user.getRole().equals("INTERN")) {
            Intern reportIntern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", reportIntern.getFirstName() + " " + reportIntern.getLastName());
            mv.addObject("status", "Your current weekly report is accepted and if any changes are required then you will be notified.");
        }

        mv.addObject("report", report);
        mv.addObject("group", group);
        return mv;
    }

    @GetMapping("/viewPdf/{internId}/{weekNo}")
    public ResponseEntity<byte[]> viewPdf(@PathVariable String internId, @PathVariable int weekNo) {
        WeeklyReport report = weeklyReportService.getReportByInternIdAndWeekNo(internId, weekNo);
        byte[] pdfContent = report.getSubmittedPdf();

        // Log the action of viewing the PDF report
        Intern intern = internService.getInternById(internId);
        String internFullName = intern != null ? intern.getFirstName() + " " + intern.getLastName() : "Unknown";
        logService.saveLog(internId, "Viewed Weekly Report PDF", internFullName + " viewed the PDF report for week " + weekNo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    @PostMapping("/change_weekly_report/changed_report")
    public String chanegWeeklyReportSubmission(@RequestParam("weekNo") int weekNo, @RequestParam("weeklyReportSubmission") MultipartFile weeklyReportSubmission)
            throws IllegalStateException, IOException, Exception {
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        CurrentWeekNo = weekNo;
        report.setSubmittedPdf(weeklyReportSubmission.getBytes());

        String storageDir = baseDir2 + intern.getGroup().getGroupId() + "/Weekly Reports/" + intern.getGroup().getGroupId() + "_Week" + weekNo + ".pdf";
        File existingFile = new File(storageDir);

        if (existingFile.exists()) {
            existingFile.delete();
        }

        Files.write(Paths.get(storageDir), weeklyReportSubmission.getBytes());

        MyUser user = myUserService.getUserByUsername(intern.getEmail());
        report.setReplacedBy(user);
        Date currentDate = new Date();
        // Check if the deadline is greater than or equal to the reportSubmittedDate
        if (report.getDeadline().compareTo(currentDate) >= 0) {
            // If the deadline is greater than or equal to the reportSubmittedDate, set the
            // status to "submitted"
            report.setStatus("submitted");
        } else {
            // If the deadline is less than the reportSubmittedDate, set the status to "late
            // submitted"
            report.setStatus("late submitted");
        }

        weeklyReportService.addReport(report);
        logService.saveLog(intern.getInternId(), "Weekly Report Changed", "Weekly Report changed successfully.");
        return "redirect:/bisag/intern/change_weekly_report/" + weekNo;
    }

    @GetMapping("/submit_forms")
    public ModelAndView submitForms() {
        ModelAndView mv = new ModelAndView("intern/submit_forms");
        Intern intern = getSignedInIntern();

        // Log the action of accessing the submit forms page
        String internId = intern != null ? String.valueOf(intern.getInternId()) : "Unknown";
        String internFullName = intern != null ? intern.getFirstName() + " " + intern.getLastName() : "Unknown";
        logService.saveLog(internId, "Accessed Submit Forms", internFullName + " accessed the submit forms page.");

        mv.addObject("intern", intern);
        return mv;
    }

    @PostMapping("/submit_forms")
    public String submitForms(@RequestParam("permanentAddress") String permanentAddress,
                              @RequestParam("dateOfBirth") java.sql.Date dateOfBirth,
                              @RequestParam("gender") String gender,
                              @RequestParam("collegeGuideHodName") String collegeGuideHodName,
                              @RequestParam("aggregatePercentage") Double aggregatePercentage,
                              @RequestParam("usedResource") String usedResource,
                              @RequestParam("registrationForm") MultipartFile registrationForm,
                              @RequestParam("securityForm") MultipartFile securityForm,
                              @RequestParam("projectDefinitionForm") MultipartFile projectDefinitionForm,
                              @RequestParam("icardForm") MultipartFile icardForm) throws IllegalStateException, IOException, Exception {
        System.out.println("called sub");
        Intern intern = getSignedInIntern();

        String storageDir = baseDir + intern.getEmail() + "/";
        File directory = new File(storageDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save files to local storage
        String registrationFormName = storageDir + "registrationForm.pdf";
        String securityFormName = storageDir + "securityForm.pdf";
        String icardFormName = storageDir + "icardForm.pdf";
        String projectDefinitionFormName = storageDir + "projectDefinitnionForm.pdf";
        String extraForm = storageDir + "extraForm.pdf";

        // Save Passport Size Image
        Files.write(Paths.get(registrationFormName), registrationForm.getBytes());

        // Save College Icard Image
        Files.write(Paths.get(securityFormName), securityForm.getBytes());

        // Save NOC PDF
        Files.write(Paths.get(icardFormName), icardForm.getBytes());

        //Save Project Definition form
        Files.write(Paths.get(projectDefinitionFormName), projectDefinitionFormName.getBytes());

        //Save Project Definition form
        Files.write(Paths.get(extraForm), extraForm.getBytes());

        intern.setPermanentAddress(permanentAddress);
        intern.setDateOfBirth(dateOfBirth);
        intern.setGender(gender);
        intern.setCollegeGuideHodName(collegeGuideHodName);
        intern.setAggregatePercentage(aggregatePercentage);
        intern.setUsedResource(usedResource);
        intern.setIcardForm(icardForm.getBytes());
        intern.setProjectDefinitionForm(projectDefinitionForm.getBytes());
        intern.setExtraForm(extraForm.getBytes());
        intern.setSecurityForm(securityForm.getBytes());
        intern.setRegistrationForm(registrationForm.getBytes());
        internService.registerIntern(intern);
        logService.saveLog(intern.getInternId(), "Submitted forms", "Forms Submitted successfully.");
        return "redirect:/bisag/intern/submit_forms";
    }

    @GetMapping("/documents/icardForm/{id}")
    public ResponseEntity<byte[]> getICardFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getIcardForm();

            if (pdf != null) {

                logService.saveLog(id, "Viewed I-Card Form",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their I-Card form.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/projectDefinitionForm/{id}")
    public ResponseEntity<byte[]> getProjectDefinitionFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getProjectDefinitionForm();

            if (pdf != null) {

                logService.saveLog(id, "Viewed Project Definition Form",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their Project Definition form.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/extraForm/{id}")
    public ResponseEntity<byte[]> getExtraFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getExtraForm();

            if (pdf != null) {
                logService.saveLog(id, "Viewed Extra Form",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their Extra form.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/security/{id}")
    public ResponseEntity<byte[]> getSecurityFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getSecurityForm();

            if (pdf != null) {
                logService.saveLog(id, "Viewed Security Form",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their security form.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/registration/{id}")
    public ResponseEntity<byte[]> getRegistrationFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getRegistrationForm();

            if (pdf != null) {
                logService.saveLog(id, "Viewed Registration Form",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their registration form.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/resume/{id}")
    public ResponseEntity<byte[]> getResumePdfForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getResumePdf();

            if (pdf != null) {
                logService.saveLog(id, "Viewed Resume",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their resume.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/noc/{id}")
    public ResponseEntity<byte[]> getNocPdfForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] pdf = application.getNocPdf();

            if (pdf != null) {
                logService.saveLog(id, "Viewed NOC Form",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their NOC form.");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/icard/{id}")
    public ResponseEntity<byte[]> getICardImageForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] image = application.getCollegeIcardImage();

            if (image != null) {
                logService.saveLog(id, "Viewed College I-Card",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their college I-Card.");
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/documents/passport/{id}")
    public ResponseEntity<byte[]> getPassportSizeImageForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            byte[] image = application.getPassportSizeImage();

            if (image != null) {

                logService.saveLog(id, "Viewed Passport Image",
                        "Intern " + application.getFirstName() + " " + application.getLastName() + " accessed their passport-size image.");

                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/query")
    public ModelAndView query() {
        ModelAndView mv = new ModelAndView("/intern/query");
        List<Admin> admins = adminService.getAdmin();
        List<Guide> guides = guideService.getGuide();
        Intern intern = getSignedInIntern();

        if (intern.getGroupEntity() != null) {
            mv.addObject("group", intern.getGroupEntity());
        } else {
            mv.addObject("group", null);
        }

        mv.addObject("admins", admins);
        mv.addObject("guides", guides);
        mv.addObject("intern", intern);

        logService.saveLog(intern.getInternId(), "Viewed Query Page",
                "Intern " + intern.getFirstName() + " " + intern.getLastName() + " accessed the query page.");

        return mv;
    }

    @GetMapping("/final_report_submission")
    public ModelAndView finalReportSubmission(HttpSession session, Model model) {
        ModelAndView mv = new ModelAndView("/intern/final_report_submission");

        Intern intern = getSignedInIntern();

        if (intern.getGroupEntity() != null) {
            mv.addObject("group", intern.getGroupEntity());
        } else {
            mv.addObject("group", null);
        }

        Date deadlineOfFinalReport = addDaysToYoungestCompletionDate();
        String submitDisable = deadlineOfFinalReport.after(new Date()) ? "false" : "true";

        mv.addObject("intern", intern);
        mv.addObject("deadline", deadlineOfFinalReport);
        mv.addObject("submitDisable", submitDisable);

        logService.saveLog(intern.getInternId(), "Viewed Final Report Submission Page",
                "Intern " + intern.getFirstName() + " " + intern.getLastName() + " accessed the final report submission page.");

        return mv;
    }

    @PostMapping("/final_report_submission")
    public String finalReportSubmission(@RequestParam("finalReport") MultipartFile finalReport) throws Exception {
        Intern intern = getSignedInIntern();
        GroupEntity group = intern.getGroup();
        String storageDir = baseDir + intern.getEmail() + "/";
        File directory = new File(storageDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String finalReportFileName = storageDir + "finalReport.pdf";

        Files.write(Paths.get(finalReportFileName), finalReport.getBytes());
        group.setFinalReport(finalReport.getBytes());
        group.setFinalReportStatus("gpending");
        groupRepo.save(group);
        logService.saveLog(intern.getInternId(), "Final Report Submitted", "Final Report Submitted successfully.");
        return "redirect:/bisag/intern/final_report_submission";
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Intern intern = getSignedInIntern();
        logService.saveLog(intern.getInternId(), "Changed Password", "Password Changed successfully.");
        internService.changePassword(intern, newPassword);
        return "redirect:/logout";
    }

    @GetMapping("/apply_leave")
    public ModelAndView applyLeave() {
        Intern intern = getSignedInIntern();
        ModelAndView mv = new ModelAndView("intern/apply_leave");

        float totalAttendance = attendanceService.calculateTotalAttendance(intern.getInternId());

        List<LeaveApplication> leaveApplications = leaveApplicationService.getInternLeaves(intern.getInternId());

        LeaveApplication lastLeave = leaveApplicationService.getLastLeaveApplication(intern.getInternId());

        List<LeaveApplication> leaveHistory = leaveApplicationService.getInternLeaveHistory(intern.getInternId());

        // Pass data to the template
        mv.addObject("intern", intern);
        mv.addObject("totalAttendance", totalAttendance);
        mv.addObject("leaveApplications", leaveApplications);
        mv.addObject("leaveApplication", new LeaveApplication());
        mv.addObject("lastLeave", lastLeave);
        mv.addObject("leaveHistory", leaveHistory);

        // Log the action
        logService.saveLog(intern.getInternId(), "Viewed Leave Application Page",
                "Intern " + intern.getFirstName() + " " + intern.getLastName() + " accessed the leave application page.");

        return mv;
    }

//    @PostMapping("/apply_leave")
//    public String applyLeave(@ModelAttribute LeaveApplication leaveApplication, Principal principal) {
//        leaveApplication.setInternId(principal.getName());
//        leaveApplicationService.applyForLeave(leaveApplication);
//        return "redirect:/bisag/intern/apply_leave";
//    }

    @PostMapping("/submit_leave")
    public String submitLeave(@RequestParam("internId") String internId,
                              @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
                              @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
                              @RequestParam("leaveType") String leaveType, // New field
                              LeaveApplication leaveApplication) {

        if (internId == null || internId.isEmpty()) {
            throw new RuntimeException("Intern ID is missing!");
        }

        Intern intern = internRepo.findById(internId)
                .orElseThrow(() -> new RuntimeException("Intern not found"));

        leaveApplication.setFromDate(fromDate);
        leaveApplication.setToDate(toDate);
        leaveApplication.setInternId(internId);
        leaveApplication.setLeaveType(leaveType);
        leaveApplication.setStatus("Pending");
        leaveApplication.setSubmittedOn(LocalDateTime.now());

        leaveApplicationRepo.save(leaveApplication);

        logService.saveLog(internId, "Leave Application Submitted",
                "Intern " + intern.getFirstName() + " applied for leave from " + fromDate + " to " + toDate + " (" + leaveType + ")");

        return "redirect:/bisag/intern/apply_leave?success=true";
    }

    @PostMapping("/{internId}/profile-picture")
    public String updateProfilePicture(
            @PathVariable("internId") String internId,
            @RequestParam("profilePicture") MultipartFile profilePicture) {
        try {
            byte[] profilePictureBytes = profilePicture.getBytes();
            internService.updateInternProfilePicture(internId, profilePictureBytes);
            logService.saveLog(internId, "Updated Profile Picture", "Intern updated profile picture successfully.");
            return "redirect:/bisag/intern/intern_dashboard";
        } catch (IOException e) {
            logService.saveLog(internId, "Failed Profile Picture Update", "Error occurred while updating profile picture.");
            return "redirect:/bisag/intern/intern_dashboard";
        }
    }

    // ========================= Intern Undertaking Acceptance ==========================

    //  Check if Intern has accepted the undertaking (Once in a lifetime)
    @GetMapping("/check-undertaking")
    public ResponseEntity<Boolean> checkUndertaking(HttpSession session) {
        String internId = (String) session.getAttribute("internId");

        // If the intern has already accepted, return true
        boolean hasAccepted = undertakingRepo.existsByIntern(internId);
        return ResponseEntity.ok(hasAccepted);
    }

    //  Display the Undertaking Form (Only if not accepted)
    @GetMapping("/undertaking")
    public ModelAndView showUndertakingForm(HttpSession session) {
        String internId = (String) session.getAttribute("internId");

        //  Check if intern has already accepted the undertaking
        boolean hasAccepted = undertakingRepo.existsByIntern(internId);
        if (hasAccepted) {
            return new ModelAndView("redirect:/bisag/intern/intern_dashboard"); // Redirect if already accepted
        }

        ModelAndView mv = new ModelAndView("intern/undertaking");
        String undertakingContent = undertakingRepo.findLatestUndertakingContent();

        if (undertakingContent == null || undertakingContent.isEmpty()) {
            undertakingContent = "No undertaking content available.";
        }

        mv.addObject("undertakingContent", undertakingContent);
        return mv;
    }

    //  Accept Undertaking (Save only if not already accepted)
    @PostMapping("/accept-undertaking")
    public ResponseEntity<Boolean> acceptUndertaking(HttpSession session) {
        String internId = (String) session.getAttribute("internId");

        // Check if already accepted
        if (undertakingRepo.existsByIntern(internId)) {
            return ResponseEntity.ok(true); // Already accepted, no need to save again
        }

        // Save new acceptance
        Undertaking undertaking = new Undertaking();
        undertaking.setIntern(internId);
        undertakingRepo.save(undertaking);

        return ResponseEntity.ok(true);
    }

    @GetMapping("/undertaking-content")
    @ResponseBody
    public String getLatestUndertakingContent() {
        String latestContent = undertakingRepo.findLatestUndertakingContent();
        return (latestContent != null && !latestContent.isEmpty()) ? latestContent : "No undertaking content available.";
    }
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-View Thesis PDF_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    @GetMapping("/view-thesis/{id}")
    public ResponseEntity<Resource> viewThesis(@PathVariable Long id, Principal principal) throws IOException {
        Optional<ThesisStorage> optionalThesisStorage = thesisStorageService.getThesisById(id);

        if (optionalThesisStorage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ThesisStorage thesisStorage = optionalThesisStorage.get();

        String emailOrUsername = principal.getName();

        Optional<Intern> optionalIntern = Optional.ofNullable(internService.getInternByUsername(getUsername())); // Change this based on your login system

        if (optionalIntern.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Intern not found
        }

        Intern loggedInIntern = optionalIntern.get();
        String loggedInInternId = loggedInIntern.getInternId();

        if (!loggedInInternId.equals(thesisStorage.getAllowedInternId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (thesisStorage.getFilePath() == null || thesisStorage.getFilePath().isEmpty()) {
            System.err.println("Error: Thesis file path is null or empty for ID: " + id);
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(thesisStorage.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // Set headers for direct access
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Messaging Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    // Intern sends a message
    @PostMapping("/chat/send")
    public ResponseEntity<Message> sendMessageAsIntern(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam String messageText) {

        // Extract actual internId (assuming senderId is passed as a string)
        Optional<Intern> intern = internService.findById(senderId);
        if (intern.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Message message = messageService.sendMessage(intern.get().getInternId(), receiverId, messageText);
        return ResponseEntity.ok(message);
    }

    // Intern fetches chat history (both sent and received messages)
    @GetMapping("/chat/history")
    public ResponseEntity<List<Message>> getChatHistoryAsIntern(
            @RequestParam String senderId,
            @RequestParam String receiverId) {

        Optional<Intern> intern = internService.findById(senderId);
        if (intern.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String internId = intern.get().getInternId();

        // Fetch both sent and received messages
        List<Message> messages = messageService.getChatHistory(internId, receiverId);
        messages.addAll(messageService.getChatHistory(receiverId, internId)); // Fetch messages in reverse order

        messages.sort(Comparator.comparing(Message::getTimestamp));

        return ResponseEntity.ok(messages);
    }

    //Task Assignment Module
    @GetMapping("/tasks")
    public String viewInternTasks(Model model) {
        // Fetch the logged-in intern using the existing method
        Intern loggedIntern = getSignedInIntern();

        if (loggedIntern == null) {
            return "redirect:/intern/login"; // Redirect if not authenticated or inactive
        }

        // Fetch tasks assigned to the logged-in intern
        List<TaskAssignment> tasks = taskAssignmentService.getTasksByIntern(loggedIntern.getInternId());
        model.addAttribute("tasks", tasks);

        return "intern/intern-tasks"; // Returns the intern's task list page
    }
    //  Get Tasks Assigned to a Specific Intern
    @GetMapping("/tasks/{internId}")
    public String getInternTasks(@PathVariable("internId") String internId, Model model) {
        List<TaskAssignment> tasks = taskAssignmentService.getTasksByIntern(internId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("internId", internId); // Add internId for navigation
        return "intern-tasks"; // Renders the intern's task list page
    }
    //  Update Task Status by Intern
    @PutMapping("/tasks/update-status/{taskId}")
    public ResponseEntity<Map<String, String>> updateTaskStatus(@PathVariable("taskId") Long taskId, @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        boolean isUpdated = taskAssignmentService.updateTaskStatus(taskId, newStatus);

        Map<String, String> response = new HashMap<>();
        if (isUpdated) {
            response.put("success", "Task status updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Failed to update task status");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/tasks/upload-proof/{taskId}")
    public ResponseEntity<Map<String, String>> uploadProof(@PathVariable Long taskId, @RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<TaskAssignment> taskOpt = taskAssignmentRepo.findById(taskId);
            if (!taskOpt.isPresent()) {
                response.put("error", "Task not found");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if not exists
            String uploadDir = "uploads/task_proofs/";
            Files.createDirectories(Paths.get(uploadDir));

            // Save file in local storage
            String fileName = "task_" + taskId + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, file.getBytes());

            TaskAssignment task = taskOpt.get();
            task.setProofAttachment(fileName);
            taskAssignmentRepo.save(task);

            response.put("success", "Proof uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to upload proof: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/tasks/proof/{taskId}")
    public ResponseEntity<Resource> getProofAttachment(@PathVariable Long taskId) {
        Optional<TaskAssignment> taskOpt = taskAssignmentRepo.findById(taskId);

        if (!taskOpt.isPresent() || taskOpt.get().getProofAttachment() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
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

    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_Feedback Module-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
    //-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
    @GetMapping("/feedback_form")
    public String showFeedbackForm(Model model) {
        Intern currentIntern = getSignedInIntern();

        List<Intern> internList = new ArrayList<>();
        internList.add(currentIntern);

        Feedback feedback = new Feedback();
        feedback.setInternId(currentIntern.getInternId());
        feedback.setFirstName(currentIntern.getFirstName());  // Set firstName
        feedback.setLastName(currentIntern.getLastName());    // Set lastName

        model.addAttribute("interns", internList);
        model.addAttribute("feedback", feedback);
        return "intern/feedback_form";
    }

    @PostMapping("/feedback_form")
    public String submitFeedback(@ModelAttribute Feedback feedback) {
        try {
            Intern currentIntern = getSignedInIntern();
            System.out.println("Current Intern ID: " + currentIntern.getInternId());
            System.out.println("Current Intern First Name: " + currentIntern.getFirstName());
            System.out.println("Current Intern Last Name: " + currentIntern.getLastName());

            feedback.setInternId(currentIntern.getInternId());
            feedback.setFirstName(currentIntern.getFirstName());
            feedback.setLastName(currentIntern.getLastName());

            feedBackService.saveFeedback(feedback);
            return "redirect:/bisag/intern/intern_dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/bisag/intern/feedback_form?error=true";
        }
    }

}