package com.rh4.controllers;

import com.rh4.entities.*;
import com.rh4.repositories.RecordRepo;
import com.rh4.repositories.VerificationRepo;
import com.rh4.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/bisag/hr")
public class HRController {
    @Autowired
    HttpSession session;
    @Autowired
    private LogService logService;
    @Autowired
    private HRService hrService;
    @Autowired
    private InternService internService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private RecordRepo recordRepo;
    @Autowired
    private VerificationRepo verificationRepo;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private GroupService groupService;


    public HR getSignedInHR() {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            System.out.println("No username found in session.");
            return null;
        }

        HR hr = hrService.getHRByUsername(username);

        if (hr == null) {
            System.out.println("No hr found for username: " + username);
        }

        return hr;
    }

    @GetMapping("/hr_dashboard")
    public ModelAndView hr_dashboard(Model model) {
        ModelAndView mv = new ModelAndView("hr/hr_dashboard");

        String username = (String) session.getAttribute("username");
        HR hr = hrService.getHRByUsername(username);

        if (hr != null) {
            session.setAttribute("id", hr.getHRId());
            session.setAttribute("username", username);

            logService.saveLog(String.valueOf(hr.getHRId()), "HR Dashboard Accessed",
                    "HR " + hr.getName() + " accessed the dashboard.");

            long countInterns = internService.countInterns();
            model.addAttribute("countInterns", countInterns);

            mv.addObject("username", username);
            mv.addObject("hr", hr);
        } else {
            System.out.println("Error: HR not found for logging!");
        }

        return mv;
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        HR hr = getSignedInHR();

        logService.saveLog(String.valueOf(hr.getHRId()), "Password Change Request",
                "HR " + hr.getName() + " requested a password change.");

        hrService.changePassword(hr, newPassword);

        logService.saveLog(String.valueOf(hr.getHRId()), "Password Changed Successfully",
                "HR " + hr.getName() + " successfully changed their password.");

        return "redirect:/logout";
    }

    @GetMapping("/relieving_records_list")
    public String getAllRelievingRecords(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<RRecord> records = recordService.getAllRecords();
            model.addAttribute("records", records);

            HR hr = getSignedInHR();

            if (hr != null) {
                String id = String.valueOf(hr.getHRId());

                logService.saveLog(id, "Viewed Relieving Records",
                        "HR " + hr.getName() + " accessed the relieving records list.");
            }

            if (!records.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage", "Relieving records loaded successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No relieving records found.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading relieving records. Please try again.");
        }

        return "hr/relieving_records_list";
    }

    //Show records ID wise-------------------
    @GetMapping("/relieving_records_detail/{id}")
    public String getRecordsDetails(@PathVariable("id") String id, Model model) {
        Optional<RRecord> record = recordService.getRecordById(Long.parseLong(id));
        if (record.isPresent()) {
            model.addAttribute("records", record);
            logService.saveLog(id, "Relieving Records Details View",
                    "HR viewed the details of records with ID: " + id);
            return "hr/relieving_records_detail";
        } else {
            return "error/404";
        }
    }

    @PostMapping("/updateIdentityCards")
    @ResponseBody
    public ResponseEntity<String> updateIdentityCards(@RequestBody Map<String, String> data) {
        Long id = Long.parseLong(data.get("id"));
        String newStatus = data.get("identityCards");

        Optional<RRecord> optionalRecord = recordRepo.findById(id);
        if (optionalRecord.isPresent()) {
            RRecord record = optionalRecord.get();
            record.setIdentityCards(newStatus);
            recordRepo.save(record);
            return ResponseEntity.ok("Updated Successfully");
        }
        HR hr = getSignedInHR();

        if (hr != null) {
            String idd = String.valueOf(hr.getHRId());

            logService.saveLog(idd, "Updated Relieving Records",
                    "HR " + hr.getName() + " updated the relieving records.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Record not found");
    }

    // Display Approved Verifications
    @GetMapping("/verification_requests")
    public String showApprovedVerifications(Model model) {
        List<Verification> approvedVerifications = verificationService.getApprovedVerifications();
        model.addAttribute("verifications", approvedVerifications);

        HR hr = getSignedInHR();
        String id = String.valueOf(hr.getHRId());

        logService.saveLog(id, "Viewed Approved Verifications",
                "HR " + hr.getName() + " viewed the list of approved verifications.");

        return "hr/hr_verification_list";
    }

    @GetMapping("/intern_verification_details/{id}")
    public ModelAndView internverificationDetails(@PathVariable("id") String id, Model model) {
        ModelAndView mv = new ModelAndView();

        Optional<Intern> intern = internService.getIntern(id);
//        model = countNotifications(model);

        mv.addObject("intern", intern);

        List<College> colleges = fieldService.getColleges();
        List<Domain> domains = fieldService.getDomains();
//        List<Branch> branches = fieldService.getBranches();
        List<GroupEntity> groups = groupService.getGroups();

        mv.addObject("colleges", colleges);
        mv.addObject("domains", domains);
//        mv.addObject("branches", branches);
        mv.addObject("groups", groups);

        mv.setViewName("hr/intern_verification_details");

        HR hr = getSignedInHR();
        if (hr != null && intern.isPresent()) {
            logService.saveLog(String.valueOf(hr.getHRId()), "Viewed Intern Verification Details",
                    "HR " + hr.getName() + " viewed the details of intern ID: " + id + ", Name: " + intern.get().getFirstName());
        } else {
            System.out.println("Error: HR not found for logging!");
        }

        return mv;
    }
}
