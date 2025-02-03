package com.rh4.repositories;

import com.rh4.entities.RRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordRepo extends JpaRepository<RRecord, Long> {
    List<RRecord> findByInternId(String internId);
}