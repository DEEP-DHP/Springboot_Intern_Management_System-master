package com.rh4.repositories;

import com.rh4.entities.Thesis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ThesisRepo  extends JpaRepository<Thesis, Long> {
    List<Thesis> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    Page<Thesis> findByIssueDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Thesis> findAll(Pageable pageable);
}
