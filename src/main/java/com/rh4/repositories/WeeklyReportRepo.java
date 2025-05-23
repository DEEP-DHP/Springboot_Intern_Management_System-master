package com.rh4.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rh4.entities.GroupEntity;
import com.rh4.entities.WeeklyReport;

@Repository
public interface WeeklyReportRepo extends JpaRepository<WeeklyReport, Long> {

    @Query("FROM WeeklyReport w WHERE w.group = :group AND w.weekNo = :weekNo")
    WeeklyReport findByGroupAndWeekNo(@Param("group") GroupEntity group, @Param("weekNo") int weekNo);

    @Query("FROM WeeklyReport w WHERE w.group = :group ORDER BY w.reportSubmittedDate DESC")
    List<WeeklyReport> getRecentWeekNo(@Param("group") GroupEntity group);

    @EntityGraph(attributePaths = {"intern", "group", "group.guide"})
    List<WeeklyReport> findAllByGroupId(long id);

    @EntityGraph(attributePaths = {"intern", "group", "group.guide"})
    @Query("SELECT w FROM WeeklyReport w WHERE w.guide.guideId = :id")
    List<WeeklyReport> findAllByGuideId(long id);

    WeeklyReport findByWeekNoAndGroup(int weekNo, GroupEntity group);

    WeeklyReport findByInternInternIdAndWeekNo(String internId, int weekNo);

    List<WeeklyReport> findByReportSubmittedDate(Date reportSubmittedDate);

    @Query("SELECT w FROM WeeklyReport w WHERE YEAR(w.reportSubmittedDate) = :year")
    List<WeeklyReport> findReportsByYear(@Param("year") int year);

    WeeklyReport findTopByGroupIdOrderByReportSubmittedDateDesc(Long groupId);

    @Query("SELECT g FROM GroupEntity g WHERE g.id NOT IN " +
            "(SELECT DISTINCT w.group.id FROM WeeklyReport w WHERE w.deadline >= CURRENT_DATE)")
    List<GroupEntity> findGroupsWithOverdueReports();

    @Query("SELECT w FROM WeeklyReport w WHERE w.reportSubmittedDate IS NULL OR w.reportSubmittedDate > w.deadline")
    List<WeeklyReport> findOverdueReports();

    @Query("SELECT w FROM WeeklyReport w WHERE w.group.id NOT IN " +
            "(SELECT DISTINCT w2.group.id FROM WeeklyReport w2 WHERE w2.reportSubmittedDate IS NOT NULL)")
    List<WeeklyReport> findPendingReports();
    List<WeeklyReport> findByGroupGroupIdAndIsRead(String groupId, int isRead);

    @EntityGraph(attributePaths = {"intern", "group", "group.guide"})
    List<WeeklyReport> findByGroupGroupId(String groupId);
    List<WeeklyReport> findByReportSubmittedDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
//    @EntityGraph(attributePaths = {"group", "group.guide", "intern"})
//    @Query("SELECT w FROM WeeklyReport w")
//    Page<WeeklyReport> findAllWithGroupAndGuide(Pageable pageable);
@EntityGraph(attributePaths = {"group", "group.guide", "intern"})
@Query("SELECT w FROM WeeklyReport w")
List<WeeklyReport> findAllWithGroupAndGuide();
    @EntityGraph(attributePaths = {"group", "group.guide", "intern"})
    @Query("SELECT w FROM WeeklyReport w WHERE w.group.id IN :groupIds")
    List<WeeklyReport> findByGroupIdInWithGroupAndGuide(@Param("groupIds") List<Long> groupIds);
    @Query("SELECT wr FROM WeeklyReport wr JOIN FETCH wr.group g JOIN FETCH g.guide guide JOIN FETCH wr.intern intern")
    Stream<WeeklyReport> streamAllReports();
}
