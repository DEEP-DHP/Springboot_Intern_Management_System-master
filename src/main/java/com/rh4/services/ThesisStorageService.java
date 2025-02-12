package com.rh4.services;

import com.rh4.entities.ThesisStorage;
import com.rh4.repositories.ThesisStorageRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ThesisStorageService {
    private final ThesisStorageRepo thesisStorageRepo;

    @Value("${thesis.storage.directory}") // Define this in application.properties
    private String storageDirectory;

    public ThesisStorageService(ThesisStorageRepo thesisStorageRepository) {
        this.thesisStorageRepo = thesisStorageRepository;
    }

    public ThesisStorage storeThesis(MultipartFile file, String internId) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(storageDirectory, fileName);
        Files.write(filePath, file.getBytes());

        ThesisStorage thesis = new ThesisStorage();
        thesis.setInternId(internId);
        thesis.setFileName(fileName);
        thesis.setFilePath(filePath.toString());
        thesis.setUploadDate(new Date());
        return thesisStorageRepo.save(thesis);
    }

    public List<ThesisStorage> getAllTheses() {
        return thesisStorageRepo.findAll();
    }

    public Optional<ThesisStorage> getThesisById(Long id) {
        return thesisStorageRepo.findById(id);
    }

    public void deleteThesis(Long id) {
        thesisStorageRepo.findById(id).ifPresent(thesis -> {
            File file = new File(thesis.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            thesisStorageRepo.delete(thesis);
        });
    }
}