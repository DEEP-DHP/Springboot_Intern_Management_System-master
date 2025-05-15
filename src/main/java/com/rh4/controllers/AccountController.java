package com.rh4.controllers;

import com.rh4.entities.Account;
import com.rh4.entities.Admin;
import com.rh4.entities.Guide;
import com.rh4.entities.RRecord;
import com.rh4.repositories.RecordRepo;
import com.rh4.services.AccountService;
import com.rh4.services.LogService;
import com.rh4.services.RecordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bisag/account")
public class AccountController {

    @Autowired
    HttpSession session;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LogService logService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private RecordRepo recordRepo;

    public Account getSignedInAccount() {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            System.out.println("No username found in session.");
            return null;
        }

        Account account = accountService.getAccountByUsername(username);

        if (account == null) {
            System.out.println("No Account found for username: " + username);
        }

        return account;
    }

    @GetMapping("/account_dashboard")
    public ModelAndView account_dashboard(Model model) {
        String username = (String) session.getAttribute("username");
        Account account = accountService.getAccountByUsername(username);

        if (account != null) {
            // Redirect to change password if firstLogin is 1 (true)
            if (account.getFirstLogin() == 1) {
                return new ModelAndView("redirect:/bisag/account/change_passwordd");
            }

            session.setAttribute("id", account.getAccountId());
            session.setAttribute("username", username);

            logService.saveLog(String.valueOf(account.getAccountId()), "Accountant Accessed Dashboard",
                    "Accountant " + account.getName() + " accessed the dashboard.");

            ModelAndView mv = new ModelAndView("account/account_dashboard");
            mv.addObject("username", username);
            mv.addObject("account", account);
            return mv;
        } else {
            System.out.println("Error: Account not found for logging!");
            return new ModelAndView("redirect:/bisag/login");
        }
    }

    @PostMapping("/change_password")
    public String changePassword(@RequestParam("newPassword") String newPassword) {
        Account account = getSignedInAccount();

        logService.saveLog(String.valueOf(account.getAccountId()), "Password Change Request",
                "Accountant " + account.getName() + " requested a password change.");

        accountService.changePassword(account, newPassword);

        logService.saveLog(String.valueOf(account.getAccountId()), "Password Changed Successfully",
                "Accountant " + account.getName() + " successfully changed their password.");

        return "redirect:/logout";
    }

    @GetMapping("/change_passwordd")
    public ModelAndView changePasswordPage(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return new ModelAndView("redirect:/bisag/account/login");
        }

        ModelAndView mv = new ModelAndView("account/change_passwordd");
        mv.addObject("username", username);
        mv.addObject("forcePasswordChange", true);
        return mv;
    }

    @PostMapping("/change_passwordd")
    public String changeAccountPassword(@RequestParam("newPassword") String newPassword) {
        Account account = getSignedInAccount(); // Get currently logged-in accountant
        accountService.changePassword(account, newPassword);
        Account accountEntity = accountService.getAccountByUsername(account.getEmailId());

        if (accountEntity != null) {
            System.out.println("Before update, firstLogin: " + accountEntity.getEmailId());
            accountEntity.setFirstLogin(0); // Mark first login as complete

            Optional<Account> existingAccount = accountService.getAccount(accountEntity.getAccountId());
            accountService.updateAccount(accountEntity, existingAccount);

            System.out.println("After update, firstLogin: " + accountEntity.getFirstLogin());
        } else {
            System.out.println("Account entity not found for email: " + account.getEmailId());
        }
        logService.saveLog(String.valueOf(account.getAccountId()), "First Password Changed", "Password changed successfully for first time.");

        return "redirect:/logout";
    }

    @GetMapping("/relieving_records_list")
    public String getAllRelievingRecords(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "40") int size,
                                         @RequestParam(defaultValue = "false") boolean showAll,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            List<RRecord> records;

            if (showAll) {
                records = recordRepo.findBySendAccountOrderByIdDesc("Yes");
            } else {
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));
                Page<RRecord> recordPage = recordRepo.findBySendAccount("Yes", pageable);
                records = recordPage.getContent();

                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", recordPage.getTotalPages());
                model.addAttribute("totalCount", recordPage.getTotalElements());
            }

            model.addAttribute("records", records);
            model.addAttribute("pageSize", size);
            model.addAttribute("showAll", showAll);

            Account account = getSignedInAccount();
            model.addAttribute("account", account);

            if (account != null) {
                logService.saveLog(String.valueOf(account.getAccountId()), "Viewed Relieving Records",
                        "Accountant " + account.getName() + " accessed the relieving records list.");
            }

            if (!records.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage", "Relieving records loaded successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No relieving records found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading relieving records. Please try again.");
        }

        return "account/relieving_records_list";
    }

    @PostMapping("/update_relieving_record")
    @ResponseBody
    public ResponseEntity<String> updateRelievingRecordInline(@RequestBody RRecord updatedRecord) {
        try {
            Optional<RRecord> existing = recordService.getById(updatedRecord.getId());
            if (existing.isPresent()) {
                RRecord record = existing.get();
                record.setStipend(updatedRecord.getStipend());
                record.setSubmissionTimestamp(LocalDateTime.now());
                recordService.saveRecord(record);
                Account signedInAccount = getSignedInAccount();
                if (signedInAccount != null) {
                    logService.saveLog(String.valueOf(signedInAccount.getAccountId()), "Updated Relieving Record",
                            "Accountant " + signedInAccount.getName() + " updated stipend details for intern ID: " + record.getInternId());
                } else {
                    System.out.println("Error: Signed-in accountant not found for logging!");
                }
                return ResponseEntity.ok("Updated");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Record not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred");
        }
    }

    @PostMapping("/update_account/{id}")
    public String updateAdmin(@ModelAttribute("account") Account account, @PathVariable("id") long id) {
        Optional<Account> existingAccount = accountService.getAccount(account.getAccountId());

        if (existingAccount.isPresent()) {

            String currentPassword = existingAccount.get().getPassword();
            Account updatedAccount = existingAccount.get();
            updatedAccount.setName(account.getName());
            updatedAccount.setLocation(account.getLocation());
            updatedAccount.setContactNo(account.getContactNo());
            updatedAccount.setEmailId(account.getEmailId());

            Account signedInAccount = getSignedInAccount();
            if (signedInAccount != null) {
                logService.saveLog(String.valueOf(signedInAccount.getAccountId()), "Updating Accountant Details",
                        "Accountant " + signedInAccount.getName() + " updated the details of admin ID: " + id + ", Name: " + updatedAccount.getName());
            } else {
                System.out.println("Error: Signed-in accountant not found for logging!");
            }

            accountService.updateAccount(updatedAccount, existingAccount);
        }
        return "redirect:/logout";
    }
}
