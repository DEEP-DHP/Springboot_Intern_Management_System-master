package com.rh4.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.rh4.repositories.*;
import com.rh4.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.rh4.models.ReportFilter;
import com.rh4.services.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bisag/admin")
public class AdminController {

    @Autowired
    HttpSession session;
    @Autowired
    AdminRepo adminRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private InternService internService;
    @Autowired
    private MyUserService myUserService;
    @Autowired
    private InternApplicationRepo internApplicationRepo;
    @Autowired
    private EmailSenderService emailService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private WeeklyReportService weeklyReportService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private GroupRepo groupRepo;
    @Autowired
    private CancelledRepo cancelledRepo;
    @Autowired
    private InternRepo internRepo;
    @Autowired
    private GuideService guideService;
    @Autowired
    private DataExportService dataExportService;
    @Autowired
    private ThesisService thesisService;
    @Autowired
    private LogService logService;
    @Autowired
    private InternApplicationService internApplicationService;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private AttendanceRepo attendanceRepo;
    @Autowired
    private RecordService recordService;
    @Autowired
    private LeaveApplicationService leaveApplicationService;
    @Autowired
    private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private UndertakingService undertakingService;
    @Autowired
    private UndertakingRepo undertakingRepo;
    @Autowired
    private ThesisStorageRepo thesisStorageRepo;


    @Value("${app.storage.base-dir}")
    private String baseDir;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private GroupEntity groupEntity;
    @Autowired
    private LogRepo logRepo;

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Admin adminName(HttpSession session) {
        String username = (String) session.getAttribute("username");
        return adminService.getAdminByUsername(username);
    }

    public Admin getSignedInAdmin() {
        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        return admin;
    }

    public String generateInternId() {
        // Generate custom internId using current year and serial number
        SimpleDateFormat yearFormat = new SimpleDateFormat("yy");
        String currentYear = yearFormat.format(new Date());

        // Assuming you have a method to get the next serial number
        int serialNumber = generateSerialNumber();
        ++serialNumber;
        // Combine the parts to form the custom internId
        String sno = String.valueOf(serialNumber);
        String formattedSerialNumber = String.format("%04d", Integer.parseInt(sno));
        System.out.println("serialNumber..." + serialNumber);
        System.out.println("formated..." + formattedSerialNumber);
        String internId = currentYear + "BISAG" + formattedSerialNumber;
        return internId;
    }

    public int generateSerialNumber() {

        String id = internService.getMostRecentInternId();
        if (id == null)
            return 0;
        String serialNumber = id.substring(id.length() - 4);
        int lastFourDigits = Integer.parseInt(serialNumber);
        return lastFourDigits;
    }

//	public String generateGroupId() {
//		// Generate custom groupId using current year and serial number
//		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
//		String currentYear = yearFormat.format(new Date());
//
//		// Assuming you have a method to get the next serial number
//		int serialNumber = generateSerialNumberForGroup();
//		++serialNumber;
//		// Combine the parts to form the custom internId
//		String sno = String.valueOf(serialNumber);
//		String formattedSerialNumber = String.format("%03d", Integer.parseInt(sno));
//		System.out.println("serialNumber..." + serialNumber);
//		System.out.println("formated..." + formattedSerialNumber);
//		String groupId = currentYear + "G" + formattedSerialNumber;
//		return groupId;
//	}
//
//	public int generateSerialNumberForGroup() {
//
//		String id = groupService.getMostRecentGroupId();
//		if (id == null)
//			return 0;
//		String serialNumber = id.substring(id.length() - 3);
//		int lastThreeDigits = Integer.parseInt(serialNumber);
//		return lastThreeDigits;
//	}

    // Method to generate the group ID
    public String generateGroupId() {
        // Check if the current year has changed
        int year = getCurrentYear();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String currentYear = yearFormat.format(new Date());
        int currentYearInt = Integer.parseInt(currentYear);
        int serialNumber = generateSerialNumberForGroup();
        if (year != currentYearInt) {
            currentYearInt = year; // Update the current year
            serialNumber = 0;   // Reset the serial number to 0
        }

        // Increment the serial number
        serialNumber++;

        // Format the serial number
        String formattedSerialNumber = String.format("%03d", serialNumber);

        // Combine the parts to form the custom group ID
        String groupId = currentYear + "G" + formattedSerialNumber;

        return groupId;
    }

    // Method to get the current year
    private int getCurrentYear() {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String yearString = yearFormat.format(new Date());
        return Integer.parseInt(yearString);
    }

    // Method to generate the serial number for group
    private int generateSerialNumberForGroup() {
        String id = groupService.getMostRecentGroupId();
        if (id == null || id.isEmpty()) {
            return 0;
        } else {
            // Extract the serial number part from the group ID
            String serialNumberString = id.substring(5); // Assuming "yyyyG" prefix
            return Integer.parseInt(serialNumberString);
        }
    }

    Model countNotifications(Model model) {
        // count

        //count approve for interview
        long approveForInterviewApplicationsCount = internService.approveForInterviewApplicationsCount();
        model.addAttribute("interviewPendingApplicationsCount", approveForInterviewApplicationsCount);

        //count approve for internship(get interview pending results )
        long countPendingInterviewApplications = internService.countPendingInterviewApplications();
        model.addAttribute("countInterviewApplications", countPendingInterviewApplications);

        //Project definitions remaining to approve from admin
        long adminPendingProjectDefinitionCount = groupService.adminPendingProjectDefinitionCount();
        model.addAttribute("adminPendingProjectDefinitionCount", adminPendingProjectDefinitionCount);

        long pendingGroupCreationCount = internService.pendingGroupCreationCount();
        model.addAttribute("pendingGroupCreationCount", pendingGroupCreationCount);

        //cancellation request count
        long requestedCancellationsCount = internService.countRequestedCancellations();
        model.addAttribute("requestedCancellationsCount", requestedCancellationsCount);

        long countGuide = guideService.countGuides();
        model.addAttribute("countGuide", countGuide);

        // New notifications
        long pendingLeaveApplicationsCount = leaveApplicationService.countPendingLeaveApplications();
        model.addAttribute("pendingLeaveApplicationsCount", pendingLeaveApplicationsCount);

        long pendingVerificationRequestsCount = verificationService.countPendingVerificationRequests();
        model.addAttribute("pendingVerificationRequestsCount", pendingVerificationRequestsCount);

        return model;
    }

    // Method to reject a student and log the action
//    public void rejectStudent(String firstName, String lastName, String contactNo, String email,
//                              String collegeName, String branch, int semester, String degree,
//                              String domain, String reason) {
//        logService.saveLog(firstName, lastName, contactNo, email, collegeName, branch,
//                semester, degree, domain, "Rejected", reason);
//    }
//
//    // Method to fail a student and log the action
//    public void failStudent(String firstName, String lastName, String contactNo, String email,
//                            String collegeName, String branch, int semester, String degree,
//                            String domain, String reason) {
//        logService.saveLog(firstName, lastName, contactNo, email, collegeName, branch,
//                semester, degree, domain, "Failed", reason);
//    }

    // Admin Dashboard

    @GetMapping("/admin_dashboard")
    public ModelAndView admin_dashboard(Model model) {

        ModelAndView mv = new ModelAndView("admin/admin_dashboard");

        String username = (String) session.getAttribute("username");

        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {
            session.setAttribute("id", admin.getAdminId());
            session.setAttribute("username", username);

            logService.saveLog(String.valueOf(admin.getAdminId()), "Admin Dashboard Accessed",
                    "Admin " + admin.getName() + " accessed the dashboard.");

            model = countNotifications(model);

            long countInterns = internService.countInterns();
            model.addAttribute("countInterns", countInterns);

            // Add the username to the ModelAndView
            mv.addObject("username", username);
            mv.addObject("admin", admin);
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return mv;
    }

    // Group Manage

    // Intern Management///////////////////////////////////////////////

    @GetMapping("/register_intern")
    public String registerIntern() {
        return "admin/InternRegistration";
    }

    @PostMapping("/register_intern")
    public String registerIntern(@ModelAttribute("intern") Intern intern) {
        intern.setInternId(generateInternId());
        internService.registerIntern(intern);

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        Admin admin = adminService.getAdminByUsername(adminUsername);

        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Intern Registered",
                    "Admin " + admin.getName() + " registered a new intern with ID: " + intern.getInternId());
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return "redirect:/bisag";
    }

