package com.rh4.services;

import com.rh4.entities.InternApplication;
import com.rh4.entities.Log;
import com.rh4.repositories.LogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {

    @Autowired
    private LogRepo logRepo;

    public void saveRejectedIntern(InternApplication intern) {
        Log log = new Log();
        log.setFirstName(intern.getFirstName());
        log.setLastName(intern.getLastName());
        log.setContactNo(intern.getContactNo());
        log.setEmail(intern.getEmail());
        log.setCollegeName(intern.getCollegeName());
        log.setBranch(intern.getBranch());
        log.setSemester(intern.getSemester());
        log.setDegree(intern.getDegree());
        log.setDomain(intern.getDomain());
        log.setAction("Rejected");
        log.setReason("Rejected by admin.");
        log.setTimestamp(LocalDateTime.now());

        logRepo.save(log);
    }
}