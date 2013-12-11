package com.peacox.recommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RecommendationsWithRealEmissionsRepository extends JpaRepository<RecommendationsWithRealEmissions, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
	@Modifying  
	@Transactional
	@Query("update RecommendationsWithRealEmissions r set user_id = ?2, session_id = ?3 where id = ?1 ")
	void update(int id, long userId, String sessionId);
	
}