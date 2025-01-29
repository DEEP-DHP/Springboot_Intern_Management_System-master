package com.rh4.repositories;

import com.rh4.entities.InternApplication;
import com.rh4.entities.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepo extends JpaRepository<Log, Long> {

    @Query("SELECT l FROM Log l WHERE l.action = 'Rejected'")
    List<Log> getRejectedInternLogs();
}