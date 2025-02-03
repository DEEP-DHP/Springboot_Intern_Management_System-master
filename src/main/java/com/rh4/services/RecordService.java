package com.rh4.services;

import com.rh4.entities.RRecord;
import com.rh4.repositories.RecordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordService {
    @Autowired
    private RecordRepo recordRepo;

    public List<RRecord> getAllRecords() {
        return recordRepo.findAll();
    }

    public Optional<RRecord> getRecordById(Long id) {
        return recordRepo.findById(id);
    }

    public List<RRecord> getRecordsByInternId(String internId) {
        return recordRepo.findByInternId(internId);
    }

    public void saveRecord(RRecord record) {
        recordRepo.save(record);
    }
}