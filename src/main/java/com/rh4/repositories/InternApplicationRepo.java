package com.rh4.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rh4.entities.InternApplication;

@Repository
public interface InternApplicationRepo extends JpaRepository<InternApplication, Long> {

	@Query("from InternApplication where status='approved'")
	public List<InternApplication> getInternApprovedStatus();

	//Method to fetch rejected interns
	@Query("from InternApplication where status='rejected' or finalStatus = 'failed'")
	public List<InternApplication> getInternRejectedStatus();

	public long countByStatus(String string);

	public long countByFinalStatus(String string);

	@Query("select COUNT(*) from InternApplication where status='approved' and finalStatus='pending'")
	public long countPendingInterviewApplications();

	public InternApplication findByEmail(String username);

	@Query("select COUNT(*) from InternApplication where status='approved' and finalStatus='passed' and groupCreated = false")
	public long countByGroupCreated();

	@Query("SELECT i FROM InternApplication i WHERE i.guideId = :guideId AND i.finalStatus = :status")
	List<InternApplication> findByGuideIdAndFinalStatus(@Param("guideId") long guideId, @Param("status") String status);
}