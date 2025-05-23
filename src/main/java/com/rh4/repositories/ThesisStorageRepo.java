package com.rh4.repositories;

import com.rh4.entities.ThesisStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThesisStorageRepo extends JpaRepository<ThesisStorage, Long> {
    Optional<ThesisStorage> findById(Long id);
    List<ThesisStorage> findAll();
    List<ThesisStorage> findByUploadDateBetween(LocalDateTime start, LocalDateTime end);


}
