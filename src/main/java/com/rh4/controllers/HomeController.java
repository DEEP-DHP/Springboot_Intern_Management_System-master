package com.rh4.controllers;

import com.rh4.repositories.*;
import com.rh4.services.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.rh4.entities.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Validated
public class HomeController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);
    private InternApplicationRepo internApplicationRepo;
    private EmailSenderService emailService;

    @Value("${app.storage.base-dir}")
    private String baseDir;

    @Autowired
    private InternService internService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private LogService logService;
    @Autowired
    private InternController internController;

    public HomeController(InternApplicationRepo internApplicationRepo, EmailSenderService emailService) {
        this.internApplicationRepo = internApplicationRepo;
        this.emailService = emailService;
    }

    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //////////////////////////////////////////////////////////

    @GetMapping("/message")
    public String msg() {
        return "msg";
    }
    //////////////////////////////////////////////////////////

    @GetMapping("/")
    public String index() {
        return "index";
    }

//    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        return "login";
    }


    @PostMapping("/login")
    public void login(Intern intern) {

    }

    @GetMapping("/bisag_internship")
    public ModelAndView bisag_internship() {
        ModelAndView mv = new ModelAndView("internapply");
        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        List<Degree> degrees = fieldService.getDegrees();
        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);
        mv.addObject("degrees", degrees);

        return mv;
    }

//    @PostMapping("/bisag_internship")
//    public String bisagInternship(
//            @RequestParam("firstName") String firstName,
////            @RequestParam("lastName") String lastName,
//            @RequestParam("contactNo") String contactNo,
//            @RequestParam("email") String email,
//            @RequestParam("collegeName") String collegeName,
////            @RequestParam("branch") String branch,
//            @RequestParam("passportSizeImage") MultipartFile passportSizeImage,
//            @RequestParam("icardImage") MultipartFile icardImage,
//            @RequestParam("nocPdf") MultipartFile nocPdf,
//            @RequestParam("resumePdf") MultipartFile resumePdf,
//            @RequestParam("semester") int semester,
//            @RequestParam("password") String password,
//            @RequestParam("degree") String degree,
//            @RequestParam("domain") String domain,
//            @RequestParam("joiningDate") Date joiningDate,
//            @RequestParam("completionDate") Date completionDate,
//            @RequestParam("securityPin") String securityPin,
//            HttpSession session) {
//
//        try {
//            // Check for existing email
////            if (internApplicationRepo.findByEmail(email).isPresent()) {
////                session.setAttribute("msg", "Error: Email already registered.");
////                return "redirect:/bisag_internship";
////            }
//
//            // File storage setup
//            String storageDir = baseDir + email + "/";
//            File directory = new File(storageDir);
//            if (!directory.exists()) directory.mkdirs();
//
//            // Save files
//            Files.write(Paths.get(storageDir + "passportSizeImage.jpg"), passportSizeImage.getBytes());
//            Files.write(Paths.get(storageDir + "collegeIcardImage.jpg"), icardImage.getBytes());
//            Files.write(Paths.get(storageDir + "nocPdf.pdf"), nocPdf.getBytes());
//            Files.write(Paths.get(storageDir + "resumePdf.pdf"), resumePdf.getBytes());
//
//            // Save InternApplication data
//            InternApplication internApplication = new InternApplication();
//            internApplication.setFirstName(firstName);
////            internApplication.setLastName(lastName);
//            internApplication.setContactNo(contactNo);
//            internApplication.setEmail(email);
//            internApplication.setCollegeName(collegeName);
    ////            internApplication.setBranch(branch);
//            internApplication.setSemester(semester);
//            internApplication.setPassword(password);
//            internApplication.setDegree(degree);
//            internApplication.setDomain(domain);
//            internApplication.setJoiningDate(joiningDate);
//            internApplication.setCompletionDate(completionDate);
//            internApplication.setPassportSizeImage(passportSizeImage.getBytes());
//            internApplication.setCollegeIcardImage(icardImage.getBytes());
//            internApplication.setNocPdf(nocPdf.getBytes());
//            internApplication.setResumePdf(resumePdf.getBytes());
//            internApplication.setSecurityPin(securityPin);
//
//            internApplicationRepo.save(internApplication);
//
//            // Save User credentials
//            MyUser user = new MyUser();
//            user.setUsername(email);
//            String encryptedPassword = passwordEncoder().encode(password);
//            user.setPassword(encryptedPassword);
//            user.setSecurityPin(securityPin);
//            user.setEnabled(true);
//            user.setUserId(Long.toString(internApplication.getId()));
//            user.setRole("UNDERPROCESSINTERN");
//            userRepo.save(user);
//
//            // Confirmation email
//            emailService.sendSimpleEmail(
//                    email,
//                    "Congratulations! Your BISAG Internship application was successful.",
//                    "BISAG Administrative Office"
//            );
//
//            session.setAttribute("msg", "Application submitted successfully. Wait for 24-48 hours for recruitment.");
//            return "redirect:/bisag_internship";
//
//        } catch (DataIntegrityViolationException e) {
//            // Detailed error message for debugging
//            Throwable rootCause = e.getRootCause();
//            session.setAttribute("msg", "Error: " + (rootCause != null ? rootCause.getMessage() : "Unknown data integrity violation."));
//            return "redirect:/bisag_internship";
//        } catch (Exception e) {
//            session.setAttribute("msg", "Error: " + e.getMessage());
//            return "redirect:/bisag_internship";
//        }
//    }


