package com.rh4.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rh4.entities.*;
import com.rh4.repositories.*;

@Service
public class HRService {

    @Autowired
    private HRRepo hrRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MyUserService myUserService;

    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public void registerHR(HR hr) {
        // save to hr table
        String encryptedPassword = passwordEncoder().encode(hr.getPassword());
        hr.setPassword(encryptedPassword);
        hrRepo.save(hr);
        // save to user table
        MyUser user = new MyUser();
        user.setUsername(hr.getEmailId());
        // encrypt password
        user.setPassword(encryptedPassword);
        user.setRole("HR");
        user.setEnabled(true);
        // from long to string
        String userId = Long.toString(hr.getHRId());
        user.setUserId(userId);
        userRepo.save(user);
        System.out.println(user.getId());
    }

    public List<HR> getAllHr() {
        return hrRepo.findAll();
    }

    public Optional<HR> getHR(long id) {
        return hrRepo.findById(id);
    }

    public void deleteHR(long id) {
        hrRepo.deleteById(id);
    }

    public void updateHR(HR updatedHR, Optional<HR> existingHR) {

        if (updatedHR.getEmailId().equals(existingHR.get().getEmailId())) {
            hrRepo.save(updatedHR);
            String HRId = Long.toString(updatedHR.getHRId());
            String password = updatedHR.getPassword();
            String emailId = updatedHR.getEmailId();
            userRepo.updateHRUser(Long.valueOf(HRId), password, emailId, "HR");
        } else {
            if (userRepo.findByUsername(updatedHR.getEmailId()) == null) {
                // If it doesn't exist, proceed with the update
                hrRepo.save(updatedHR);
                String HRId = Long.toString(updatedHR.getHRId());
                String password = updatedHR.getPassword();
                String emailId = updatedHR.getEmailId();
                userRepo.updateHRUser(Long.valueOf(HRId), password, emailId, "HR");
            } else {
                // Handle the case where the email already exists
                // You can throw an exception, log a message, or take other appropriate action
                // For now, let's print a message to the console
                System.out.println("Email already exists: " + updatedHR.getEmailId());
            }
        }

    }

    public HR getHRByUsername(String username) {
        return hrRepo.findByEmailId(username);
    }

    public void changePassword(HR hr, String newPassword) {
        String encryptedPassword = passwordEncoder().encode(newPassword);
        hr.setPassword(encryptedPassword);
        hrRepo.save(hr);
        // save to user table
        MyUser user = myUserService.getUserByUsername(hr.getEmailId());
        user.setPassword(encryptedPassword);
        userRepo.save(user);
    }

}