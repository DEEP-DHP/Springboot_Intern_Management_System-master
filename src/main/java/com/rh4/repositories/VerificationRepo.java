package com.rh4.repositories;

import com.rh4.entities.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VerificationRepo extends JpaRepository<Verification, String> {
    List<Verification> findByStatus(String status);

    long countByStatus(String status);

    @Query("SELECT v FROM Verification v WHERE v.status = 'approved' AND v.verifiedDate BETWEEN :start AND :end")
    List<Verification> findApprovedVerificationsBetweenDates(@Param("start") LocalDateTime start,
                                                             @Param("end") LocalDateTime end);
}