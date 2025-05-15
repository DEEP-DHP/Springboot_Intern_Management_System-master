package com.rh4.controllers;

import java.util.List;
import java.util.Optional;

import com.rh4.entities.*;
import com.rh4.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.rh4.repositories.InternApplicationRepo;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/bisag/super_admin")
public class SuperAdminController {
	private EmailSenderService emailService;
	public SuperAdminController(EmailSenderService emailService) {	
		this.emailService = emailService;
	}
	@Autowired
	private AdminService adminService;
	@Autowired
	private HRService hrService;
	@Autowired
	private SuperAdminService superAdminService;
	@Autowired
	private AccountService accountService;
	private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
	@GetMapping("/super_admin_dashboard")
	public ModelAndView superAdmin_Dashboard(HttpSession session, Model model)
	{
			ModelAndView mv = new ModelAndView("super_admin/super_admin_dashboard");

		    String username = (String) session.getAttribute("username");

		    SuperAdmin sadmin = superAdminService.getSuperAdminByUsername(username);
		    
		    long adminCount = superAdminService.countAdmin();
	 	    model.addAttribute("adminCount", adminCount);

			long HRCount = superAdminService.countHR();
			model.addAttribute("HRCount", HRCount);

			long AccountCount = superAdminService.countAccount();
			model.addAttribute("AccountCount", AccountCount);

		    session.setAttribute("id", sadmin.getSuperAdminId());
		    session.setAttribute("username", username);

		    mv.addObject("username", username);

		    return mv;
		//return "super_admin/super_admin_dashboard";
	}
	@GetMapping("/register_admin")
	public String registerAdmin()
	{
		return "super_admin/admin_registration";
	}
	
	@PostMapping("/register_admin")
	public String registerAdmin(@ModelAttribute("admin") Admin admin)
	{
		adminService.registerAdmin(admin);
		emailService.sendSimpleEmail(
				admin.getEmailId(),
				"Notification: Appointment as Administrator\r\n"
				+ "\r\n"
				+ "Dear " + admin.getName() + "\r\n"
				+ "\r\n"
				+ "I trust this email finds you well. We are pleased to inform you that you have been appointed as an administrator within our organization, effective immediately. Your dedication and contributions to the team have not gone unnoticed, and we believe that your new role will bring added value to our operations.\r\n"
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
				+ "\r\n"
				+ "Best regards,\r\n"
				+ "\r\n"
				+ "Your Colleague,\r\n"
				+ "Administrator\r\n"
				+ "1231231231",
				"BISAG ADMINISTRATIVE OFFICE"
		);
		
		return "redirect:/bisag/super_admin/register_admin";
		
	}
	//--------------------------------------- Admin List -------------------------------------------//
	
	@GetMapping("/admin_list")
	public ModelAndView adminList()
	{
		ModelAndView mv = new ModelAndView("super_admin/admin_list");
		List<Admin> admins = adminService.getAdmin();
		mv.addObject("admins", admins);
		return mv;
	}
	@GetMapping("/admin_list/{id}")
	public ModelAndView adminList(@PathVariable("id") long id)
	{
		System.out.println("id"+id);
		ModelAndView mv = new ModelAndView();
		Optional<Admin> admin = adminService.getAdmin(id);
		mv.addObject("admin",admin);
		mv.setViewName("super_admin/admin_list_detail");
		return mv;
	}
	
	@PostMapping("/admin_list/ans")
	public String adminListRedirect()
	{
		System.out.println("iddd");
		return "redirect:/bisag/super_admin/admin_list";
	}
	
	//-------------------------------------- Admin Update ------------------------------------------//
	
