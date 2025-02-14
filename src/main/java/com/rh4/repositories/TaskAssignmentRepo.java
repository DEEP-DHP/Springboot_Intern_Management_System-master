package com.rh4.repositories;

import com.rh4.entities.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAssignmentRepo extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByIntern(String intern);
    List<TaskAssignment> findByAssignedById(String assignedById); // Get tasks assigned by admin/guide
}