//    @PostMapping("/remove-session-msg")
//    @ResponseBody
//    public void removeSessionMsg(HttpSession session) {
//        session.removeAttribute("msg");
//    }



    @PostMapping("/bisag_internship")
    public String bisagInternship(
            @RequestParam("firstName") String firstName,
//        @RequestParam("lastName") String lastName,
            @RequestParam("contactNo") String contactNo,
            @RequestParam("email") String email,
            @RequestParam("collegeName") String collegeName,
//            @RequestParam("branch") String branch,
            @RequestParam("passportSizeImage") MultipartFile passportSizeImage,
            @RequestParam("icardImage") MultipartFile icardImage,
            @RequestParam("nocPdf") MultipartFile nocPdf,
            @RequestParam("resumePdf") MultipartFile resumePdf,
            @RequestParam("semester") int semester,
            @RequestParam("password") String password,
            @RequestParam("degree") String degree,
            @RequestParam("domain") String domain,
            @RequestParam("joiningDate") Date joiningDate,
            @RequestParam("completionDate") Date completionDate,
            @RequestParam("securityPin") String securityPin,
            @RequestParam("permanentAddress") String permanentAddress,
            @RequestParam("dateOfBirth") Date dateOfBirth,
            @RequestParam("gender") String gender,
            @RequestParam("collegeGuideHodName") String collegeGuideHodName,
            @RequestParam("aggregatePercentage") Double aggregatePercentage,
            @RequestParam("sign") MultipartFile sign,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // File Storage
            String storageDir = baseDir + email + "/";
            File directory = new File(storageDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String passportFileName = storageDir + "passportSizeImage.jpg";
            String icardFileName = storageDir + "collegeIcardImage.jpg";
            String nocFileName = storageDir + "nocPdf.pdf";
            String resumeFileName = storageDir + "resumePdf.pdf";

            Files.write(Paths.get(passportFileName), passportSizeImage.getBytes());
            Files.write(Paths.get(icardFileName), icardImage.getBytes());
            Files.write(Paths.get(nocFileName), nocPdf.getBytes());
            Files.write(Paths.get(resumeFileName), resumePdf.getBytes());

            // Save Intern Application Data
            InternApplication internApplication = new InternApplication();
            internApplication.setFirstName(firstName);
//        internApplication.setLastName(lastName);
            internApplication.setContactNo(contactNo);
            internApplication.setEmail(email);
            internApplication.setCollegeName(collegeName);
//            internApplication.setBranch(branch);
            internApplication.setSemester(semester);
            internApplication.setPassword(password);
            internApplication.setDegree(degree);
            internApplication.setDomain(domain);
            internApplication.setJoiningDate(joiningDate);
            internApplication.setCompletionDate(completionDate);
            internApplication.setPassportSizeImage(passportSizeImage.getBytes());
            internApplication.setCollegeIcardImage(icardImage.getBytes());
            internApplication.setNocPdf(nocPdf.getBytes());
            internApplication.setResumePdf(resumePdf.getBytes());
            internApplication.setSecurityPin(securityPin);
            internApplication.setPermanentAddress(permanentAddress);
            internApplication.setDateOfBirth(dateOfBirth);
            internApplication.setGender(gender);
            internApplication.setCollegeGuideHodName(collegeGuideHodName);
            internApplication.setAggregatePercentage(aggregatePercentage);
            internApplication.setSign(sign.getBytes());

            internApplicationRepo.save(internApplication);

            // Save User Credentials
            MyUser user = new MyUser();
            user.setUsername(email);
            String encryptedPassword = passwordEncoder().encode(password);
            user.setPassword(encryptedPassword);
            user.setSecurityPin(securityPin);
            user.setEnabled(true);
            user.setUserId(Long.toString(internApplication.getId()));
            user.setRole("UNDERPROCESSINTERN");
            userRepo.save(user);

            // Send Confirmation Email
            emailService.sendSimpleEmail(
                    internApplication.getEmail(),
                    "Notification: Successful Application for BISAG Internship\r\n" +
                            "\r\n" +
                            "Dear " + internApplication.getFirstName() + ",\r\n" +
                            "\r\n" +
                            "Congratulations! We are pleased to inform you that your application for the BISAG internship has been successful. Your enthusiasm, qualifications, and potential have stood out, and we believe that you will make valuable contributions to our team.\r\n" +
                            "\r\n" +
                            "As an intern, you will have the opportunity to learn, grow, and gain hands-on experience in a dynamic and innovative environment. We trust that your time with us will be rewarding, and we look forward to seeing your skills and talents in action.\r\n" +
                            "\r\n" +
                            "Please find attached detailed information about the internship program, including your start date, orientation details, and any additional requirements. If you have any questions or need further assistance, feel free to contact [Contact Person/Department].\r\n" +
                            "\r\n" +
                            "Once again, congratulations on being selected for the BISAG internship program. We are excited to welcome you to our team and wish you a fulfilling and successful internship experience.\r\n" +
                            "\r\n" +
                            "Best regards,\r\n" +
                            "\r\n" +
                            "Your Colleague,\r\n" +
                            "Internship Coordinator\r\n" +
                            "BISAG INTERNSHIP PROGRAM\r\n" +
                            "1231231231",
                    "BISAG ADMINISTRATIVE OFFICE"
            );
            redirectAttributes.addFlashAttribute("msg", "Application Submitted Successfully");
//            session.removeAttribute("msg");
            return "redirect:/bisag_internship";


        } catch (DataIntegrityViolationException e) {
            // Handles duplicate email constraint violation
            String errorMessage = "Error: <ul>";
            if (e.getMessage().contains("Duplicate entry")) {
                errorMessage += "<li>Email ID already exists. Please use a different email.</li>";
            }
            errorMessage += "</ul>";
//            session.setAttribute("msg",   errorMessage);
            redirectAttributes.addFlashAttribute("msg", errorMessage);
            return "redirect:/bisag_internship";


        } catch (ConstraintViolationException e) {
            // Handles validation errors
            StringBuilder errorMessage = new StringBuilder("Error: Validation failed due to the following reasons:<br><ul>");
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                errorMessage.append("<li>").append(violation.getMessage()).append("</li>");
            }
            errorMessage.append("</ul>");
//            session.setAttribute("msg", errorMessage.toString());
            redirectAttributes.addFlashAttribute("msg", errorMessage.toString());
            return "redirect:/bisag_internship";

        } catch (Exception e) {
            // Handles all other errors
            String errorMessage = "Error: Validation failed due to the following reasons:<br><ul>";
            errorMessage += "<li>" + e.getMessage() + "</li>";
            errorMessage += "</ul>";
//            session.setAttribute("msg", errorMessage);
            redirectAttributes.addFlashAttribute("msg", errorMessage);
            return "redirect:/bisag_internship";
        }

    }

    @PostMapping("/remove-session-msg")
    public ResponseEntity<Void> removeSessionMessage(HttpSession session, RedirectAttributes redirectAttributes) {
//        session.removeAttribute("msg");
        redirectAttributes.addFlashAttribute("msg", "Removing Session");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Fetch internId from the session
        String internId = (String) request.getSession().getAttribute("id");

        if (internId != null) {
            // Fetch intern details using internId from internService
            Intern intern = internService.getIntern(internId).orElseThrow(() -> new RuntimeException("Intern not found"));            if (intern != null) {
                // Log intern's logout action
                logService.saveLog(intern.getInternId(), "Logged Out", "Intern " + intern.getFirstName() + " logged out.");
            }
        }

        // Perform logout logic
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/login?logout"; // Redirect to the login page with a logout parameter
    }

    //Cancel internship
    @GetMapping("/under_process_intern")
    public ModelAndView underProcessApplication(HttpSession session) {
        ModelAndView mv = new ModelAndView("under_process_application");
        String username = (String) session.getAttribute("username");
        InternApplication intern = internService.getInternApplicationByUsername(username);
        session.setAttribute("id", intern.getId());
        session.setAttribute("username", username);
        mv.addObject("internApplication", intern);
        mv.addObject("username", username);
        return mv;
    }

    @GetMapping("/cancel/{id}")
    public String cancelInternship(@PathVariable("id") long id) {
        Optional<InternApplication> intern = internService.getInternApplication(id);
        internService.cancelInternApplication(intern);
        return "redirect:/under_process_intern";
    }
}