    @GetMapping("/intern/{id}")
    public ModelAndView internDetails(@PathVariable("id") String id, Model model) {
        ModelAndView mv = new ModelAndView();

        Optional<Intern> intern = internService.getIntern(id);
        model = countNotifications(model);

        mv.addObject("intern", intern);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
        List<Branch> branches = fieldService.getBranches();
        List<GroupEntity> groups = groupService.getGroups();

        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
        mv.addObject("branches", branches);
        mv.addObject("groups", groups);

        mv.setViewName("admin/intern_detail");

        Admin admin = getSignedInAdmin();
        if (admin != null && intern.isPresent()) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Intern Details",
                    "Admin " + admin.getName() + " viewed the details of intern ID: " + id + ", Name: " + intern.get().getFirstName() + " " + intern.get().getLastName());
        } else {
            System.out.println("Error: Admin or Intern not found for logging!");
        }

        return mv;
    }

    @GetMapping("/update_admin/{id}")
    public ModelAndView updateAdmin(@PathVariable("id") long id, Model model) {
        ModelAndView mv = new ModelAndView("super_admin/update_admin");

        Optional<Admin> admin = adminService.getAdmin(id);
        mv.addObject("admin", admin.orElse(new Admin()));

        model = countNotifications(model);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null && admin.isPresent()) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Updating Admin Details",
                    "Admin " + signedInAdmin.getName() + " is updating the details of admin ID: " + id + ", Name: " + admin.get().getName());
        } else {
            System.out.println("Error: Signed-in admin or target admin not found for logging!");
        }

        return mv;
    }

    @PostMapping("/update_admin/{id}")
    public String updateAdmin(@ModelAttribute("admin") Admin admin, @PathVariable("id") long id) {
        Optional<Admin> existingAdmin = adminService.getAdmin(admin.getAdminId());

        if (existingAdmin.isPresent()) {

            String currentPassword = existingAdmin.get().getPassword();
            Admin updatedAdmin = existingAdmin.get();
            updatedAdmin.setName(admin.getName());
            updatedAdmin.setLocation(admin.getLocation());
            updatedAdmin.setContactNo(admin.getContactNo());
            updatedAdmin.setEmailId(admin.getEmailId());

            Admin signedInAdmin = getSignedInAdmin();
            if (signedInAdmin != null) {
                logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Updating Admin Details",
                        "Admin " + signedInAdmin.getName() + " updated the details of admin ID: " + id + ", Name: " + updatedAdmin.getName());
            } else {
                System.out.println("Error: Signed-in admin not found for logging!");
            }

            adminService.updateAdmin(updatedAdmin, existingAdmin);
        }
        return "redirect:/logout";
    }

    // Manage intern application///////////////////////////////////
    @GetMapping("/intern_application")
    public ModelAndView internApplication(Model model) {
        ModelAndView mv = new ModelAndView("admin/intern_application");

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Applications",
                    "Admin " + signedInAdmin.getName() + " accessed the intern application page.");
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        List<InternApplication> interns = internService.getInternApplication();
        model = countNotifications(model);
        mv.addObject("interns", interns);
        mv.addObject("admin", adminName(session));

        return mv;
    }

    @GetMapping("/intern_application/{id}")
    public ModelAndView internApplication(@PathVariable("id") long id, Model model) {
        System.out.println("id" + id);
        ModelAndView mv = new ModelAndView();

        Optional<InternApplication> intern = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Application Details",
                    "Admin " + signedInAdmin.getName() + " viewed the details of Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        // Adding attributes to the ModelAndView for rendering the page
        mv.addObject("intern", intern);
        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
        List<Branch> branches = fieldService.getBranches();
        model = countNotifications(model);

        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
        mv.addObject("branches", branches);

        mv.setViewName("admin/intern_application_detail");
        return mv;
    }

    @GetMapping("/intern_application_docs/{id}")
    public ModelAndView internApplicationDocs(@PathVariable("id") long id, Model model) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id); // Implement this service method
        System.out.println("ID: " + id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Application Documents",
                    "Admin " + signedInAdmin.getName() + " viewed the documents for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        // Check if the intern application exists
        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            model.addAttribute("id", id);
            model.addAttribute("passportSizeImage", application.getPassportSizeImage());
            model.addAttribute("collegeIcardImage", application.getCollegeIcardImage());
            model.addAttribute("nocPdf", application.getNocPdf());
            model.addAttribute("resumePdf", application.getResumePdf());
        } else {
            model.addAttribute("error", "Intern Application not found");
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/intern_application_docs");
        return mv;
    }

    @GetMapping("/intern_docs/{id}")
    public ModelAndView internDocs(@PathVariable("id") String id, Model model) {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        System.out.println("ID: " + id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Intern Documents",
                    "Admin " + signedInAdmin.getName() + " viewed the documents for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        // Check if the intern exists
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get(); // Retrieve the Intern object
            model.addAttribute("id", id);
            model.addAttribute("intern", application);
            model.addAttribute("passportSizeImage", application.getPassportSizeImage());
            model.addAttribute("collegeIcardImage", application.getCollegeIcardImage());
            model.addAttribute("nocPdf", application.getNocPdf());
            model.addAttribute("resumePdf", application.getResumePdf());
            model.addAttribute("icardForm", application.getIcardForm());
            model.addAttribute("registrationForm", application.getRegistrationForm());
            model.addAttribute("securityForm", application.getSecurityForm());
        } else {
            model.addAttribute("error", "Intern Application not found");
        }

        // Return the view with the model and data
        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/intern_docs");
        return mv;
    }

    @GetMapping("/documents/passport/{id}")
    public ResponseEntity<byte[]> getPassportSizeImageForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " viewed the passport size image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] image = application.getPassportSizeImage();

            if (image != null) {
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

    @GetMapping("/intern/documents/passport/{id}")
    public ResponseEntity<byte[]> getPassportSizeImageForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " viewed the passport size image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] image = application.getPassportSizeImage();

            if (image != null) {
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

    @GetMapping("/documents/icard/{id}")
    public ResponseEntity<byte[]> getICardImageForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View College I-card Image",
                    "Admin " + signedInAdmin.getName() + " viewed the college I-card image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] image = application.getCollegeIcardImage();

            if (image != null) {
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

    @GetMapping("/intern/documents/icard/{id}")
    public ResponseEntity<byte[]> getICardImageForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View College I-card Image",
                    "Admin " + signedInAdmin.getName() + " viewed the college I-card image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] image = application.getCollegeIcardImage();

            if (image != null) {
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

    @GetMapping("/documents/noc/{id}")
    public ResponseEntity<byte[]> getNocPdfForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View NOC PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the NOC PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] pdf = application.getNocPdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/noc/{id}")
    public ResponseEntity<byte[]> getNocPdfForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View NOC PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the NOC PDF for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getNocPdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documents/resume/{id}")
    public ResponseEntity<byte[]> getResumePdfForInternApplication(@PathVariable("id") long id) {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Resume PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the Resume PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();

            byte[] pdf = application.getResumePdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/resume/{id}")
    public ResponseEntity<byte[]> getResumePdfForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        Admin signedInAdmin = getSignedInAdmin();
        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "View Resume PDF",
                    "Admin " + signedInAdmin.getName() + " viewed the Resume PDF for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getResumePdf();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/registration/{id}")
    public ResponseEntity<byte[]> getRegistrationFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getRegistrationForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/security/{id}")
    public ResponseEntity<byte[]> getSecurityFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getSecurityForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/intern/documents/icardForm/{id}")
    public ResponseEntity<byte[]> getICardFormForIntern(@PathVariable("id") String id) {
        Optional<Intern> optionalApplication = internService.getIntern(id);

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            byte[] pdf = application.getIcardForm();

            if (pdf != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/documents/passport/{id}")
    public String updatePassportSizeImage(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin(); // Assuming you have a method to get the logged-in admin

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " updated the passport size image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "passportSizeImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "passportSizeImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setPassportSizeImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/documents/icard/{id}")
    public String updateICardImage(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update I-Card Image",
                    "Admin " + signedInAdmin.getName() + " updated the college I-Card image for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "collegeIcardImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "collegeIcardImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setCollegeIcardImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/documents/noc/{id}")
    public String updateNocPdf(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update NOC PDF",
                    "Admin " + signedInAdmin.getName() + " updated the NOC PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "nocPdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "nocPdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setNocPdf(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/documents/resume/{id}")
    public String updateResumePdf(@PathVariable("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<InternApplication> optionalApplication = internService.getInternApplication(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update Resume PDF",
                    "Admin " + signedInAdmin.getName() + " updated the Resume PDF for Intern Application with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            InternApplication application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "resumePdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "resumePdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setResumePdf(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_application_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_application_docs/" + id;
    }

    @PostMapping("/intern/documents/passport/{id}")
    public String updatePassportSizeImageForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update Passport Size Image",
                    "Admin " + signedInAdmin.getName() + " updated the passport size image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "passportSizeImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "passportSizeImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setPassportSizeImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/icard/{id}")
    public String updateICardImageForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        Admin signedInAdmin = getSignedInAdmin();

        if (signedInAdmin != null) {
            logService.saveLog(String.valueOf(signedInAdmin.getAdminId()), "Update I-Card Image",
                    "Admin " + signedInAdmin.getName() + " updated the college I-Card image for Intern with ID: " + id);
        } else {
            System.out.println("Error: Signed-in admin not found for logging!");
        }

        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "collegeIcardImage.jpg";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "collegeIcardImage.jpg";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setCollegeIcardImage(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/noc/{id}")
    public String updateNocPdfForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();
            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "nocPdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "nocPdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());
            application.setNocPdf(file.getBytes());
            internService.save(application);
            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/resume/{id}")
    public String updateResumePdfForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "resumePdf.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "resumePdf.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setResumePdf(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/icardForm/{id}")
    public String updateICardFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "icardForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "icardForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setIcardForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/registration/{id}")
    public String updateRegistrationFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "registrationForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "registrationForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setRegistrationForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
    }

    @PostMapping("/intern/documents/security/{id}")
    public String updateSecurityFormForIntern(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<Intern> optionalApplication = internService.getIntern(id);
        if (optionalApplication.isPresent()) {
            Intern application = optionalApplication.get();

            String storageDir = baseDir + application.getEmail() + "/";
            String oldFilePath = storageDir + "securityForm.pdf";

            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            String newFilePath = storageDir + "securityForm.pdf";
            Files.write(Paths.get(newFilePath), file.getBytes());

            application.setSecurityForm(file.getBytes());
            internService.save(application);

            return "redirect:/bisag/admin/intern_docs/" + id;
        }
        return "redirect:/bisag/admin/intern_docs/" + id;
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

            // Send email notifications based on status
            if (status.equals("rejected")) {
                emailService.sendSimpleEmail(intern.get().getEmail(),
                        "Notification: Rejection of BISAG Internship Application\r\n" + "\r\n" + "Dear "
                                + intern.get().getFirstName() + ",\r\n" + "\r\n"
                                + "We appreciate your interest in the BISAG internship program and the effort you put into your application. After careful consideration, we regret to inform you that your application has not been successful on this occasion.\r\n"
                                + "\r\n"
                                + "Please know that the decision was a difficult one, and we had many qualified candidates. We want to thank you for your interest in joining our team and for taking the time to apply for the internship position.\r\n"
                                + "\r\n"
                                + "We encourage you to continue pursuing your goals, and we wish you the best in your future endeavors. If you have any feedback or questions about the decision, you may reach out to [Contact Person/Department].\r\n"
                                + "\r\n"
                                + "Thank you again for considering BISAG for your internship opportunity. We appreciate your understanding.\r\n"
                                + "\r\n" + "Best regards,\r\n" + "\r\n" + "Your Colleague,\r\n"
                                + "Internship Coordinator\r\n" + "BISAG INTERNSHIP PROGRAM\r\n" + "1231231231",
                        "BISAG INTERNSHIP RESULT");
            } else {
                emailService.sendSimpleEmail(intern.get().getEmail(), message + " your unique id is " + intern.get().getId(),
                        "BISAG INTERNSHIP RESULT");
            }
        }
        return "redirect:/bisag/admin/intern_application";
    }


    @PostMapping("/intern_application/update")
    public String internApplicationSubmission(@RequestParam long id, InternApplication internApplication, MultipartHttpServletRequest req) throws IllegalStateException, IOException, Exception {
        Optional<InternApplication> intern = internService.getInternApplication(id);

        if (internApplication.getIsActive()) {
            intern.get().setFirstName(internApplication.getFirstName());
            intern.get().setLastName(internApplication.getLastName());
            intern.get().setContactNo(internApplication.getContactNo());

            MyUser user = myUserService.getUserByUsername(intern.get().getEmail());
            user.setUsername(internApplication.getEmail());
            userRepo.save(user);

            intern.get().setEmail(internApplication.getEmail());
            intern.get().setCollegeName(internApplication.getCollegeName());
            intern.get().setIsActive(true);
            intern.get().setBranch(internApplication.getBranch());
            intern.get().setDomain(internApplication.getDomain());
            intern.get().setSemester(internApplication.getSemester());
            intern.get().setJoiningDate(internApplication.getJoiningDate());
            intern.get().setCompletionDate(internApplication.getCompletionDate());

            logService.saveLog(String.valueOf(id), "Updated intern application details", "InternApplication Update");
        } else {
            intern.get().setIsActive(false);
            Cancelled cancelledEntry = new Cancelled();
            cancelledEntry.setTableName("InternApplication");
            cancelledEntry.setCancelId(Long.toString(intern.get().getId()));
            cancelledRepo.save(cancelledEntry);

            logService.saveLog(String.valueOf(id), "Cancelled intern application", "InternApplication Cancellation");
        }

        intern.get().setUpdatedAt(LocalDateTime.now());
        internService.addInternApplication(intern.get());
        return "redirect:/bisag/admin/intern_application/" + id;
    }


    @PostMapping("/intern/update")
    public String updateIntern(@RequestParam String id, Intern internApplication, @RequestParam("groupId") String groupId, MultipartHttpServletRequest req) throws IllegalStateException, IOException, Exception {
        Optional<Intern> intern = internService.getIntern(id);

        if (groupId.equals("createOwnGroup")) {
            String generatedId = generateGroupId();
            GroupEntity group = new GroupEntity();
            group.setGroupId(generatedId);
            groupService.registerGroup(group);
            intern.get().setGroup(group);
        } else {
            intern.get().setGroup(groupService.getGroup(groupId));
        }

        if (intern.get().getIsActive()) {
            intern.get().setFirstName(internApplication.getFirstName());
            intern.get().setLastName(internApplication.getLastName());
            intern.get().setContactNo(internApplication.getContactNo());

            MyUser user = myUserService.getUserByUsername(intern.get().getEmail());
            user.setUsername(internApplication.getEmail());
            userRepo.save(user);

            InternApplication internA = internService.getInternApplicationByUsername(intern.get().getEmail());
            internA.setEmail(internApplication.getEmail());
            internApplicationRepo.save(internA);

            intern.get().setEmail(internApplication.getEmail());
            intern.get().setCollegeName(internApplication.getCollegeName());
            intern.get().setBranch(internApplication.getBranch());
            intern.get().setIsActive(true);
            intern.get().setDomain(internApplication.getDomain());
            intern.get().setSemester(internApplication.getSemester());
            intern.get().setJoiningDate(internApplication.getJoiningDate());
            intern.get().setCompletionDate(internApplication.getCompletionDate());
            intern.get().setPermanentAddress(internApplication.getPermanentAddress());
            intern.get().setDateOfBirth(internApplication.getDateOfBirth());
            intern.get().setGender(internApplication.getGender());
            intern.get().setCollegeGuideHodName(internApplication.getCollegeGuideHodName());
            intern.get().setDegree(internApplication.getDegree());
            intern.get().setAggregatePercentage(internApplication.getAggregatePercentage());
            intern.get().setUsedResource(internApplication.getUsedResource());

            logService.saveLog(id, "Updated intern details", "Intern Update");
        }

        if (!internApplication.getIsActive()) {
            intern.get().setIsActive(false);
            intern.get().setCancellationStatus("cancelled");
            Cancelled cancelledEntry = new Cancelled();
            cancelledEntry.setTableName("intern");
            cancelledEntry.setCancelId(intern.get().getInternId());
            cancelledRepo.save(cancelledEntry);

            logService.saveLog(id, "Cancelled intern", "Intern Cancellation");
        }

        intern.get().setUpdatedAt(LocalDateTime.now());
        internRepo.save(intern.get());
        return "redirect:/bisag/admin/intern/" + id;
    }


    @GetMapping("/intern_application/approved_interns")
    public ModelAndView approvedInterns(Model model) {
        ModelAndView mv = new ModelAndView();
        List<InternApplication> intern = internService.getApprovedInterns();
        model = countNotifications(model);

        mv.addObject("interns", intern);
        mv.setViewName("admin/approved_interns");
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");

        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {

            session.setAttribute("id", admin.getAdminId());

            logService.saveLog(String.valueOf(admin.getAdminId()),
                    "View Shortlisted Intern Applications",
                    "Admin " + admin.getName() + " accessed the shortlisted intern applications page.");
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return mv;
    }

    @GetMapping("/intern_application/approved_intern/{id}")
    public ModelAndView approvedInterns(@PathVariable("id") long id, Model model) {
        System.out.println("approved id: " + id);
        ModelAndView mv = new ModelAndView();

        Optional<InternApplication> intern = internService.getInternApplication(id);
        mv.addObject("intern", intern);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
        List<Branch> branches = fieldService.getBranches();

        model = countNotifications(model);
        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
        mv.addObject("branches", branches);
        mv.setViewName("admin/approved_intern_application_detail");

        String username = (String) session.getAttribute("username");

        Admin admin = adminService.getAdminByUsername(username);

        if (admin != null) {

            session.setAttribute("id", admin.getAdminId());

            // Log the action
            logService.saveLog(String.valueOf(admin.getAdminId()),
                    "View Shortlisted Intern Application Details",
                    "Admin " + admin.getName() + " accessed the details of shortlisted intern with ID: " + id);
        } else {
            System.out.println("Error: Admin not found for logging!");
        }

        return mv;
    }

//    // Method to reject an intern and log the rejection
//    @PutMapping("/rejectIntern/{internId}")
//    public String rejectIntern(@PathVariable Long internId) {
//        // Call the service method through the injected service instance (not statically)
//        internApplicationService.updateStatusToRejected(internId);
//
//        // Alternatively, call the log service directly if needed
//        // logService.logRejectedIntern(internApplication);
//
//        return "Intern has been rejected and logged successfully.";
//    }

    @PostMapping("/intern_application/approved_intern/update")
    public String approvedInterns(@RequestParam long id, InternApplication internApplication, MultipartHttpServletRequest req)
            throws IllegalStateException, IOException, Exception {
        Optional<InternApplication> intern = internService.getInternApplication(id);

        if (internApplication.getIsActive()) {
            intern.get().setFirstName(internApplication.getFirstName());
            intern.get().setLastName(internApplication.getLastName());
            intern.get().setContactNo(internApplication.getContactNo());
            intern.get().setEmail(internApplication.getEmail());
            intern.get().setIsActive(true);
            intern.get().setCollegeName(internApplication.getCollegeName());
            intern.get().setBranch(internApplication.getBranch());
            intern.get().setDomain(internApplication.getDomain());
            intern.get().setSemester(internApplication.getSemester());
            intern.get().setJoiningDate(internApplication.getJoiningDate());
            intern.get().setCompletionDate(internApplication.getCompletionDate());
        } else {
            intern.get().setIsActive(false);
            Cancelled cancelledEntry = new Cancelled();
            cancelledEntry.setTableName("InternApplication");
            cancelledEntry.setCancelId(Long.toString(intern.get().getId()));
            cancelledRepo.save(cancelledEntry);
        }

        internService.addInternApplication(intern.get());

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Update Approved Intern",
                    "Admin " + admin.getName() + " updated details of approved intern with ID: " + id);
        }

        return "redirect:/bisag/admin/intern_application/approved_interns";
    }

    @PostMapping("/intern_application/approved_intern/ans")
    public String approvedInterns(@RequestParam String message, @RequestParam long id, @RequestParam String finalStatus) {
        System.out.println("id: " + id + ", finalStatus: " + finalStatus);

        Optional<InternApplication> intern = internService.getInternApplication(id);
        intern.get().setFinalStatus(finalStatus);
        internService.addInternApplication(intern.get());

        if (finalStatus.equals("failed")) {
            emailService.sendSimpleEmail(intern.get().getEmail(), "You are Failed", "BISAG INTERNSHIP RESULT");
        } else {
            String finalMessage = message + "\n" + "Username: " + intern.get().getFirstName() +
                    intern.get().getLastName() + "\n Password: " + intern.get().getFirstName() + "_" + intern.get().getId();
            emailService.sendSimpleEmail(intern.get().getEmail(), finalMessage, "BISAG INTERNSHIP RESULT");
        }

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Final Status",
                    "Admin with ID: " + admin.getAdminId() + " updated final status of intern with ID: " + id + " to " + finalStatus);
        }

        return "redirect:/bisag/admin/intern_application/approved_interns";
    }

    @GetMapping("/intern_application/new_interns")
    public ModelAndView newInterns(Model model) {
        ModelAndView mv = new ModelAndView();
        List<Intern> intern = internService.getInterns();
        mv.addObject("intern", intern);
        model = countNotifications(model);
        mv.setViewName("admin/new_interns");
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed New Interns",
                    "Admin " + admin.getName() + " accessed the new interns page.");
        }

        return mv;
    }

    // Group Creation//////////////////////////////////////////

    @GetMapping("/create_group")
    public ModelAndView groupCreation(Model model) {
        ModelAndView mv = new ModelAndView();
        List<InternApplication> intern = internService.getInternApplication();
        mv.addObject("interns", intern);
        mv.addObject("admin", adminName(session));
        model = countNotifications(model);
        mv.setViewName("admin/create_group");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Group Creation",
                    "Admin " + admin.getName() + " accessed the group creation page.");
        }

        return mv;
    }

    @PostMapping("/create_group_details")
    public String createGroup(@RequestParam("selectedInterns") List<Long> selectedInterns) {

        System.out.println("Selected Intern IDs: " + selectedInterns);
        // generate group id
        String id = generateGroupId();
        GroupEntity group = new GroupEntity();
        Optional<InternApplication> internoptional = internService.getInternApplication(selectedInterns.get(0));
        group.setDomain(internoptional.get().getDomain());
        group.setGroupId(id);
        groupService.registerGroup(group);

        for (Long internId : selectedInterns) {
            Optional<InternApplication> internApplicationOptional = internService.getInternApplication(internId);

            if (internApplicationOptional.isPresent()) {
                InternApplication internApplication = internApplicationOptional.get();
                internApplication.setGroupCreated(true);
                internService.addInternApplication(internApplication);

                Intern intern = new Intern(internApplication.getFirstName(), internApplication.getLastName(),
                        internApplication.getContactNo(), internApplication.getEmail(),
                        internApplication.getCollegeName(), internApplication.getJoiningDate(),
                        internApplication.getCompletionDate(), internApplication.getBranch(), internApplication.getDegree(),
                        internApplication.getPassword(), internApplication.getCollegeIcardImage(),
                        internApplication.getNocPdf(), internApplication.getResumePdf(), internApplication.getPassportSizeImage(),
                        internApplication.getSemester(), internApplication.getDomain(), group);

                intern.setInternId(generateInternId());
                internService.addIntern(intern);

                // Log action for each intern registration
                String username = (String) session.getAttribute("username");
                Admin admin = adminService.getAdminByUsername(username);
                if (admin != null) {
                    logService.saveLog(String.valueOf(admin.getAdminId()), "Created Group and Registered Intern",
                            "Admin " + admin.getName() + " created a group and registered intern "
                                    + internApplication.getFirstName() + " " + internApplication.getLastName());
                }
            }
        }
        return "redirect:/bisag/admin/create_group";
    }

    // Add dynamic fields(college, branch)

    @GetMapping("/add_fields")
    public ModelAndView addFields(Model model) {
        ModelAndView mv = new ModelAndView();
        List<College> colleges = fieldService.getColleges();
        List<Branch> branches = fieldService.getBranches();
        List<Domain> domains = fieldService.getDomains();
        List<Degree> degrees = fieldService.getDegrees();
        model = countNotifications(model);
        mv.addObject("colleges", colleges);
        mv.addObject("branches", branches);
        mv.addObject("domains", domains);
        mv.addObject("degrees", degrees);
        mv.addObject("admin", adminName(session));
        mv.setViewName("admin/add_fields");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Add Fields",
                    "Admin " + admin.getName() + " accessed the add fields page.");
        }

        return mv;
    }

    @PostMapping("/add_college")
    public String addCollege(College college, Model model) {
        fieldService.addCollege(college);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added College",
                    "Admin " + admin.getName() + " added a new college: " + college.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/add_domain")
    public String addDomain(Domain domain, Model model) {
        fieldService.addDomain(domain);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added Domain",
                    "Admin " + admin.getName() + " added a new domain: " + domain.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/add_branch")
    public String addBranch(Branch branch, Model model) {
        fieldService.addBranch(branch);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added Branch",
                    "Admin " + admin.getName() + " added a new branch: " + branch.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/add_degree")
    public String addDegree(Degree degree, Model model) {
        fieldService.addDegree(degree);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Added Degree",
                    "Admin " + admin.getName() + " added a new degree: " + degree.getName());
        }

        return "redirect:/bisag/admin/add_fields";
    }

    // Guide Allocation///////////////////////////
    @GetMapping("/manage_group")
    public ModelAndView allocateGuide(Model model) {
        ModelAndView mv = new ModelAndView("admin/manage_group");
        List<GroupEntity> group = groupService.getGuideNotAllocatedGroup();
        model = countNotifications(model);
        mv.addObject("groups", group);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @GetMapping("/delete_college/{id}")
    public String deleteCollege(@PathVariable("id") long id) {
        fieldService.deleteCollege(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @GetMapping("/delete_degree/{id}")
    public String deleteDegree(@PathVariable("id") long id) {
        fieldService.deleteDegree(id);
        return "redirect:/bisag/admin/add_fields";
    }


    @GetMapping("/delete_branch/{id}")
    public String deleteBranch(@PathVariable("id") long id, Model model) {
        fieldService.deleteBranch(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @GetMapping("/delete_domain/{id}")
    public String deleteDomain(@PathVariable("id") long id, Model model) {
        fieldService.deleteDomain(id);
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_college/{id}")
    public String updateCollege(@ModelAttribute("college") College college, @PathVariable("id") long id) {
        Optional<College> existingCollege = fieldService.getCollege(id);
        System.out.println("In college update section");
        if (existingCollege.isPresent()) {
            College updatedCollege = existingCollege.get();
            updatedCollege.setName(college.getName());
            updatedCollege.setLocation(college.getLocation());
            fieldService.updateCollege(updatedCollege);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated College",
                        "Admin " + admin.getName() + " updated college: " + updatedCollege.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_branch/{id}")
    public String updateBranch(@ModelAttribute("branch") Branch branch, @PathVariable("id") long id) {
        Optional<Branch> existingBranch = fieldService.getBranch(id);

        if (existingBranch.isPresent()) {

            Branch updatedBranch = existingBranch.get();
            updatedBranch.setName(branch.getName());

            fieldService.updateBranch(updatedBranch);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Branch",
                        "Admin " + admin.getName() + " updated branch: " + updatedBranch.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_degree/{id}")
    public String updateDegree(@ModelAttribute("degree") Degree degree, @PathVariable("id") long id) {
        Optional<Degree> existingDegree = fieldService.getDegree(id);

        if (existingDegree.isPresent()) {
            Degree updatedDegree = existingDegree.get();
            updatedDegree.setName(degree.getName());
            fieldService.updateDegree(updatedDegree);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Degree",
                        "Admin " + admin.getName() + " updated degree: " + updatedDegree.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }

    @PostMapping("/update_domain/{id}")
    public String updateDomain(@ModelAttribute("domain") Domain domain, @PathVariable("id") long id) {
        Optional<Domain> existingDomain = fieldService.getDomain(id);

        if (existingDomain.isPresent()) {
            Domain updatedDomain = existingDomain.get();
            updatedDomain.setName(domain.getName());
            fieldService.updateDomain(updatedDomain);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Domain",
                        "Admin " + admin.getName() + " updated domain: " + updatedDomain.getName());
            }
        }
        return "redirect:/bisag/admin/add_fields";
    }


    // ----------------------------------- Guide registration
    // ---------------------------------------//

    @GetMapping("/register_guide")
    public ModelAndView registerGuide(Model model) {
        ModelAndView mv = new ModelAndView("admin/guide_registration");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Visited Guide Registration Page",
                    "Admin " + admin.getName() + " visited the guide registration page.");
        }

        model = countNotifications(model);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @PostMapping("/register_guide")
    public String registerGuide(@ModelAttribute("guide") Guide guide) {
        guideService.registerGuide(guide);

        // Send notification email to the guide
        emailService.sendSimpleEmail(guide.getEmailId(), "Notification: Appointment as Administrator\r\n" + "\r\n"
                        + "Dear " + guide.getName() + "\r\n" + "\r\n"
                        + "I trust this email finds you well. We are pleased to inform you that you have been appointed as an administrator within our organization, effective immediately. Your dedication and contributions to the team have not gone unnoticed, and we believe that your new role will bring value to our operations.\r\n"
                        + "\r\n"
                        + "As an administrator, you now hold a position of responsibility within the organization. We trust that you will approach your duties with diligence, professionalism, and a commitment to upholding the values of our organization.\r\n"
                        + "\r\n"
                        + "It is imperative to recognize the importance of your role and the impact it may have on the functioning of our team. We have confidence in your ability to handle the responsibilities that come with this position and to contribute positively to the continued success of our organization.\r\n"
                        + "\r\n"
                        + "We would like to emphasize the importance of maintaining the highest standards of integrity and ethics in your role. It is expected that you will use your administrative privileges responsibly and refrain from any misuse.\r\n"
                        + "\r\n"
                        + "Should you have any questions or require further clarification regarding your new responsibilities, please do not hesitate to reach out to [Contact Person/Department].\r\n"
                        + "\r\n"
                        + "Once again, congratulations on your appointment as an administrator. We look forward to your continued contributions and success in this elevated role.\r\n"
                        + "\r\n" + "Best regards,\r\n" + "\r\n" + "Your Colleague,\r\n" + "Administrator\r\n" + "1231231231",
                "BISAG ADMINISTRATIVE OFFICE");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Registered Guide",
                    "Admin " + admin.getName() + " registered guide " + guide.getName() + " as an administrator.");
        }

        return "redirect:/bisag/admin/guide_list";
    }


    @GetMapping("/guide_list")
    public ModelAndView guideList(Model model) {
        ModelAndView mv = new ModelAndView("admin/guide_list");
        List<Guide> guides = guideService.getGuide();
        model = countNotifications(model);
        mv.addObject("guides", guides);
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Guide List",
                    "Admin " + admin.getName() + " viewed the guide list.");
        }

        return mv;
    }

    @GetMapping("/guide_list/{id}")
    public ModelAndView guideList(@PathVariable("id") long id, Model model) {
        System.out.println("id" + id);
        ModelAndView mv = new ModelAndView();
        Optional<Guide> guide = guideService.getGuide(id);
        model = countNotifications(model);
        mv.addObject("guide", guide);
        mv.setViewName("admin/guide_list_detail");

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Guide Details",
                    "Admin " + admin.getName() + " viewed details of guide with ID: " + id);
        }

        return mv;
    }

    @GetMapping("/update_guide/{id}")
    public ModelAndView updateGuide(@PathVariable("id") long id, Model model) {
        ModelAndView mv = new ModelAndView("admin/update_guide");
        Optional<Guide> guide = guideService.getGuide(id);
        model = countNotifications(model);
        mv.addObject("guide", guide.orElse(new Guide()));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Guide Update Page",
                    "Admin " + admin.getName() + " accessed the update page for guide with ID: " + id);
        }

        return mv;
    }

    @PostMapping("/update_guide/{id}")
    public String updateGuide(@ModelAttribute("guide") Guide guide, @PathVariable("id") long id) {
        Optional<Guide> existingGuide = guideService.getGuide(guide.getGuideId());

        if (existingGuide.isPresent()) {

            String currentPassword = existingGuide.get().getPassword();
            Guide updatedGuide = existingGuide.get();
            updatedGuide.setName(guide.getName());
            updatedGuide.setLocation(guide.getLocation());
            updatedGuide.setFloor(guide.getFloor());
            updatedGuide.setLabNo(guide.getLabNo());
            updatedGuide.setContactNo(guide.getContactNo());
            updatedGuide.setEmailId(guide.getEmailId());
            if (!currentPassword.equals(encodePassword(guide.getPassword())) && guide.getPassword() != "") {
                updatedGuide.setPassword(encodePassword(guide.getPassword()));
            }
            guideService.updateGuide(updatedGuide, existingGuide);

            String username = (String) session.getAttribute("username");
            Admin admin = adminService.getAdminByUsername(username);
            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Updated Guide Information",
                        "Admin " + admin.getName() + " updated the guide information with ID: " + id);
            }
        }
        return "redirect:/bisag/admin/guide_list";
    }

    // Delete Guide
    @PostMapping("/guide_list/delete/{id}")
    public String deleteGuide(@PathVariable("id") long id) {
        guideService.deleteGuide(id);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Deleted Guide",
                    "Admin " + admin.getName() + " deleted the guide with ID: " + id);
        }

        return "redirect:/bisag/admin/guide_list";
    }

    // Manage group
    @GetMapping("/allocate_guide")
    public ModelAndView manageGroup(Model model) {
        ModelAndView mv = new ModelAndView();
        List<GroupEntity> allocatedGroups = groupService.getAllocatedGroups();
        List<GroupEntity> notAllocatedGroups = groupService.getNotAllocatedGroups();
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        model = countNotifications(model);
        mv.setViewName("/admin/allocate_guide");
        mv.addObject("alloactedGroups", allocatedGroups);
        mv.addObject("notAllocatedGroups", notAllocatedGroups);
        mv.addObject("guides", guides);
        mv.addObject("interns", interns);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    // Manage group details
    @GetMapping("/allocate_guide/{id}")
    public ModelAndView manageGroup(@PathVariable("id") String id, Model model) {
        ModelAndView mv = new ModelAndView();
        GroupEntity group = groupService.getGroup(id);
        List<Intern> interns = internService.getInterns();
        List<Guide> guides = guideService.getGuide();
        mv.setViewName("/admin/manage_group_detail");
        model = countNotifications(model);
        mv.addObject("groups", group);
        mv.addObject("guides", guides);
        mv.addObject("interns", interns);
        return mv;
    }

    @PostMapping("/allocate_guide/assign_guide")
    public String assignGuide(@RequestParam("guideid") long guideid, @RequestParam("groupId") String groupId) {
        System.out.println("guide id: " + guideid);
        groupService.assignGuide(groupId, guideid);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Assigned Guide to Group",
                    "Admin " + admin.getName() + " assigned guide with ID: " + guideid + " to group with ID: " + groupId);
        }

        return "redirect:/bisag/admin/allocate_guide";
    }

    // Project Definition Approvals
    @GetMapping("/admin_pending_def_approvals")
    public ModelAndView pendingFromGuide(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_pending_def_approvals");
        List<GroupEntity> groups = groupService.getAPendingGroups();
        model = countNotifications(model);

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Pending Project Definitions",
                    "Admin " + admin.getName() + " accessed the pending project definition approvals page.");
        }

        mv.addObject("groups", groups);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @PostMapping("/admin_pending_def_approvals/{groupId}")
    public String pendingFromAdmin(@RequestParam("apendingAns") String apendingAns,
                                   @PathVariable("groupId") String groupId,
                                   @RequestParam("projectDefinition") String projectDefinition,
                                   @RequestParam("description") String description) {

        GroupEntity group = groupService.getGroup(groupId);
        if (group != null) {
            group.setProjectDefinition(projectDefinition);
            group.setDescription(description);

            if (apendingAns.equals("approve")) {
                group.setProjectDefinitionStatus("approved");
                List<Intern> interns = internService.getInternsByGroupId(group.getId());
                for (Intern intern : interns) {
                    intern.setProjectDefinitionName(group.getProjectDefinition());
                    internRepo.save(intern);
                }

                String username = (String) session.getAttribute("username");
                Admin admin = adminService.getAdminByUsername(username);
                if (admin != null) {
                    logService.saveLog(String.valueOf(admin.getAdminId()), "Project Definition Approved",
                            "Admin " + admin.getName() + " approved project definition for group with ID: " + groupId);
                }

            } else {
                group.setProjectDefinitionStatus("pending");
            }
            groupRepo.save(group);
        } else {

        }
        return "redirect:/bisag/admin/admin_pending_def_approvals";
    }

    @GetMapping("admin_weekly_report")
    public ModelAndView weeklyReport(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report");
        List<GroupEntity> groups = groupService.getAllocatedGroups();
        List<WeeklyReport> reports = weeklyReportService.getAllReports();
        groups.sort(Comparator.comparing(GroupEntity::getGroupId));
        model = countNotifications(model);
        mv.addObject("groups", groups);
        mv.addObject("reports", reports);
        mv.addObject("admin", adminName(session));

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Weekly Reports",
                    "Admin " + admin.getName() + " accessed the weekly reports page.");
        }

        return mv;
    }

    @GetMapping("/admin_weekly_report_details/{groupId}/{weekNo}")
    public ModelAndView changeWeeklyReportSubmission(@PathVariable("groupId") String groupId, @PathVariable("weekNo") int weekNo, Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report_details");
        model = countNotifications(model);
        Admin admin = getSignedInAdmin();
        GroupEntity group = groupService.getGroup(groupId);
        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
        MyUser user = myUserService.getUserByUsername(admin.getEmailId());

        if (user.getRole().equals("ADMIN")) {
            String name = admin.getName();
            mv.addObject("replacedBy", name);

            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Weekly Report Details",
                    "Admin " + admin.getName() + " accessed weekly report details for group " + groupId + " and week " + weekNo);
        } else if (user.getRole().equals("INTERN")) {
            Intern intern = internService.getInternByUsername(user.getUsername());
            mv.addObject("replacedBy", intern.getFirstName() + intern.getLastName());
        } else {

        }

        mv.addObject("report", report);
        mv.addObject("group", group);

        return mv;
    }

    @GetMapping("/admin_weekly_report_form")
    public ModelAndView showWeeklyReportForm(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report_form");

        List<GroupEntity> groups = groupService.getAllocatedGroups();
        List<Intern> interns = internService.getAllInterns();

        model.addAttribute("groups", groups);
        model.addAttribute("interns", interns);
        model = countNotifications(model);

        mv.addObject("admin", adminName(session));
        mv.addObject("groups", groups);

        // Fetch Admin Details
        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Weekly Report Form",
                    "Admin " + admin.getName() + " accessed the weekly report submission form.");
        }

        return mv;
    }

    @PostMapping("/admin_weekly_report_form")
    public String submitWeeklyReport(@RequestParam("groupId") Long groupId,
                                     @RequestParam("internId") Intern internId,
                                     @RequestParam("guide") Guide guide,
                                     @RequestParam("weekNo") int weekNo,
                                     @RequestParam("deadline") String deadlineString,
                                     @RequestParam("status") String status,
                                     @RequestParam("submittedPdf") MultipartFile submittedPdf) {

        // Convert String to Date
        Date deadline = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            deadline = dateFormat.parse(deadlineString);
        } catch (ParseException e) {
            e.printStackTrace(); // Log error
            return "redirect:/bisag/admin/admin_weekly_report_form?error";
        }

        String username = (String) session.getAttribute("username");
        Admin admin = adminService.getAdminByUsername(username);

        weeklyReportService.submitAdminWeeklyReport(groupId, internId, guide, weekNo, deadline, status, submittedPdf);

        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Submitted Weekly Report",
                    "Admin " + admin.getName() + " submitted a weekly report for Group ID: " + groupId + ", Week No: " + weekNo);
        }

        return "redirect:/bisag/admin/admin_weekly_report_form?success";
    }


