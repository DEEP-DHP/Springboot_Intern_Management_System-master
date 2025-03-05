package com.rh4.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rh4.repositories.*;
import com.rh4.entities.MyUser;

import java.util.List;
import java.util.Optional;

@Service
public class MyUserService {

	@Autowired
	private UserRepo userRepo;
	
	public MyUser getUserByUsername(String email) {
		return userRepo.findByUsername(email);
	}
	public Optional<MyUser> findByUsername(String username) {
		return Optional.ofNullable(userRepo.findByUsername(username));
	}

	public void save(MyUser myUser) {
		userRepo.save(myUser);
	}

	// MyUserService.java
	public List<MyUser> findAllInterns() {
		return userRepo.findByRole("INTERN");
	}
}