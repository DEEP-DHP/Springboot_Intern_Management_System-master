package com.rh4.services;

import com.rh4.entities.Verification;
import com.rh4.repositories.VerificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationService {

    @Autowired
    private VerificationRepo verificationRepo;

    // Get all pending verification requests
    public List<Verification> getPendingRequests() {
        return verificationRepo.findByStatus("PENDING");
    }

    // Get verification request by ID (using Long instead of String)
    public Optional<Verification> getVerificationById(Long id) {
        return verificationRepo.findById(String.valueOf(id));  // Change to Long type
    }

    // Approve a verification request (using Long instead of String)
    public Verification approveVerification(Long id, String adminId, String remarks) {
        Optional<Verification> verification = verificationRepo.findById(String.valueOf(id));  // Change to Long type
        if (verification.isPresent()) {
            Verification v = verification.get();
            v.setStatus("APPROVED");
            v.setAdminId(adminId);
            v.setVerifiedDate(new Date());
            v.setRemarks(remarks);
            return verificationRepo.save(v);
        }
        return null;
    }

    // Reject a verification request (using Long instead of String)
    public Verification rejectVerification(Long id, String adminId, String remarks) {
        Optional<Verification> verification = verificationRepo.findById(String.valueOf(id));  // Change to Long type
        if (verification.isPresent()) {
            Verification v = verification.get();
            v.setStatus("REJECTED");
            v.setAdminId(adminId);
            v.setVerifiedDate(new Date());
            v.setRemarks(remarks);
            return verificationRepo.save(v);
        }
        return null;
    }

    // Create a new verification request
    public void createVerificationRequest(Verification verification) {
        verification.setStatus("PENDING");
        verificationRepo.save(verification);
    }
}