//    @GetMapping("/admin_weekly_report_details/{groupId}/{weekNo}")
//    public ModelAndView changeWeeklyReportSubmission(
//            @PathVariable("groupId") String groupId,
//            @PathVariable("weekNo") int weekNo,
//            Model model) {
//        ModelAndView mv = new ModelAndView("/admin/admin_weekly_report_details");
//        model = countNotifications(model);
//
//        Admin admin = getSignedInAdmin();
//        GroupEntity group = groupService.getGroup(groupId);
//
//        if (group != null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found" + groupId);
//        }
//
//        WeeklyReport report = weeklyReportService.getReportByWeekNoAndGroupId(weekNo, group);
//
//        if (report != null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Weekly Report not found for group:" + groupId);
//        }
//
//        MyUser user = myUserService.getUserByUsername(admin.getEmailId());
//        if (user.getRole().equals("ADMIN")) {
//            String name = admin.getName();
//            mv.addObject("replacedBy", name);
//        } else if (user.getRole().equals("INTERN")) {
//            Intern intern = internService.getInternByUsername(user.getUsername());
//            mv.addObject("replacedBy", intern.getFirstName() + intern.getLastName());
//        }
//
//        mv.addObject("report", report);
//        mv.addObject("group", group);
//
//        return mv;
//    }
@GetMapping("/admin_yearly_report")
public String getReportsByYear(@RequestParam(value = "date", required = true) String selectedDate, Model model) {
    int year = 0;
    List<WeeklyReport> reports = null;

    if (selectedDate != null && !selectedDate.isEmpty()) {
        LocalDate date = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_DATE);
        year = date.getYear();
        reports = weeklyReportService.getReportsByYear(year);

        if (reports == null || reports.isEmpty()) {
            model.addAttribute("message", "Report not found for the year " + year);
        }
    } else {
        reports = weeklyReportService.getAllReports();
    }

    model.addAttribute("selectedDate", selectedDate);
    model.addAttribute("year", year);
    model.addAttribute("reports", reports);

    String username = (String) session.getAttribute("username");
    Admin admin = adminService.getAdminByUsername(username);

    if (admin != null) {
        logService.saveLog(String.valueOf(admin.getAdminId()), "Accessed Yearly Reports",
                "Admin " + admin.getName() + " accessed the yearly reports page for the year " + year);
    }

    return "admin/admin_yearly_report";
}
@GetMapping("/cancellation_requests")
public ModelAndView cancellationRequests(Model model) {
    ModelAndView mv = new ModelAndView("/admin/cancellation_requests");
    List<Intern> requestedInterns = internService.getInternsByCancellationStatus("requested");
    model = countNotifications(model);

    Admin admin = getSignedInAdmin();
    logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Cancellation Requests",
            "Admin " + admin.getName() + " viewed cancellation requests.");

    mv.addObject("requestedInterns", requestedInterns);
    mv.addObject("admin", adminName(session));

    return mv;
}

    @PostMapping("/cancellation_requests/ans")
    public String pendingCancellationsFromAdmin(@RequestParam("cancelAns") String cancelAns,
                                                @RequestParam("internId") String internId) {

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Responded to Cancellation Request",
                "Admin " + admin.getName() + " responded to cancellation request for intern ID " + internId + " with answer: " + cancelAns);

        adminService.cancelIntern(cancelAns, internId);

        return "redirect:/bisag/admin/cancellation_requests";
    }

    @GetMapping("/query_to_guide")
    public ModelAndView queryToGuide(Model model) {
        ModelAndView mv = new ModelAndView("/admin/query_to_guide");
        List<Admin> admins = adminService.getAdmin();
        List<Guide> guides = guideService.getGuide();
        List<Intern> interns = internService.getInterns();
        List<GroupEntity> groups = groupService.getAllocatedGroups();
        model = countNotifications(model);

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Queried Guide",
                "Admin " + admin.getName() + " queried a guide.");

        mv.addObject("groups", groups);
        mv.addObject("interns", interns);
        mv.addObject("admins", admins);
        mv.addObject("guides", guides);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @GetMapping("/admin_pending_final_reports")
    public ModelAndView adminPendingFinalReports(Model model) {
        ModelAndView mv = new ModelAndView("/admin/admin_pending_final_reports");
        List<GroupEntity> groups = groupService.getAPendingFinalReports();
        model = countNotifications(model);

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Pending Final Reports",
                "Admin " + admin.getName() + " viewed pending final reports.");

        mv.addObject("groups", groups);
        mv.addObject("admin", adminName(session));
        return mv;
    }

    @PostMapping("/admin_pending_final_reports/ans")
    public String adminPendingFinalReports(@RequestParam("apendingAns") String apendingAns,
                                           @RequestParam("groupId") String groupId) {
        GroupEntity group = groupService.getGroup(groupId);

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Responded to Final Report",
                "Admin " + admin.getName() + " responded to final report for group ID " + groupId + " with answer: " + apendingAns);

        if (apendingAns.equals("approve")) {
            group.setFinalReportStatus("approved");
        } else {
            group.setFinalReportStatus("pending");
        }
        groupRepo.save(group);

        return "redirect:/bisag/admin/admin_pending_final_reports";
    }

    //-------------------------------- Leave Application Module------------------------------------
    @GetMapping("/manage_leave_applications")
    public ModelAndView manageLeaveApplications(Model model, HttpSession session) {
        ModelAndView mv = new ModelAndView("/admin/manage_leave_applications");
        List<Intern> interns = internService.getInterns();
        List<LeaveApplication> leaveApplications = leaveApplicationService.getAllLeaveApplications();
        model = countNotifications(model);

        // Fetch Admin Details
        Admin admin = getSignedInAdmin();

        // Log Access Action
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Leave Applications",
                    "Admin " + admin.getName() + " viewed all leave applications.");
        }

        mv.addObject("interns", interns);
        mv.addObject("leaveApplications", leaveApplications);
        mv.addObject("admin", adminName(session));

        return mv;
    }

    @GetMapping("/manage_leave_applications_details/{id}")
    public ModelAndView manageLeaveApplicationsDetails(@PathVariable("id") String id, Model model, HttpSession session) {
        ModelAndView mv = new ModelAndView("/admin/manage_leave_applications_details");
        List<Intern> interns = internService.getInterns();
        List<LeaveApplication> leaveApplications = leaveApplicationService.getLeaveApplicationsByInternId(id);
        model = countNotifications(model);

        // Fetch Admin Details
        Admin admin = getSignedInAdmin();

        // Log Access Action
        if (admin != null) {
            logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Leave Application Details",
                    "Admin " + admin.getName() + " viewed leave application details for intern ID " + id);
        }

        mv.addObject("interns", interns);
        mv.addObject("leaveApplications", leaveApplications);
        mv.addObject("admin", adminName(session));

        return mv;
    }

