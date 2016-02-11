package com.hs.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hs.User;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
	User findById(Long id);
}
