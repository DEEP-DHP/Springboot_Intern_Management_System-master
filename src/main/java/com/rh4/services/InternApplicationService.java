package com.rh4.services;

import com.rh4.entities.InternApplication;
import com.rh4.repositories.InternApplicationRepo;
import com.rh4.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InternApplicationService {

    @Autowired
    private InternApplicationRepo internApplicationRepo;

    @Autowired
    private LogService logService;

    public void updateStatusToRejected(Long internId) {
        Optional<InternApplication> optionalIntern = internApplicationRepo.findById(internId);

        if (optionalIntern.isPresent()) {
            InternApplication intern = optionalIntern.get();
            intern.setStatus("rejected");
            internApplicationRepo.save(intern);

            // Move data to log table
//            logService.saveRejectedIntern(intern);
        }
    }

    public List<InternApplication> getRejectedInterns() {
        return internApplicationRepo.getInternRejectedStatus(); // Fetch rejected interns from DB
    }
}