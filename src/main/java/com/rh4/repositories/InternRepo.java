package com.rh4.repositories;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.rh4.entities.*;

@Repository
public interface InternRepo extends JpaRepository<Intern, String> {

	@Query("SELECT i.internId FROM Intern i") // Assuming 'id' is the field name for intern ID
	List<String> findAllInternIds();

	Intern findTopByOrderByInternIdDesc();

	Intern findByEmail(String username);

	List<Intern> findByGroupId(long groupId);

	Optional<Intern> findById(String id);

	List<Intern> findByCancellationStatus(String cancellationStatus);

	long countByCancellationStatus(String cancellationStatus);

	@Query("SELECT i FROM Intern i WHERE i.isActive = false")
	List<Intern> getCancelledIntern();

	@Query("SELECT i FROM Intern i WHERE i.isActive = true")
	List<Intern> getCurrentInterns();

	@Query("SELECT i FROM Intern i " + "WHERE (:college IS NULL OR i.collegeName = :college) "
			+  "AND (:guide IS NULL OR i.guide = :guide) "
			+ "AND (:domain IS NULL OR i.domain = :domain) "
			+ "AND (:degree IS NULL OR i.degree = :degree) "
			+ "AND (:startDate IS NULL OR i.joiningDate >= :startDate) "
			+ "AND (:endDate IS NULL OR i.completionDate <= :endDate)" + "AND (i.isActive =:cancelled)")
	List<Intern> getFilteredInterns(@Param("college") String college,
			@Param("guide") Optional<Guide> guide, @Param("domain") String domain, @Param("degree") String degree, @Param("startDate") Date startDate,
			@Param("endDate") Date endDate, @Param("cancelled") boolean cancelled);

	@Query("SELECT i FROM Intern i " + "INNER JOIN i.group g "
			+ "WHERE (:college IS NULL OR i.collegeName = :college) "
			+ "AND (:guide IS NULL OR g.guide = :guide) " + "AND (:domain IS NULL OR i.domain = :domain) "
			+ "AND (:startDate IS NULL OR i.joiningDate >= :startDate) "
			+ "AND (:endDate IS NULL OR i.completionDate <= :endDate)" + "AND g.finalReportStatus = 'pending'")
	List<Intern> getPendingInternsFilter(String college, Optional<Guide> guide, String domain,
			Date startDate, Date endDate);

//	@Query("from InternApplication where status = 'rejected'")
//	public List<InternApplication> getInternRejectedStatus();

	@Query("SELECT i FROM InternApplication i WHERE i.status = 'rejected'")
	List<InternApplication> getInternRejectedStatus();

	@Modifying
	@Query("UPDATE Intern i SET i.profilePicture = :profilePicture WHERE i.internId = :internId")
	void updateProfilePicture(@Param("internId") String internId, @Param("profilePicture") byte[] profilePicture);

	@Query("SELECT DISTINCT i.projectDefinitionName FROM Intern i")
	List<String> findDistinctProjectDefinitionNames();

	@Query("SELECT DISTINCT i.gender FROM Intern i")
	List<String> findDistinctGenders();

	@Query("SELECT i FROM Intern i WHERE i.group.id = :groupId")
	List<Intern> findByGroupId(@Param("groupId") Long groupId);

	@Query("SELECT i FROM Intern i WHERE i.firstName = :internName")
	Intern findByFirstName(@Param("internName") String internName);
	Intern findByInternId(String internId); // Custom query method

	List<Intern> findByFirstNameStartingWithIgnoreCase(String firstName);

