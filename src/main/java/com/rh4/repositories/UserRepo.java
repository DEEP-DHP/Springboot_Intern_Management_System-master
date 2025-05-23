package com.rh4.repositories;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.rh4.entities.Admin;
import com.rh4.entities.MyUser;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepo extends CrudRepository<MyUser,Long>{
	
	@Query("SELECT u FROM MyUser u WHERE u.username = :username")
	public MyUser findByUsername(@Param("username") String username);
	
	@Modifying
	@Transactional
	@Query("UPDATE MyUser u SET u.password = :password, u.username = :emailId WHERE u.userId = :adminId AND u.role = :role")
	public void updateAdminUser(@Param("adminId") String adminId, @Param("password") String password, @Param("emailId") String emailId, @Param("role") String role);

	@Modifying
	@Transactional
	@Query("UPDATE MyUser u SET u.password = :password, u.username = :emailId WHERE u.userId = :HRId AND u.role = :role")
	public void updateHRUser(@Param("HRId") Long HRId, @Param("password") String password, @Param("emailId") String emailId, @Param("role") String role);

	@Modifying
	@Transactional
	@Query("UPDATE MyUser u SET u.password = :password, u.username = :emailId WHERE u.userId = :AccountId AND u.role = :role")
	public void updateAccountUser(@Param("AccountId") Long AccountId, @Param("password") String password, @Param("emailId") String emailId, @Param("role") String role);

	@Modifying
	@Transactional
	@Query("UPDATE MyUser u SET u.password = :password, u.username = :emailId WHERE u.userId = :guideId AND u.role = :role")
	public void updateGuideUser(@Param("guideId") String guideId, @Param("password") String password, @Param("emailId") String emailId, @Param("role") String role);

	@Modifying
	@Transactional
	@Query("DELETE FROM MyUser u WHERE u.username = :email AND u.role = :role ")
	public void deleteByUsername(@Param("email") String email,@Param("role") String role);

	List<MyUser> findByRole(String role);
	Optional<MyUser> findByUserId(String userId); // userId corresponds to internId

}