	@GetMapping("/update_admin/{id}")
	public ModelAndView updateAdmin(@PathVariable("id") long id) {
		ModelAndView mv = new ModelAndView("super_admin/update_admin");
		Optional<Admin> admin = adminService.getAdmin(id);
		mv.addObject("admin", admin.orElse(new Admin()));
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
	    	if(!currentPassword.equals(encodePassword(admin.getPassword())) && admin.getPassword()!="")
	    	{
	    		updatedAdmin.setPassword(encodePassword(admin.getPassword()));
	    	}
	        adminService.updateAdmin(updatedAdmin,existingAdmin);
	    }
		return "redirect:/bisag/super_admin/admin_list";
	}

	//-------------------------------------- Admin Delete ------------------------------------------//
	
	// Delete Admin
    @PostMapping("/admin_list/delete/{id}")
    public String deleteAdmin(@PathVariable("id") long id) {
        adminService.deleteAdmin(id);
        return "redirect:/bisag/super_admin/admin_list";
    }
	
	//-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_HR-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
	@GetMapping("/register_hr")
	public String registerHR()
	{
		return "super_admin/HR_registration";
	}

	@PostMapping("/register_hr")
	public String registerHR(@ModelAttribute("HR") HR hr)
	{
		hrService.registerHR(hr);
		emailService.sendSimpleEmail(
				hr.getEmailId(),
				"Notification: Appointment as HR\r\n"
						+ "\r\n"
						+ "Dear " + hr.getName() + "\r\n"
						+ "\r\n"
						+ "I trust this email finds you well. We are pleased to inform you that you have been appointed as HR within our organization, effective immediately. Your dedication and contributions to the team have not gone unnoticed, and we believe that your new role will bring added value to our operations.\r\n"
						+ "\r\n"
						+ "As a HR, you now hold a position of responsibility within the organization. We trust that you will approach your duties with diligence, professionalism, and a commitment to upholding the values of our organization.\r\n"
						+ "\r\n"
						+ "It is imperative to recognize the importance of your role and the impact it may have on the functioning of our team. We have confidence in your ability to handle the responsibilities that come with this position and to contribute positively to the continued success of our organization.\r\n"
						+ "\r\n"
						+ "We would like to emphasize the importance of maintaining the highest standards of integrity and ethics in your role. It is expected that you will use your HR privileges responsibly and refrain from any misuse.\r\n"
						+ "\r\n"
						+ "Should you have any questions or require further clarification regarding your new responsibilities, please do not hesitate to reach out to [Contact Person/Department].\r\n"
						+ "\r\n"
						+ "Once again, congratulations on your appointment as an HR. We look forward to your continued contributions and success in this elevated role.\r\n"
						+ "\r\n"
						+ "Best regards,\r\n"
						+ "\r\n"
						+ "Your Colleague,\r\n"
						+ "HR\r\n"
						+ "1231231231",
				"BISAG HR OFFICE"
		);

		return "redirect:/bisag/super_admin/register_hr";

	}

	//--------------------------------------- HR List -------------------------------------------//

	@GetMapping("/hr_list")
	public ModelAndView hrList()
	{
		ModelAndView mv = new ModelAndView("super_admin/hr_list");
		List<HR> hrs = hrService.getAllHr();
		mv.addObject("hrs", hrs);
		return mv;
	}
	@GetMapping("/hr_list/{id}")
	public ModelAndView hrList(@PathVariable("id") long id)
	{
		System.out.println("id"+id);
		ModelAndView mv = new ModelAndView();
		Optional<HR> hr = hrService.getHR(id);
		mv.addObject("hr",hr);
		mv.setViewName("super_admin/hr_list_detail");
		return mv;
	}

	@PostMapping("/hr_list/ans")
	public String hrListRedirect()
	{
		return "redirect:/bisag/super_admin/hr_list";
	}

	//-------------------------------------- HR Update ------------------------------------------//

	@GetMapping("/update_hr/{id}")
	public ModelAndView updateHR(@PathVariable("id") long id) {
		ModelAndView mv = new ModelAndView("super_admin/update_hr");
		Optional<HR> hr = hrService.getHR(id);
		mv.addObject("hr", hr.orElse(new HR()));
		return mv;
	}

	@PostMapping("/update_hr/{id}")
	public String updateHR(@ModelAttribute("hr") HR hr, @PathVariable("id") long id) {
		Optional<HR> existingHR = hrService.getHR(hr.getHRId());

		if (existingHR.isPresent()) {

			String currentPassword = existingHR.get().getPassword();
			HR updatedHR = existingHR.get();
			updatedHR.setName(hr.getName());
			updatedHR.setLocation(hr.getLocation());
			updatedHR.setContactNo(hr.getContactNo());
			updatedHR.setEmailId(hr.getEmailId());
			if(!currentPassword.equals(encodePassword(hr.getPassword())) && hr.getPassword()!="")
			{
				updatedHR.setPassword(encodePassword(hr.getPassword()));
			}
			// Save the updated hr entity
			hrService.updateHR(updatedHR,existingHR);
		}
		return "redirect:/bisag/super_admin/hr_list";
	}

	//-------------------------------------- HR Delete ------------------------------------------//

	// Delete HR
	@PostMapping("/hr_list/delete/{id}")
	public String deleteHR(@PathVariable("id") long id) {
		hrService.deleteHR(id);
		return "redirect:/bisag/super_admin/hr_list";
	}






