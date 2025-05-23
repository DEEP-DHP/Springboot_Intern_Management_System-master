package com.rh4.repositories;

import com.rh4.entities.RRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RecordRepo extends JpaRepository<RRecord, Long> {
    List<RRecord> findByInternId(String internId);

//    @Query("SELECT r FROM RRecord r WHERE LOWER(r.internId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.collegeName) LIKE LOWER(CONCAT('%', :search, '%'))")
//    List<RRecord> findByInternIdContainingIgnoreCaseOrCollegeNameContainingIgnoreCase(@Param("search") String search);

//    List<RRecord> findByStatus(String status);
    @Query("SELECT r.finalReport FROM RRecord r WHERE r.internId = :internId")
    String findFinalReportByInternId(@Param("internId") String internId);
    RRecord findTopByInternIdOrderBySubmissionTimestampDesc(String internId);
    @Query("SELECT r.internId FROM RRecord r WHERE r.finalReport IS NOT NULL")
    List<String> findAllInternsWithFinalReport();
    @Query("SELECT r.internId, r.finalReport FROM RRecord r WHERE r.internId IN :ids")
    List<Object[]> findFinalReportsForInternIds(@Param("ids") List<String> internIds);
    @Query("SELECT r.internId, MAX(r.submissionTimestamp) FROM RRecord r WHERE r.internId IN :internIds GROUP BY r.internId")
    List<Object[]> findLatestTimestampsForInternIds(@Param("internIds") List<String> internIds);
    @Query("SELECT r.internId FROM RRecord r WHERE r.finalReport IS NOT NULL")
    List<String> findInternsWithFinalReport();
    @Query("SELECT r.internId FROM RRecord r WHERE r.finalReport IS NOT NULL")
    List<String> findInternIdsWithFinalReport();
    List<RRecord> findBySendAccountOrderByIdDesc(String sendAccount);

    Page<RRecord> findBySendAccount(String sendAccount, Pageable pageable);
}
