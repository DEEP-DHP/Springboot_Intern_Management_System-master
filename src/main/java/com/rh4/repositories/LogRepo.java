package com.rh4.repositories;

import com.rh4.entities.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepo extends JpaRepository<Log, Long> {
    List<Log> findByInternId(String internId);
    // Fetch logs in descending order of timestamp
    @Query("SELECT l FROM Log l ORDER BY l.timestamp DESC")
    List<Log> findAllByOrderByTimestampDesc();
    List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    void deleteByTimestampBetween(LocalDateTime start, LocalDateTime end);
}