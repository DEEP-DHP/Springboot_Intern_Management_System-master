package com.rh4.repositories;

import com.rh4.entities.Attendance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    List<Attendance> findByInternIdOrderByYearAscMonthAsc(String internId);
    List<Attendance> findByInternId(String internId);
//    List<Attendance> findAll(String internId);
@Transactional
@Modifying
@Query("DELETE FROM Attendance a WHERE a.month = :month AND a.year = :year")
void deleteByMonthAndYear(@Param("month") String month, @Param("year") int year);
}