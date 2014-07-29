package com.peacox.recommender.repository;

import java.util.List;

public interface UserProfileService {
	
	public UserProfile findUserProfileByUserId(Long id);
	public UserProfile create(UserProfile userProfile);
	public List<UserProfile> findAllUserProfiles();
}