//    @PostMapping("/approve_leave/{id}")
//    public String approveLeave(@PathVariable("id") Long id) {
//        leaveApplicationService.approveLeave(id, "admin");
//        return "redirect:/admin/manage_leave_applications";
//    }
//
//    @PostMapping("/reject_leave/{id}")
//    public String rejectLeave(@PathVariable("id") Long id) {
//        leaveApplicationService.rejectLeave(id, "admin");
//        return "redirect:/admin/manage_leave_applications";
//    }

    @GetMapping("/generate_intern_report")
    public ModelAndView generateInternReport(Model model) {
        ModelAndView mv = new ModelAndView("admin/generate_intern_report");

        List<College> college = fieldService.getColleges();
        List<Branch> branch = fieldService.getBranches();
        List<Domain> domain = fieldService.getDomains();
        List<Guide> guide = guideService.getGuide();
        List<Degree> degree = fieldService.getDegrees();
        List<GroupEntity> groupEntities = groupService.getGroups();
        List<String> projectDefinitions = internService.getDistinctProjectDefinitions();
        List<Intern> interns = internService.getAllInterns();
        List<String> genders = internService.getDistinctGenders();

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Generate Intern Report Page",
                "Admin " + admin.getName() + " viewed the Generate Intern Report page.");

        model = countNotifications(model);
        mv.addObject("interns", interns);
        mv.addObject("project_definition_name", projectDefinitions);
        mv.addObject("colleges", college);
        mv.addObject("branches", branch);
        mv.addObject("domains", domain);
        mv.addObject("guides", guide);
        mv.addObject("degrees", degree);
        mv.addObject("genders", genders);
        mv.addObject("admin", adminName(session));

        return mv;
    }

    @PostMapping("/generate_intern_report")
    public String generateInternReport(HttpServletResponse response, @ModelAttribute("ReportFilter") ReportFilter reportFilter) throws Exception {
        College college;
        Branch branch;
        Optional<Guide> guide;
        Domain domain;
        Degree degree;

        // Log filter details before applying the filter
        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Applied Report Filters",
                "Admin " + admin.getName() + " applied the following filters: College = " + reportFilter.getCollege() + ", Branch = " + reportFilter.getBranch() +
                        ", Guide = " + reportFilter.getGuide() + ", Domain = " + reportFilter.getDomain() + ", Degree = " + reportFilter.getDegree() +
                        ", Date Range = " + reportFilter.getStartDate() + " to " + reportFilter.getEndDate());

        if (reportFilter.getBranch().equals("All")) {
            reportFilter.setBranch(null);
        } else {
            branch = fieldService.findByBranchName(reportFilter.getBranch());
        }

        if (reportFilter.getCollege().equals("All")) {
            reportFilter.setCollege(null);
        } else {
            college = fieldService.findByCollegeName(reportFilter.getCollege());
        }

        if (reportFilter.getGuide().equals("All")) {
            guide = null;
        } else {
            guide = guideService.getGuideByName(reportFilter.getGuide());
        }

        if (reportFilter.getDomain().equals("All")) {
            reportFilter.setDomain(null);
        } else {
            domain = fieldService.getDomainByName(reportFilter.getDomain());
        }

        if (reportFilter.getDegree().equals("All")) {
            reportFilter.setDegree(null);
        } else {
            degree = fieldService.findByDegreeName(reportFilter.getDegree());
        }

        List<Intern> filteredInterns = internService.getFilteredInterns(reportFilter.getCollege(),
                reportFilter.getBranch(), guide, reportFilter.getDomain(), reportFilter.getCancelled(),
                reportFilter.getStartDate(), reportFilter.getEndDate(), reportFilter.getCancelled());

        logService.saveLog(String.valueOf(admin.getAdminId()), "Filtered Interns for Report",
                "Admin " + admin.getName() + " filtered " + filteredInterns.size() + " interns based on the selected criteria.");

        for (Intern intern : filteredInterns) {
            System.out.println(intern.getFirstName());
        }

        // Exporting the report in the selected format (PDF or Excel)
        if (reportFilter.getFormat().equals("pdf")) {
            dataExportService.exportToPdf(filteredInterns, response);
            logService.saveLog(String.valueOf(admin.getAdminId()), "Exported Intern Report (PDF)",
                    "Admin " + admin.getName() + " exported the intern report in PDF format.");
        } else {
            dataExportService.exportToExcel(filteredInterns, response);
            logService.saveLog(String.valueOf(admin.getAdminId()), "Exported Intern Report (Excel)",
                    "Admin " + admin.getName() + " exported the intern report in Excel format.");
        }

        return "redirect:/bisag/admin/admin_dashboard";
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Admin admin = getSignedInAdmin();

        logService.saveLog(String.valueOf(admin.getAdminId()), "Password Change Request",
                "Admin " + admin.getName() + " requested a password change.");

        adminService.changePassword(admin, newPassword);

        logService.saveLog(String.valueOf(admin.getAdminId()), "Password Changed Successfully",
                "Admin " + admin.getName() + " successfully changed their password.");

        return "redirect:/logout";
    }

    //-------------------------- View all thesis records-----------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------------------------------
