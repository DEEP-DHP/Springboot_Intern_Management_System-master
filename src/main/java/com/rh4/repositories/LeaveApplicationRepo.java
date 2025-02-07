package com.rh4.repositories;

import com.rh4.entities.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaveApplicationRepo extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByInternId(String internId);
    List<LeaveApplication> findByStatus(String status);
    List<LeaveApplication> findByStatusIn(List<String> statuses);
    Optional<LeaveApplication> findById(Long id);
    // Fetch last submitted leave for an intern
    LeaveApplication findTopByInternIdOrderBySubmittedOnDesc(String internId);

    // Fetch all leave applications for an intern
    List<LeaveApplication> findByInternIdOrderBySubmittedOnDesc(String internId);

    long countByStatus(String status);
}