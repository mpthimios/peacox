package com.peacox.recommender.repository;

import java.util.List;

public interface UserService {
	
	public User findUserByUserId(Long id);
	public User create(User user);
	public List<User> findAllUsers();
}
