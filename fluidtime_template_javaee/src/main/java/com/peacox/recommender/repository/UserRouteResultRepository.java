package com.peacox.recommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRouteResultRepository extends JpaRepository<UserRouteResult, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
	@Modifying  
	@Transactional
	@Query("update UserRouteResult u set session_id = ?2 where id = ?1")
	void update(int id, String sessionId);
}