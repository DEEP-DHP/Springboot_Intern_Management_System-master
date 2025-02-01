package com.rh4.repositories;

import com.rh4.entities.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    List<Attendance> findByInternIdOrderByYearAscMonthAsc(String internId);
    List<Attendance> findByInternId(String internId);
//    List<Attendance> findAll(String internId);

}