//    @GetMapping("/thesis")
//    public String viewThesisList(Model model) {
//        List<Thesis> thesisList = thesisService.getAllTheses();
//        model.addAttribute("theses", thesisList);
//        return "thesis_list";
//    }

    public AdminController(ThesisService thesisService) {
        this.thesisService = thesisService;
    }

    // Show form to add a new thesis
    @GetMapping("thesis/new")
    public String showThesisForm(Model model) {
        Admin admin = getSignedInAdmin();
        model.addAttribute("thesis", new Thesis());

        logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis Form Access", "Admin " + admin.getName() + " accessed the 'Add Thesis' form.");

        return "admin/thesis_form";
    }

    // Handle adding/updating a thesis record
    @PostMapping("thesis/save")
    public String saveThesis(@ModelAttribute("thesis") Thesis thesis, RedirectAttributes redirectAttributes) {
        Admin admin = getSignedInAdmin();

        thesisService.saveThesis(thesis);
        redirectAttributes.addFlashAttribute("successMessage", "Thesis record submitted successfully!");
        logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis Saved",
                "Admin " + admin.getName() + " added or updated a thesis with ID: " + thesis.getId());

        return "redirect:/bisag/admin/thesis_list";
    }

    // Show all thesis records
    @GetMapping("/thesis_list")
    public String getThesisList(Model model) {
        List<Thesis> thesisList = thesisService.getAllTheses();
        model.addAttribute("thesisList", thesisList);
        Admin admin = getSignedInAdmin();

        logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis List View", "Admin " + admin.getName() + " viewed the list of thesis.");

        return "admin/thesis_list";
    }