//_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_
//_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-Accounts Module_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
//_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_

	@GetMapping("/register_account")
	public String registerAccount()
	{
		return "super_admin/account_registration";
	}

	@PostMapping("/register_account")
	public String registerAccount(@ModelAttribute("Account") Account account)
	{
		accountService.registerAccount(account);
		emailService.sendSimpleEmail(
				account.getEmailId(),
				"Notification: Appointment as Account Office\r\n"
						+ "\r\n"
						+ "Dear " + account.getName() + "\r\n"
						+ "\r\n"
						+ "I trust this email finds you well. We are pleased to inform you that you have been appointed as Accountant within our organization, effective immediately. Your dedication and contributions to the team have not gone unnoticed, and we believe that your new role will bring added value to our operations.\r\n"
						+ "\r\n"
						+ "As an Accountant, you now hold a position of responsibility within the organization. We trust that you will approach your duties with diligence, professionalism, and a commitment to upholding the values of our organization.\r\n"
						+ "\r\n"
						+ "It is imperative to recognize the importance of your role and the impact it may have on the functioning of our team. We have confidence in your ability to handle the responsibilities that come with this position and to contribute positively to the continued success of our organization.\r\n"
						+ "\r\n"
						+ "We would like to emphasize the importance of maintaining the highest standards of integrity and ethics in your role. It is expected that you will use your HR privileges responsibly and refrain from any misuse.\r\n"
						+ "\r\n"
						+ "Should you have any questions or require further clarification regarding your new responsibilities, please do not hesitate to reach out to [Contact Person/Department].\r\n"
						+ "\r\n"
						+ "Once again, congratulations on your appointment as an HR. We look forward to your continued contributions and success in this elevated role.\r\n"
						+ "\r\n"
						+ "Best regards,\r\n"
						+ "\r\n"
						+ "Your Colleague,\r\n"
						+ "Accounts\r\n"
						+ "1231231231",
				"BISAG ACCOUNT OFFICE"
		);

		return "redirect:/bisag/super_admin/register_account";

	}

	//--------------------------------------- Account List -------------------------------------------//

	@GetMapping("/account_list")
	public ModelAndView accountList()
	{
		ModelAndView mv = new ModelAndView("super_admin/account_list");
		List<Account> accounts = accountService.getAllAccounts();
		mv.addObject("accounts", accounts);
		return mv;
	}
	@GetMapping("/account_list/{id}")
	public ModelAndView accountList(@PathVariable("id") long id)
	{
		System.out.println("id"+id);
		ModelAndView mv = new ModelAndView();
		Optional<Account> account = accountService.getAccount(id);
		mv.addObject("account",account);
		mv.setViewName("super_admin/account_list_detail");
		return mv;
	}

	@PostMapping("/account_list/ans")
	public String accountListRedirect()
	{
		return "redirect:/bisag/super_admin/account_list";
	}

	//-------------------------------------- Account Update ------------------------------------------//

	@GetMapping("/update_account/{id}")
	public ModelAndView updateAccount(@PathVariable("id") long id) {
		ModelAndView mv = new ModelAndView("super_admin/update_account");
		Optional<Account> account = accountService.getAccount(id);
		mv.addObject("account", account.orElse(new Account()));
		return mv;
	}

	@PostMapping("/update_account/{id}")
	public String updateAccount(@ModelAttribute("account") Account account, @PathVariable("id") long id) {
		Optional<Account> existingAccount = accountService.getAccount(account.getAccountId());

		if (existingAccount.isPresent()) {

			String currentPassword = existingAccount.get().getPassword();
			Account updatedAccount = existingAccount.get();
			updatedAccount.setName(account.getName());
			updatedAccount.setLocation(account.getLocation());
			updatedAccount.setContactNo(account.getContactNo());
			updatedAccount.setEmailId(account.getEmailId());
			if(!currentPassword.equals(encodePassword(account.getPassword())) && account.getPassword()!="")
			{
				updatedAccount.setPassword(encodePassword(account.getPassword()));
			}
			// Save the updated hr entity
			accountService.updateAccount(updatedAccount,existingAccount);
		}
		return "redirect:/bisag/super_admin/account_list";
	}

	//-------------------------------------- Account Delete ------------------------------------------//

	// Delete Account
	@PostMapping("/account_list/delete/{id}")
	public String deleteAccount(@PathVariable("id") long id) {
		accountService.deleteAccount(id);
		return "redirect:/bisag/super_admin/account_list";
	}
}
