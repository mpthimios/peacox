package com.peacox.recommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RecommendationsRepository extends JpaRepository<Recommendations, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
}