//    @PostMapping("/update-return/{id}")
//    public ResponseEntity<Thesis> updateReturnDate(
//            @PathVariable Long id, @RequestBody Map<String, String> request) {
//        try {
//            System.out.println("Received update request for Thesis ID: " + id);
//
//            // Parse the date
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            Date utilDate = dateFormat.parse(request.get("actualReturnDate"));
//            java.sql.Date actualReturnDate = new java.sql.Date(utilDate.getTime());
//
//            String location = request.get("location");
//
//            Thesis updatedThesis = thesisService.updateReturnDate(id, actualReturnDate, location);
//            return ResponseEntity.ok(updatedThesis);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//        }
//    }
@GetMapping("/thesis/update/{id}")
public String showUpdatePage(@PathVariable Long id, Model model) {
    Thesis thesis = thesisService.getThesisById(id)
            .orElseThrow(() -> new RuntimeException("Thesis not found with id: " + id));
    model.addAttribute("thesis", thesis);
    return "admin/update_thesis";
}
    @PostMapping("/thesis/update/{id}")
    public String updateThesis(@PathVariable Long id,
                               @RequestParam String actualReturnDate,
                               @RequestParam String location) {
        java.sql.Date returnDate = java.sql.Date.valueOf(actualReturnDate);

        thesisService.updateThesisReturnDateAndLocation(id, returnDate, location);

        return "redirect:/bisag/admin/thesis_list";
    }
    //Show thesis ID wise-------------------
    @GetMapping("/thesis_list_detail/{id}")
    public String getThesisDetails(@PathVariable("id") String id, Model model) {
        Optional<Thesis> thesis = thesisService.getThesisById(Long.parseLong(id));
        if (thesis.isPresent()) {
            model.addAttribute("thesis", thesis);
            Admin admin = getSignedInAdmin();

            logService.saveLog(String.valueOf(admin.getAdminId()), "Thesis Details View",
                    "Admin " + admin.getName() + " viewed the details of thesis with ID: " + id);
            return "admin/thesis_list_detail";
        } else {
            return "error/404";
        }
    }
    // Show form to edit an existing thesis
//    @GetMapping("/thesis/edit/{id}")
//    public String editThesis(@PathVariable Long id, Model model) {
//        Thesis thesis = thesisService.getThesisById(id);
//        model.addAttribute("thesis", thesis);
//        return "thesis-form";
//    }

    // Delete a thesis record
//    @GetMapping("/thesis/delete/{id}")
//    public String deleteThesis(@PathVariable Long id) {
//        thesisService.deleteThesis(id);
//        return "redirect:/admin/thesis";
//    }

    //LOGS-----------------------------------------------------------------------------------------
    // Endpoint to view logs
//    @GetMapping("/logs")
//    public String viewLogs(Model model) {
//        List<Log> logs = logService.getAllLogs();
//        model.addAttribute("logs", logs);
//        return "admin/logs"; // This ensures Thymeleaf looks inside templates/admin/logs.html
//    }

    // If the service returns a List of InternApplication
