package com.rh4.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.rh4.entities.*;

public interface CollegeRepo extends JpaRepository<College, Long> {

	College findByName(String name);

	Optional<College> findByCollegeId(long id);
 
}