package com.rh4.services;

import com.rh4.entities.Intern;
import com.rh4.entities.LeaveApplication;
import com.rh4.repositories.InternRepo;
import com.rh4.repositories.LeaveApplicationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepo leaveApplicationRepo;
    @Autowired
    private InternRepo internRepo;

    public void applyForLeave(LeaveApplication leaveApplication) {
        leaveApplication.setStatus("Pending");
        leaveApplicationRepo.save(leaveApplication);
    }

    public List<LeaveApplication> getInternLeaves(String internId) {
        return leaveApplicationRepo.findByInternId(internId);
    }

    public List<LeaveApplication> getPendingLeaves() {
        return leaveApplicationRepo.findByStatus("Pending");
    }

    public Optional<LeaveApplication> getLeaveById(Long id) {
        return leaveApplicationRepo.findById(id);
    }

    public LeaveApplication updateLeaveApproval(Long id, boolean isGuide, boolean approve) {
        Optional<LeaveApplication> leaveOpt = leaveApplicationRepo.findById(id);
        if (leaveOpt.isPresent()) {
            LeaveApplication leave = leaveOpt.get();
            if (isGuide) {
                leave.setGuideApproval(approve);
            } else {
                leave.setAdminApproval(approve);
            }
            leave.updateStatus();
            return leaveApplicationRepo.save(leave);
        }
        return null;
    }

    /**
     * Get all leave applications
     */
    public List<LeaveApplication> getAllLeaveApplications() {
        return leaveApplicationRepo.findAll();
    }

    /**
     * Get leave applications by intern ID
     */
    public List<LeaveApplication> getLeaveApplicationsByInternId(String internId) {
        return leaveApplicationRepo.findByInternId(internId);
    }

    /**
     * Submit a new leave application
     */
    public LeaveApplication submitLeaveApplication(LeaveApplication leaveApplication) {
        leaveApplication.setStatus("Pending"); // Default status
        leaveApplication.setGuideApproval(false);
        leaveApplication.setAdminApproval(false);
        return leaveApplicationRepo.save(leaveApplication);
    }

    /**
     * Approve leave (Admin or Guide)
     */
    public void approveLeave(Long leaveId, String role) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(leaveId);
        if (optionalLeave.isPresent()) {
            LeaveApplication leaveApplication = optionalLeave.get();
            if ("admin".equalsIgnoreCase(role)) {
                leaveApplication.setAdminApproval(true);
            } else if ("guide".equalsIgnoreCase(role)) {
                leaveApplication.setGuideApproval(true);
            }

            // If both guide and admin approve, update status to "Approved"
            if (leaveApplication.isAdminApproval() && leaveApplication.isGuideApproval()) {
                leaveApplication.setStatus("Approved");
            }

            leaveApplicationRepo.save(leaveApplication);
        }
    }

    /**
     * Reject leave (Admin or Guide)
     */
    public void rejectLeave(Long leaveId, String role) {
        Optional<LeaveApplication> optionalLeave = leaveApplicationRepo.findById(leaveId);
        if (optionalLeave.isPresent()) {
            LeaveApplication leaveApplication = optionalLeave.get();
            leaveApplication.setStatus("Rejected"); // If any one rejects, status becomes "Rejected"
            leaveApplication.setAdminApproval(false);
            leaveApplication.setGuideApproval(false);
            leaveApplicationRepo.save(leaveApplication);
        }
    }

    public void submitLeave(String internId, LeaveApplication leaveApplication, MultipartFile proofDoc) {
        Intern intern = internRepo.findById(internId)
                .orElseThrow(() -> new RuntimeException("Intern not found"));

        leaveApplication.setInternId(internId);
        leaveApplication.setStatus("Pending");

        // Use LocalDateTime directly
        leaveApplication.setSubmittedOn(LocalDateTime.now());

        if (!proofDoc.isEmpty()) {
            String fileName = saveFile(proofDoc);
            leaveApplication.setProofDocument(fileName);
        }

        leaveApplicationRepo.save(leaveApplication);
    }

    public String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get("uploads/" + fileName);
            Files.write(filePath, file.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("⚠️ Error saving file", e);
        }
    }
}