//    @PostMapping("/rejectIntern/{id}")
//    public String rejectIntern(@PathVariable Long id) {
//        internApplicationService.updateStatusToRejected(id);
//        return "redirect:/admin/logs";
//    }

    // Show rejected interns in the logs
    @GetMapping("/logs")
    public String showRejectedInterns(Model model) {
        List<InternApplication> rejectedInterns = internApplicationService.getRejectedInterns();

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Rejected Interns", "Admin " + admin.getName() + " accessed the rejected interns list.");

        model.addAttribute("rejectedInterns", rejectedInterns);
        return "admin/logs";
    }

    // Get intern activity logs----------------------------------------------------------------
    @GetMapping("/activity_logs")
    public String getInternActivityLogs(Model model) {
        List<Log> logs = logService.getAllLogs();
        if (logs == null) {
            logs = new ArrayList<>();  // Ensure it's not null
        }
        System.out.println("Logs fetched: " + logs.size());

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logInternAction(id, "Viewed Activity Logs", "Admin " + admin.getName() + " accessed the intern activity logs.");

        model.addAttribute("logs", logs);
        return "admin/activity_logs";
    }

    public void logInternAction(String internId, String action, String details) {
        logService.saveLog(internId, action, details);
    }

    public void submitReport(String internId, String reportId) {

        logInternAction(internId, "Submitted Weekly Report", "Report ID: " + reportId);
    }

    public void updateProfile(String internId, String newEmail, String newPhone) {

        logInternAction(internId, "Updated Profile", "Changed Email: " + newEmail + ", Phone: " + newPhone);
    }

    // ========================== COMPANY VERIFICATION REQUESTS ========================== //
    //View all pending verification requests
    @GetMapping("/verification_requests")
    public ModelAndView viewVerificationRequests(Model model, HttpSession session) {
        ModelAndView mv = new ModelAndView("admin/verification_requests");

        // Fetch pending verification requests
        List<Verification> pendingRequests = verificationService.getPendingRequests();
        mv.addObject("requests", pendingRequests);

        model = countNotifications(model);

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Verification Requests",
                "Admin " + admin.getName() + " accessed the list of pending verification requests.");

        return mv;
    }

    // View details of a specific verification request
    @GetMapping("/verify/{id}")
    public String viewVerificationDetails(@PathVariable("id") long id, Model model) {

        Optional<Verification> verification = verificationService.getVerificationById(id);

        if (verification.isPresent()) {

            Admin admin = getSignedInAdmin();
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Verification Details View",
                    "Admin " + admin.getName() + " viewed the details of verification request with ID: " + id);

            model.addAttribute("verification", verification.get());

            return "admin/verification_detail";
        } else {
            model.addAttribute("errorMessage", "Verification request not found.");
            return "error/404";
        }
    }

    @PostMapping("/approve/{id}")
    public String approveVerification(@PathVariable("id") long id,
                                      @RequestParam(value = "remarks", required = false) String remarks,
                                      RedirectAttributes redirectAttributes) {
        Optional<Verification> verification = verificationService.getVerificationById(id);
        if (verification.isPresent()) {
            Verification v = verification.get();

            Admin admin = getSignedInAdmin();
            String adminId = String.valueOf(admin.getAdminId());

            verificationService.approveVerification(id, adminId, remarks);

            logService.saveLog(adminId, "Verification Approved",
                    "Admin " + admin.getName() + " approved the verification request with ID: " + id);

            redirectAttributes.addFlashAttribute("successMessage", "Verification request approved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification request not found.");
        }

        return "redirect:/bisag/admin/approved-verifications";
    }

    @PostMapping("/reject/{id}")
    public String rejectVerification(@PathVariable("id") long id,
                                     @RequestParam(value = "remarks", required = false) String remarks,
                                     RedirectAttributes redirectAttributes) {
        Optional<Verification> verification = verificationService.getVerificationById(id);
        if (verification.isPresent()) {
            Verification v = verification.get();

            Admin admin = getSignedInAdmin();
            String adminId = String.valueOf(admin.getAdminId());

            verificationService.rejectVerification(id, adminId, remarks);

            logService.saveLog(adminId, "Verification Rejected",
                    "Admin " + admin.getName() + " rejected the verification request with ID: " + id);

            redirectAttributes.addFlashAttribute("successMessage", "Verification request rejected successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification request not found.");
        }

        return "redirect:/bisag/admin/rejected-verifications";
    }

    //Form for companies to submit verification requests
    @GetMapping("/verification_request_form")
    public ModelAndView verificationRequestForm() {
        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Verification Request Form", "Admin " + admin.getName() + " accessed the verification request form.");

        return new ModelAndView("admin/verification_request_form");
    }

    //for verification module
    @GetMapping("/get-intern-details/{internId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getInternDetails(@PathVariable String internId) {
        Intern intern = internService.getInternById(internId);

        if (intern != null) {
            Map<String, String> internDetails = new HashMap<>();
            internDetails.put("internName", intern.getFirstName());
            internDetails.put("internContact", intern.getContactNo());

            Admin admin = getSignedInAdmin();

            if (admin != null) {
                logService.saveLog(String.valueOf(admin.getAdminId()), "Fetched Intern Details",
                        "Admin " + admin.getName() + " fetched details for Intern ID " + internId);
            }

            return ResponseEntity.ok(internDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    //Handle company verification request submission
    @PostMapping("/submit_verification_request")
    public ModelAndView submitVerificationRequest(
            @RequestParam String internId,
            @RequestParam String internName,
            @RequestParam String internContact,
            @RequestParam String companyName,
            @RequestParam String contact,
            @RequestParam String email) {

        Verification verification = new Verification();
        verification.setInternId(internId);
        verification.setInternName(internName);
        verification.setInternContact(internContact);
        verification.setCompanyName(companyName);
        verification.setContact(contact);
        verification.setEmail(email);

        verificationService.createVerificationRequest(verification);

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());
        logService.saveLog(id, "Submitted Verification Request",
                "Admin " + admin.getName() + " submitted a verification request for Intern ID: " + internId);

        // Redirect to the verification requests page
        ModelAndView mv = new ModelAndView("admin/verification_requests");
        mv.addObject("success", true);
        mv.addObject("requests", verificationService.getPendingRequests());
        return mv;
    }

    // Success page after verification request submission
    @GetMapping("/verification_success")
    public ModelAndView verificationSuccess() {
        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Verification Success Page",
                "Admin " + admin.getName() + " accessed the verification success page.");

        return new ModelAndView("admin/verification_success");
    }

    // Display Approved Verifications
    @GetMapping("/approved-verifications")
    public String showApprovedVerifications(Model model) {
        List<Verification> approvedVerifications = verificationService.getApprovedVerifications();
        model.addAttribute("verifications", approvedVerifications);

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Approved Verifications",
                "Admin " + admin.getName() + " viewed the list of approved verifications.");

        return "admin/approved_verifications";
    }

    // Display Rejected Verifications
    @GetMapping("/rejected-verifications")
    public String showRejectedVerifications(Model model) {
        List<Verification> rejectedVerifications = verificationService.getRejectedVerifications();
        model.addAttribute("verifications", rejectedVerifications);

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Viewed Rejected Verifications",
                "Admin " + admin.getName() + " viewed the list of rejected verifications.");

        return "admin/rejected_verifications";
    }

    //-------------------------------------Attendance Module-------------------------------------------
    @PostMapping("/upload_attendance")
    public String uploadAttendance(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/admin/attendance";
        }

        try {
            attendanceService.processAttendanceFile(file);

            Admin admin = getSignedInAdmin();

            if (admin != null) {
                String id = String.valueOf(admin.getAdminId());

                logService.saveLog(id, "Uploaded Attendance Data",
                        "Admin " + admin.getName() + " uploaded a new attendance file.");
            }

            redirectAttributes.addFlashAttribute("successMessage", "Attendance data uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload attendance data.");
        }

        return "redirect:/bisag/admin/attendance";
    }

    @GetMapping("/attendance")
    public String getAllAttendance(Model model) {
        List<Attendance> attendances = attendanceRepo.findAll();

        //  total attendance percentage for each intern
        Map<String, Float> internTotalAttendanceMap = new HashMap<>();
        for (Attendance attendance : attendances) {
            String internId = attendance.getInternId();
            if (!internTotalAttendanceMap.containsKey(internId)) {
                float totalAttendance = attendanceService.calculateTotalAttendance(internId);
                internTotalAttendanceMap.put(internId, totalAttendance);
            }
        }

        model.addAttribute("attendances", attendances);
        model.addAttribute("internTotalAttendanceMap", internTotalAttendanceMap);

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());
        logService.saveLog(id, "Viewed Attendance Data",
                "Admin " + admin.getName() + " accessed the attendance records.");

        return "admin/attendance";
    }

    // --------------------------------------Display all relieving records---------------------------------------------
    // Display the form for adding a relieving record
    @GetMapping("/relieving_records")
    public String viewRelievingRecords(Model model) {
        // ✅ Fetch all intern IDs from the Intern table
        List<Intern> interns = internService.getAllInterns();
        List<College> college = fieldService.getColleges();

        Optional<RRecord> record = recordService.getRecordById(1L);

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Relieving Records",
                "Admin " + admin.getName() + " viewed the Relieving Records page.");

        model.addAttribute("interns", interns);
        model.addAttribute("college", college);
        model.addAttribute("records", record);
        model.addAttribute("admin", adminName(session));

        return "admin/relieving_records";
    }

    // Handle relieving record submission
    @PostMapping("/submit_relieving_record")
    public String submitRelievingRecord(
            @RequestParam String internId,
            @RequestParam String FirstName,
            @RequestParam String groupId,
            @RequestParam String collegeName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate joiningDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate plannedDate,
            @RequestParam String password,
            @RequestParam String media,
            @RequestParam String status,
            @RequestParam String project,
            @RequestParam String thesis,
            @RequestParam String others,
            @RequestParam String books,
            @RequestParam String subscription,
            @RequestParam String accessRights,
            @RequestParam String pendrives,
            @RequestParam String unusedCd,
            @RequestParam String backupProject,
            @RequestParam String system,
            @RequestParam String identityCards,
            @RequestParam String stipend,
            @RequestParam String information,
            @RequestParam String weeklyReport,
            @RequestParam String attendance,
            RedirectAttributes redirectAttributes) {

        List<RRecord> existingRecords = recordService.findByInternId(internId);
        if (!existingRecords.isEmpty()) {  // 🔹 CORRECTED: Check if list is not empty
            redirectAttributes.addFlashAttribute("error", "Intern already relieved!");
            return "redirect:/bisag/admin/relieving_records_list";
        }

        java.sql.Date joiningDateConverted = java.sql.Date.valueOf(joiningDate);
        java.sql.Date plannedDateConverted = java.sql.Date.valueOf(plannedDate);

        RRecord record = new RRecord();
        record.setInternId(internId);
        record.setFirstName(FirstName);
        record.setGroupId(groupId);
        record.setCollegeName(collegeName);
        record.setJoiningDate(joiningDateConverted);
        record.setPlannedDate(plannedDateConverted);
        record.setPassword(password);
        record.setMedia(media);
        record.setStatus(status);
        record.setProject(project);
        record.setThesis(thesis);
        record.setOthers(others);
        record.setBooks(books);
        record.setSubscription(subscription);
        record.setAccessRights(accessRights);
        record.setPendrives(pendrives);
        record.setUnusedCd(unusedCd);
        record.setBackupProject(backupProject);
        record.setSystem(system);
        record.setIdentityCards(identityCards);
        record.setStipend(stipend);
        record.setInformation(information);
        record.setWeeklyReport(weeklyReport);
        record.setAttendance(attendance);

        recordService.saveRecord(record);

        Admin admin = getSignedInAdmin();
        String id = String.valueOf(admin.getAdminId());

        logService.saveLog(id, "Submitted Relieving Record",
                "Admin " + admin.getName() + " submitted a relieving record for Intern ID: " + internId);

        redirectAttributes.addFlashAttribute("success", "Relieving record submitted successfully!");
        return "redirect:/bisag/admin/relieving_records_list";
    }
    @GetMapping("/relieving_records_list")
    public String getAllRelievingRecords(Model model) {
        List<RRecord> records = recordService.getAllRecords();
        model.addAttribute("records", records);

        Admin admin = getSignedInAdmin();

        if (admin != null) {
            String id = String.valueOf(admin.getAdminId());

            // Log Action
            logService.saveLog(id, "Viewed Relieving Records",
                    "Admin " + admin.getName() + " accessed the relieving records list.");
        }

        return "admin/relieving_records_list";
    }
    //Show records ID wise-------------------
    @GetMapping("/relieving_records_detail/{id}")
    public String getRecordsDetails(@PathVariable("id") String id, Model model) {
        Optional<RRecord> record = recordService.getRecordById(Long.parseLong(id));
        if (record.isPresent()) {
            model.addAttribute("records", record);
            logService.saveLog(id, "Relieving Records Details View",
                    "Admin viewed the details of records with ID: " + id);
            return "admin/relieving_records_detail";
        } else {
            return "error/404";
        }
    }

    // Automcatically fetches the group id when the intern id is being selected
    @GetMapping("/getGroupId/{internId}")
    @ResponseBody
    public ResponseEntity<String> getGroupId(@PathVariable String internId) {
        Optional<Intern> intern = internService.findById(internId);

        if (intern.isPresent()) {
            return ResponseEntity.ok(String.valueOf(intern.get().getGroup().getGroupId()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group ID not found");
        }
    }

    @GetMapping("/getCollegeName/{internId}")
    @ResponseBody
    public ResponseEntity<String> getCollegeName(@PathVariable String internId) {
        Optional<Intern> intern = internService.findById(internId);

        if (intern.isPresent() && intern.get().getCollegeName() != null) {
            return ResponseEntity.ok(intern.get().getCollegeName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("College not found");
        }
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

//            if (intern.getGuide() != null) {
//                Long guide = intern.getGuide().getGuideId();  // Assuming getGuideId() returns Long
//                response.put("guide", (guide != null) ? String.valueOf(guide) : "N/A");
//            } else {
//                response.put("guide", "N/A");
//            }

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/getAllGuides")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getAllGuides() {
        List<Guide> guides = guideService.findAllGuides();  // Use the correct method
        List<Map<String, String>> guideList = new ArrayList<>();

        for (Guide guide : guides) {
            Map<String, String> guideMap = new HashMap<>();
            guideMap.put("id", String.valueOf(guide.getGuideId())); // Convert Long to String
            guideMap.put("name", guide.getName()); // Fetch guide name
            guideList.add(guideMap);
        }

        return ResponseEntity.ok(guideList);
    }

    // Fetch interns based on Group ID
    @GetMapping("/getInternsByGroup/{groupId}")
    public ResponseEntity<List<Intern>> getInternsByGroup(@PathVariable Long groupId) {
        List<Intern> interns = internService.getInternsByGroupId(groupId);
        return ResponseEntity.ok(interns);
    }

    //-------------------------------Leave application module-----------------------------------
    // Fetch pending leave applications for admin view
    @GetMapping("/pending_leaves")
    public String viewPendingLeaves(Model model, HttpSession session) {
        List<LeaveApplication> pendingLeaves = leaveApplicationRepo.findByStatus("Pending");

        // Get the role from the session (ensure it is set during login)
        String role = (String) session.getAttribute("role");

        model = countNotifications(model);

        Admin admin = getSignedInAdmin();
        logService.saveLog(String.valueOf(admin.getAdminId()), "Viewed Pending Leave Applications",
                "Admin " + admin.getName() + " viewed pending leave applications.");

        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("role", role); // Pass role to Thymeleaf

        return "admin/pending_leaves";
    }

    // Approve leave request
    @PostMapping("/approve_leave/{id}")
    public String approveLeave(@PathVariable Long id) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setAdminApproval(true); // Set approval for admin

            // If both admin and guide have approved, set status as "Approved"
            if (leave.isAdminApproval() && leave.isGuideApproval()) {
                leave.setStatus("Approved");
            }

            leaveApplicationRepo.save(leave);

            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Approved Leave Application",
                        "Admin " + admin.getName() + " approved leave application for intern ID: " + leave.getInternId());
            }
        }
        return "redirect:/bisag/admin/pending_leaves";
    }

    @PostMapping("/reject_leave/{id}")
    public String rejectLeave(@PathVariable Long id) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(id);
        if (optionalLeave.isPresent()) {
            LeaveApplication leave = optionalLeave.get();
            leave.setStatus("Rejected");
            leave.setAdminApproval(false);
            leave.setGuideApproval(false);
            leaveApplicationRepo.save(leave);

            Admin admin = getSignedInAdmin();
            if (admin != null) {
                String adminId = String.valueOf(admin.getAdminId());
                logService.saveLog(adminId, "Rejected Leave Application",
                        "Admin " + admin.getName() + " rejected leave application for intern ID: " + leave.getInternId());
            }
        }
        return "redirect:/bisag/admin/pending_leaves";
    }

    // View Leave History (Approved & Rejected)
    @GetMapping("/leave_history")
    public String viewLeaveHistory(Model model) {
        List<LeaveApplication> leaveHistory = leaveApplicationRepo.findByStatusIn(Arrays.asList("Approved", "Rejected"));
        model.addAttribute("leaveHistory", leaveHistory);

        Admin admin = getSignedInAdmin();
        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Leave History",
                    "Admin " + admin.getName() + " viewed the leave history.");
        }

        return "admin/leave_history";
    }

    // View Leave Details
    @GetMapping("/leave_details/{id}")
    public String viewLeaveDetails(@PathVariable Long id, Model model) {
        LeaveApplication leave = leaveApplicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Application Not Found"));
        model.addAttribute("leave", leave);

        Admin admin = getSignedInAdmin();
        if (admin != null) {
            String adminId = String.valueOf(admin.getAdminId());
            logService.saveLog(adminId, "Viewed Leave Details",
                    "Admin " + admin.getName() + " viewed details of leave application ID: " + id);
        }

        return "admin/leave_details";
    }

    // ========================= Undertaking Form Management ==========================

    // View Undertaking Forms Page
    @GetMapping("/undertaking")
    public String showUndertakingForm(Model model) {
        System.out.println("Admin Undertaking page accessed"); // Debugging
        List<Undertaking> forms = undertakingRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("forms", forms);
        return "admin/undertaking_form"; // Ensure the HTML file exists
    }

    // ✅ Corrected POST method to add undertaking form
    @PostMapping("/add_undertaking")
    public String addUndertaking(@RequestParam String rules) {
        Undertaking undertaking = new Undertaking();
        undertaking.setContent(rules);
        undertaking.setCreatedAt(LocalDateTime.now());
        undertakingRepo.save(undertaking);
        return "redirect:/bisag/admin/undertaking"; // Redirect after form submission
    }

    // ✅ Corrected method for updating the undertaking form
    @PostMapping("/update_undertaking/{id}")
    public String updateUndertaking(@PathVariable Long id, @RequestParam String content) {
        Undertaking undertaking = undertakingRepo.findById(id).orElseThrow(() -> new RuntimeException("Undertaking not found"));
        undertaking.setContent(content);
        undertakingRepo.save(undertaking);
        return "redirect:/bisag/admin/undertaking"; // Redirect to avoid form resubmission issues
    }
    // ✅ Fetch the latest Undertaking Form content for Interns
    @GetMapping("/undertaking-content")
    @ResponseBody
    public String getUndertakingContent() {
        String latestContent = undertakingRepo.findLatestUndertakingContent();

        if (latestContent == null || latestContent.isEmpty()) {
            return "No undertaking content available."; // Avoid empty response issues
        }
        return latestContent;
    }

    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_Thesis Storage Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    //_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
    private static final String STORAGE_PATH = "/Users/pateldeep/Desktop/Coding/Springboot_Intern_Management_System-master-main/src/main/resources/static/files/thesis-storage/";

    // ✅ Upload Thesis PDF
    @PostMapping("/upload-thesis")
    public String uploadThesis(@RequestParam("internId") String internId,
                               @RequestParam("thesisTitle") String thesisTitle,
                               @RequestParam("file") MultipartFile file) {
        try {
            // Ensure storage directory exists
            File directory = new File(STORAGE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = STORAGE_PATH + fileName;

            // Save file locally
            file.transferTo(new File(filePath));

            // Save metadata to database
            ThesisStorage thesis = new ThesisStorage();
            thesis.setInternId(internId);
            thesis.setThesisTitle(thesisTitle);
            thesis.setFilePath(filePath);
            thesis.setUploadDate(new Date());
            thesisStorageRepo.save(thesis);

            return "redirect:/bisag/admin/thesis-storage";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // ✅ Fetch List of Uploaded Theses
    @GetMapping("/thesis-storage")
    public String viewThesisStorage(Model model) {
        List<ThesisStorage> thesisList = thesisStorageRepo.findAll();
        model.addAttribute("theses", thesisList);
        return "admin/thesis_storage";
    }

    // ✅ Generate Shareable Link
    @GetMapping("/generate-thesis-link/{id}")
    @ResponseBody
    public String generateThesisLink(@PathVariable Long id) {
        ThesisStorage thesis = thesisStorageRepo.findById(id).orElse(null);
        if (thesis == null) {
            return "Invalid thesis ID";
        }
        return "localhost:8080/bisag/admin/view-thesis/" + id;
    }

    // ✅ Download/View Thesis PDF
    @GetMapping("/view-thesis/{id}")
    public ResponseEntity<Resource> viewThesis(@PathVariable Long id) {
        ThesisStorage thesis = thesisStorageRepo.findById(id).orElse(null);
        if (thesis == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(thesis.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
