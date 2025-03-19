package com.rh4.services;

import com.rh4.entities.Intern;
import com.rh4.entities.LeaveApplication;
import com.rh4.repositories.InternRepo;
import com.rh4.repositories.LeaveApplicationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepo leaveApplicationRepo;

    @Autowired
    private InternRepo internRepo;

    public long countPendingLeaveRequests() {
        return leaveApplicationRepo.countByStatus("Pending");
    }

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

    //Get all leave applications
    public List<LeaveApplication> getAllLeaveApplications() {
        return leaveApplicationRepo.findAll();
    }

    //Get leave applications by intern ID
    public List<LeaveApplication> getLeaveApplicationsByInternId(String internId) {
        return leaveApplicationRepo.findByInternId(internId);
    }

    //Submit a new leave application
    public LeaveApplication submitLeaveApplication(LeaveApplication leaveApplication) {
        leaveApplication.setStatus("Pending"); // Default status
        leaveApplication.setGuideApproval(false);
        leaveApplication.setAdminApproval(false);
        return leaveApplicationRepo.save(leaveApplication);
    }

    //Approve leave (Admin or Guide)
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

    //Reject leave (Admin or Guide)
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
    // Get the last submitted leave application
    public LeaveApplication getLastLeaveApplication(String internId) {
        return leaveApplicationRepo.findTopByInternIdOrderBySubmittedOnDesc(internId);
    }

    // Get full leave history for the intern
    public List<LeaveApplication> getInternLeaveHistory(String internId) {
        return leaveApplicationRepo.findByInternIdOrderBySubmittedOnDesc(internId);
    }

    public long countPendingLeaveApplications() {
        return leaveApplicationRepo.countByStatus("Pending");
    }
    //Submit leave without proof document
    public void submitLeave(String internId, LeaveApplication leaveApplication) {
        Intern intern = internRepo.findById(internId)
                .orElseThrow(() -> new RuntimeException("Intern not found"));

        leaveApplication.setInternId(internId);
        leaveApplication.setStatus("Pending");

        leaveApplication.setSubmittedOn(LocalDateTime.now());

        leaveApplicationRepo.save(leaveApplication);
    }
}