	@Query("SELECT i FROM Intern i WHERE i.group = :groupId")
	List<Intern> findByGroup_GroupId(String groupId);
	@Query("SELECT i FROM Intern i WHERE i.group.groupId = :groupId")
	List<Intern> findInternsByGroupId(@Param("groupId") String groupId);
	List<Intern> findByGroupGroupId(String groupId);
	List<Intern> findByGroup_Guide_GuideId(Long guideId);
	Page<Intern> findByGroup_Guide_GuideId(Long guideId, Pageable pageable);
	List<Intern> findByGroup(GroupEntity group);
	@Query("SELECT i FROM Intern i WHERE i.completionDate > CURRENT_DATE AND (i.cancellationStatus IS NULL OR LOWER(i.cancellationStatus) <> 'cancelled')")
	List<Intern> findInternsWithFutureCompletionDate();
	@Query("SELECT DISTINCT i.group.id FROM Intern i WHERE i.completionDate > CURRENT_DATE")
	List<Long> findActiveGroupIds();
	@EntityGraph(attributePaths = {"group"})
	@Query("SELECT i FROM Intern i")
	List<Intern> findAllWithGroup();

//	@Query("SELECT i FROM Intern i LEFT JOIN FETCH i.group")
//	Page<Intern> findAllWithGroup(Pageable pageable);
@EntityGraph(attributePaths = {"group"})
@Query("SELECT i FROM Intern i")
Page<Intern> findAllWithGroup(Pageable pageable);

//	@Query("SELECT i FROM Intern i LEFT JOIN FETCH i.group g LEFT JOIN FETCH g.guide")
//	List<Intern> findAllWithGroupAndGuide();
//@EntityGraph(attributePaths = {"group", "group.guide"})
//@Query("SELECT i FROM Intern i")
//List<Intern> findAllWithGroupAndGuide();

//	@Query("SELECT i FROM Intern i LEFT JOIN FETCH i.group g LEFT JOIN FETCH g.guide")
//	Page<Intern> findAllWithGroupAndGuide(Pageable pageable);
@EntityGraph(attributePaths = {"group", "group.guide"})
@Query("SELECT i FROM Intern i")
Page<Intern> findAllWithGroupAndGuide(Pageable pageable);
	List<Intern> findByCompletionDateAfter(LocalDate date);
	List<Intern> findByCompletionDateAfterAndCancellationStatusNot(LocalDate date, String status);
	@Query("SELECT i FROM Intern i WHERE i.internId NOT IN (SELECT r.internId FROM RRecord r)")
	List<Intern> findInternsNotInRecords();
	@EntityGraph(attributePaths = {"group", "group.guide"})
	@Query("SELECT i FROM Intern i")
	Page<Intern> findAllWithGroupAndGuideUsingEntityGraph(Pageable pageable);
	@Query("SELECT COUNT(DISTINCT i.group.id) FROM Intern i WHERE i.completionDate > :today")
	long countGroupsWithFutureInterns(@Param("today") LocalDate today);
	List<Intern> findByStatusIgnoreCase(String status);
	@Query("SELECT i FROM Intern i JOIN FETCH i.group g " +
			"WHERE i.group IS NOT NULL AND i.completionDate > :now")
	List<Intern> findAllValidForPendingReports(@Param("now") LocalDate now);
	@Query("SELECT g.groupId FROM GroupEntity g WHERE g.id = (SELECT i.group.id FROM Intern i WHERE i.internId = :internId)")
	String findGroupIdByInternId(@Param("internId") String internId);
	@Modifying
	@Transactional
	@Query("UPDATE Intern i SET i.alertSeen = 0 WHERE i.internId = :internId")
	void markAlertAsSeen(@Param("internId") String internId);

	@Transactional
	@Modifying
	@Query("UPDATE Intern i SET i.icardForm = :data WHERE i.internId = :internId")
	void updateICardForm(@Param("internId") String internId, @Param("data") byte[] data);

	@Transactional
	@Modifying
	@Query("UPDATE Intern i SET i.registrationForm = :data WHERE i.internId = :internId")
	void updateRegistrationForm(@Param("internId") String internId, @Param("data") byte[] data);

	@Transactional
	@Modifying
	@Query("UPDATE Intern i SET i.projectDefinitionForm = :data WHERE i.internId = :internId")
	void updateProjectDefinitionForm(@Param("internId") String internId, @Param("data") byte[] data);

	@Transactional
	@Modifying
	@Query("UPDATE Intern i SET i.securityForm = :data WHERE i.internId = :internId")
	void updateSecurityForm(@Param("internId") String internId, @Param("data") byte[] data);

	@Query("SELECT i FROM Intern i WHERE " +
			"STR(i.internId) LIKE CONCAT('%', :keyword, '%') OR " +
			"LOWER(i.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"STR(i.contactNo) LIKE CONCAT('%', :keyword, '%') OR " +
			"LOWER(i.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"STR(i.group.groupId) LIKE CONCAT('%', :keyword, '%') OR " +
			"LOWER(i.domain) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"LOWER(i.collegeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
			"STR(i.semester) LIKE CONCAT('%', :keyword, '%')")
	Page<Intern> searchWithGroupAndGuideUsingEntityGraph(@Param("keyword") String keyword, Pageable pageable);
}