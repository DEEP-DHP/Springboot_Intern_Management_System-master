package com.rh4.repositories;
import com.rh4.entities.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepo extends JpaRepository<GroupEntity,Long>{

	GroupEntity findTopByOrderByGroupIdDesc();
	
	@Query("FROM GroupEntity g WHERE g.guide IS NULL")
	public List<GroupEntity> getGroupEntityNoGuide();

	GroupEntity getByGroupId(String id);

	@Query("FROM GroupEntity g WHERE g.guide IS NOt NULL")
	List<GroupEntity> getAllocatedGroups();

	@Query("FROM GroupEntity g WHERE g.guide IS NULL")
	List<GroupEntity> getNotAllocatedGroups();

	List<GroupEntity> findByProjectDefinitionStatus(String string);
	
	@Query("from GroupEntity g where g.guide = :guide")
	public List<GroupEntity> getInternGroups(@Param("guide") Guide guide);

	long countByProjectDefinitionStatus(String projectDefinitionStatus);

	long countByFinalReportStatus(String finalReportStatus);


	List<GroupEntity> findByGuideAndProjectDefinitionStatus(Guide guide, String projectDefinitionStatus);

	List<GroupEntity> findByGuideAndFinalReportStatus(Guide guide, String finalReportStatus);

	List<GroupEntity> findByFinalReportStatus(String finalReportStatus);

	@Query("SELECT DISTINCT g.groupId FROM GroupEntity g")
	List<String> findAllGroupIds();
	GroupEntity findByGroupId(String groupId);

	@Query("SELECT g.groupId FROM GroupEntity g WHERE g.confirmationLetter = :status")
	List<String> findGroupIdsByConfirmationLetter(@Param("status") String status);
	List<GroupEntity> findAllByGroupIdIn(Set<String> groupIds);
	Page<GroupEntity> findByFinalReportStatus(String status, Pageable pageable);
	@Query("SELECT COUNT(g) FROM GroupEntity g WHERE g.guide = :guide AND g.finalReportStatus = 'gpending'")
	long countByGuideAndFinalReportStatusGPending(@Param("guide") Guide guide);
	@Query("SELECT COUNT(g) FROM GroupEntity g WHERE g.guide.guideId = :guideId AND g.projectDefinitionStatus = :status")
	long countPendingByGuideId(@Param("guideId") Long guideId, @Param("status") String status);
}
