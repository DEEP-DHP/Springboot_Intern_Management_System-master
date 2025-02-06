package com.rh4.repositories;

import com.rh4.entities.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveApplicationRepo extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByInternId(String internId);
    List<LeaveApplication> findByStatus(String status);
}