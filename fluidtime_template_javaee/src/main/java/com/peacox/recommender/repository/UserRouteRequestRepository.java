package com.peacox.recommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRouteRequestRepository extends JpaRepository<UserRouteRequest, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
}