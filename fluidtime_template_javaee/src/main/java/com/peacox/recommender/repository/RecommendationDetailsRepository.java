package com.peacox.recommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RecommendationDetailsRepository extends JpaRepository<RecommendationDetails, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
	
}